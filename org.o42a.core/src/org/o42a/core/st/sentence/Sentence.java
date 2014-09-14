/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.core.st.sentence;

import static org.o42a.core.st.Command.exitCommand;
import static org.o42a.core.st.Command.noCommands;
import static org.o42a.core.st.impl.SentenceErrors.declarationNotAlone;
import static org.o42a.util.fn.DoOnce.doOnce;
import static org.o42a.util.fn.Init.init;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Contained;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.impl.local.Locals;
import org.o42a.core.value.TypeParameters;
import org.o42a.util.fn.DoOnce;
import org.o42a.util.fn.Init;


public abstract class Sentence extends Contained {

	private final Block block;
	private final SentenceFactory sentenceFactory;
	private final ArrayList<Statements> alternatives = new ArrayList<>(1);
	private final Locals externalLocals;
	private Sentence prerequisite;
	private final Init<CommandTargets> targets = init(this::buildTargets);
	private boolean statementDropped;
	private final DoOnce executeInstructions =
			doOnce(this::doExecuteInstructions);

	protected Sentence(
			LocationInfo location,
			Block block,
			SentenceFactory sentenceFactory) {
		this(
				location,
				new NextSentenceDistributor(block),
				block,
				sentenceFactory);
	}

	private Sentence(
			LocationInfo location,
			NextSentenceDistributor distributor,
			Block block,
			SentenceFactory sentenceFactory) {
		super(location, distributor);
		this.block = block;
		this.sentenceFactory = sentenceFactory;
		this.externalLocals = distributor.locals();
	}

	public final Block getBlock() {
		return this.block;
	}

	public MemberRegistry getMemberRegistry() {
		return getBlock().getMemberRegistry();
	}

	public final SentenceFactory getSentenceFactory() {
		return this.sentenceFactory;
	}

	public final boolean isImperative() {
		return getBlock().isImperative();
	}

	public abstract SentenceKind getKind();

	public final boolean isInterrogation() {
		return getKind().isInterrogative() || getBlock().isInterrogation();
	}

	public final List<Statements> getAlternatives() {
		return this.alternatives;
	}

	public final boolean isEmpty() {
		return getAlternatives().isEmpty();
	}

	public final Sentence getPrerequisite() {
		return this.prerequisite;
	}

	public final boolean isConditional() {
		if (getPrerequisite() != null) {
			return true;
		}
		return getBlock().isConditional();
	}

	public final CommandTargets getTargets() {
		return this.targets.get();
	}

	public final Statements alternative(LocationInfo location) {

		final Statements alt = new Statements(location, this);

		this.alternatives.add(alt);

		return alt;
	}

	public TypeParameters<?> typeParameters(
			Scope scope,
			TypeParameters<?> expectedParameters) {

		TypeParameters<?> typeParameters = null;

		for (Statements alt : getAlternatives()) {

			final TypeParameters<?> altParameters =
					alt.typeParameters(scope, expectedParameters);

			if (altParameters == null) {
				continue;
			}
			if (typeParameters == null) {
				typeParameters = altParameters;
				continue;
			}
			if (typeParameters.assignableFrom(altParameters)) {
				continue;
			}
			if (altParameters.assignableFrom(typeParameters)) {
				typeParameters = altParameters;
				continue;
			}
			typeParameters = expectedParameters;
		}

		return typeParameters;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean separator = false;
		final Sentence prerequisite = getPrerequisite();

		if (prerequisite != null) {
			out.append(prerequisite).append(' ');
		}
		for (Statements alt : getAlternatives()) {
			if (!separator) {
				separator = true;
			} else {
				out.append("; ");
			}
			out.append(alt);
		}

		out.append(getKind().getSign());

		return out.toString();
	}

	final Locals externalLocals() {
		return this.externalLocals;
	}

	final Sentence firstPrerequisite() {

		Sentence prerequisite = getPrerequisite();

		if (prerequisite == null) {
			return null;
		}
		for (;;) {

			final Sentence prePrerequisite = prerequisite.getPrerequisite();

			if (prePrerequisite == null) {
				return prerequisite;
			}

			prerequisite = prePrerequisite;
		}
	}

	final void executeInstructions() {
		this.executeInstructions.doOnce();
	}

	final void setPrerequisite(Sentence prerequisite) {
		this.prerequisite = prerequisite;
	}

	final void dropStatement() {
		this.statementDropped = true;
	}

	final void reportEmptyInterrogation() {
		if (!this.statementDropped) {
			getLogger().warning(
					"prohibited_empty_interrogative_sentence",
					this,
					"Impty interrogative sentence");
		}
	}

	void reproduce(Block block, Reproducer reproducer) {

		final Sentence prerequisite = getPrerequisite();

		if (prerequisite != null) {
			prerequisite.reproduce(block, reproducer);
		}

		final Sentence reproduction = reproduceIn(block);

		for (Statements alt : getAlternatives()) {
			alt.reproduce(reproduction, reproducer);
		}
	}

	private Sentence reproduceIn(Block block) {
		switch (getKind()) {
		case INTERROGATIVE_SENTENCE:
			return block.interrogate(this);
		case EXCLAMATORY_SENTENCE:
			return block.exit(this);
		case DECLARATIVE_SENTENCE:
		case IMPERATIVE_SENTENCE:
			return block.declare(this);
		}
		throw new IllegalStateException(
				"Unsupported sentence kind: " + getKind());
	}

	private void doExecuteInstructions() {

		final Sentence prerequisite = getPrerequisite();

		if (prerequisite != null) {
			prerequisite.executeInstructions();
		}
		for (Statements alt : getAlternatives()) {
			alt.executeInstructions();
		}
	}

	private CommandTargets buildTargets() {
		return applyExitTargets(
				prerequisiteTargets().add(
						isImperative()
						? imperativeTargets() : declarativeTargets()));
	}

	private CommandTargets prerequisiteTargets() {

		final Sentence prerequisite = getPrerequisite();

		if (prerequisite == null) {
			return noCommands();
		}

		return prerequisite.getTargets().toPrerequisites();
	}

	private CommandTargets declarativeTargets() {

		CommandTargets result = noCommands();
		Statements first = null;

		for (Statements alt : getAlternatives()) {

			final CommandTargets targets = alt.getTargets();

			if (first == null) {
				first = alt;
			} else if (result.isEmpty()) {
				if (!result.haveError()) {
					first.reportEmptyAlternative();
					result = result.addError();
				}
				continue;
			} else if (!result.defining()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), result);
					result = result.addError();
				}
				continue;
			} else if (!targets.defining()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), targets);
					result = result.addError();
				}
				continue;
			}
			if (result.isEmpty()) {
				result = targets;
				continue;
			}
			result = result.add(targets);

			final boolean mayBeNonBreaking =
					(result.breaking() || targets.breaking())
					&& result.breaking() != targets.breaking();

			if (mayBeNonBreaking) {
				result = result.addPrerequisite();
			}
		}

		return result;
	}

	private CommandTargets imperativeTargets() {

		CommandTargets result = noCommands();
		Statements first = null;

		for (Statements alt : getAlternatives()) {

			final CommandTargets targets = alt.getTargets();

			if (first == null) {
				first = alt;
			} else if (result.isEmpty()) {
				if (!result.haveError()) {
					first.reportEmptyAlternative();
				}
				return result.addError();
			}
			if (!result.conditional() && result.looping()) {
				if (!result.haveError()) {
					result = result.addError();
					getLogger().error(
							"unreachable_alternative",
							targets,
							"Unreachable alternative");
				}
				continue;
			}
			if (result.isEmpty()) {
				result = targets;
				continue;
			}
			if (targets.isEmpty()) {
				continue;
			}
			result = result.add(targets);

			final boolean mayBeNonBreaking =
					(result.breaking() || targets.breaking())
					&& result.unconditionallyBreaking()
					!= targets.unconditionallyBreaking();

			if (mayBeNonBreaking) {
				result = result.addPrerequisite();
			}
			continue;
		}

		return result;
	}

	private CommandTargets applyExitTargets(CommandTargets targets) {

		final CommandTargets result;

		if (getKind().isInterrogative()
				&& targets.isEmpty()
				&& !targets.haveError()) {
			reportEmptyInterrogation();
			result = targets.addError();
		} else {
			result = targets;
		}
		if (!getKind().isExclamatory()) {
			return result;
		}

		return result.add(exitCommand(getLocation()));
	}

}
