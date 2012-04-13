/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.source.CompilerLogger.logAnotherLocation;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.declarative.ExplicitInclusion;
import org.o42a.core.st.impl.imperative.BracesWithinDeclaratives;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.log.Loggable;


public class Declaratives extends Statements<Declaratives, Definer> {

	private DeclarativesEnv env;
	private DefinerEnv lastEnv;
	private DefinitionTargets definitionTargets;
	private DefinerEnv effectiveEnv = null;

	Declaratives(
			LocationInfo location,
			DeclarativeSentence sentence,
			Declaratives oppositeOf) {
		super(location, sentence, oppositeOf);
		if (oppositeOf != null) {
			this.lastEnv = new OppositeStartEnv(
					getSentence().getAltEnv(),
					oppositeOf.getEnv());
 		} else {
 			this.lastEnv = new AltStartEnv(getSentence().getAltEnv());
 		}
	}

	@Override
	public final DeclarativeSentence getSentence() {
		return (DeclarativeSentence) super.getSentence();
	}

	@Override
	public final DeclarativeFactory getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public DefinitionTargets getDefinitionTargets() {
		if (this.definitionTargets != null) {
			return this.definitionTargets;
		}

		executeInstructions();

		final List<Definer> definers = getImplications();
		DefinitionTargets result = noDefinitions();
		final int size = definers.size();
		final boolean inhibit = isInhibit();

		for (int i = 0; i < size; ++i) {

			final Definer definer = definers.get(i);
			final DefinitionTargets targets = definer.getDefinitionTargets();

			if (targets.isEmpty()) {
				continue;
			}
			if (inhibit && targets.haveDeclaration()) {
				if (targets.haveField()) {
					getLogger().error(
							"prohibited_inhibit_field",
							definer,
							"Field declaration can not be used as inhibit");
				} else {
					getLogger().error(
							"prohibited_inhibit_value",
							definer,
							"Self-assignment can not be used as inhibit");
				}
				continue;
			}
			if (result.haveDeclaration()) {

				final Loggable location =
						logAnotherLocation(definer, result.lastDeclaration());

				if (result.haveField()) {
					getLogger().error(
							"redundant_statement_after_field",
									location,
									"Redundant statement after"
									+ " the field declaration");
				} else {
					getLogger().error(
							"redundant_statement_after_value",
							location,
							"Redundant statement after the self-assignment");
				}
				if (this.effectiveEnv == null) {

					final int nextIdx = i + 1;

					if (nextIdx < size) {
						this.effectiveEnv = definers.get(nextIdx).env();
					} else {
						this.effectiveEnv = lastEnv();
					}
				}

				result = targets;

				continue;
			}

			result = targets.add(result);
		}

		return this.definitionTargets = result;
	}

	public final DefinerEnv getEnv() {
		if (this.env != null) {
			return this.env;
		}
		return this.env = new DeclarativesEnv();
	}

	@Override
	public final DeclarativeBlock parentheses(LocationInfo location) {
		return parentheses(location, getContainer());
	}

	@Override
	public final DeclarativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return (DeclarativeBlock) super.parentheses(location, container);
	}

	@Override
	public void ellipsis(LocationInfo location, String name) {
		dropStatement();
		getLogger().prohibitedDeclarativeEllipsis(location);
	}

	@Override
	public void include(LocationInfo location, String tag) {
		if (!getMemberRegistry().inclusions().include(location, tag)) {
			return;
		}
		statement(new ExplicitInclusion(location, this, tag));
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(new BracesWithinDeclaratives(
				braces,
				nextDistributor(),
				braces));
	}

	@Override
	protected Definer implicate(Statement statement) {

		final Definer definer = statement.define(lastEnv());

		this.lastEnv = definer.nextEnv();

		return definer;
	}

	protected Definitions define(Scope scope) {

		final DefinitionTargets kinds = getDefinitionTargets();

		if (!kinds.haveDefinition()) {
			return null;
		}

		final List<Definer> definers = getImplications();

		for (int i = definers.size() - 1; i >= 0; --i) {

			final Definer definer = definers.get(i);
			final Definitions definitions = definer.define(scope);

			if (definitions != null) {
				return definitions;
			}
		}

		throw new IllegalStateException("Missing definition: "+ this);
	}

	private final DefinerEnv lastEnv() {
		return this.lastEnv;
	}

	private final DefinerEnv effectiveEnv() {
		getDefinitionTargets();
		if (this.effectiveEnv != null) {
			return this.effectiveEnv;
		}
		return this.effectiveEnv = lastEnv();
	}

	private final class AltStartEnv extends DefinerEnv {

		private final DefinerEnv initialEnv;

		AltStartEnv(DefinerEnv initialEnv) {
			this.initialEnv = initialEnv;
		}

		@Override
		public boolean hasPrerequisite() {
			return !isInhibit() && this.initialEnv.hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			if (isInhibit()) {
				return logicalTrue(Declaratives.this, scope);
			}
			return this.initialEnv.prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return !isInhibit() && this.initialEnv.hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			if (isInhibit()) {
				return logicalTrue(Declaratives.this, scope);
			}
			return this.initialEnv.precondition(scope);
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			if (isInhibit()) {
				return null;
			}
			return this.initialEnv.getExpectedValueStruct();
		}

	}

	private final class OppositeStartEnv extends DefinerEnv {

		private final DefinerEnv initialEnv;
		private final DefinerEnv inhibitEnv;

		OppositeStartEnv(DefinerEnv initialEnv, DefinerEnv inhibitEnv) {
			this.initialEnv = initialEnv;
			this.inhibitEnv = inhibitEnv;
		}

		@Override
		public boolean hasPrerequisite() {
			return !isInhibit() && this.initialEnv.hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			if (isInhibit()) {
				return logicalTrue(Declaratives.this, scope);
			}
			return this.initialEnv.prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return true;
		}

		@Override
		public Logical precondition(Scope scope) {

			final Logical permission =
					this.inhibitEnv.precondition(scope).negate();

			if (isInhibit()) {
				return permission;
			}

			return this.initialEnv.precondition(scope).and(permission);
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			if (isInhibit()) {
				return null;
			}
			return this.initialEnv.getExpectedValueStruct();
		}

	}

	private final class DeclarativesEnv extends DefinerEnv {

		@Override
		public boolean hasPrerequisite() {
			return effectiveEnv().hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return effectiveEnv().prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return effectiveEnv().hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			return effectiveEnv().precondition(scope);
		}

		@Override
		public String toString() {

			final DefinerEnv effectiveEnv = Declaratives.this.effectiveEnv;

			if (effectiveEnv != null) {
				return effectiveEnv.toString();
			}

			return Declaratives.this + "?";
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return getSentence().getBlock()
					.getInitialEnv().getExpectedValueStruct();
		}

	}

}
