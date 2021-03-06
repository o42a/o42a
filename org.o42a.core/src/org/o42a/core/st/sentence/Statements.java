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

import static org.o42a.core.st.Command.noCommands;
import static org.o42a.core.st.impl.SentenceErrors.*;

import java.util.ArrayList;
import java.util.List;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.core.*;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.ref.impl.cond.RefCondition;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.imperative.LoopStatement;
import org.o42a.core.st.impl.imperative.NamedBlocks;
import org.o42a.core.st.impl.local.Locals;
import org.o42a.core.value.TypeParameters;
import org.o42a.util.string.Name;


public final class Statements extends Contained {

	private final Sentence sentence;
	private final ArrayList<Command> commands = new ArrayList<>(1);
	private Locals locals;
	private boolean statementDropped;
	private boolean incompatibilityReported;
	private CommandTargets targets;
	private int instructionsExecuted;

	Statements(LocationInfo location, Sentence sentence) {
		super(location, sentence.distribute());
		this.sentence = sentence;
		this.locals = sentence.externalLocals();
	}

	public Sentence getSentence() {
		return this.sentence;
	}

	public final boolean isImperative() {
		return getSentence().getBlock().isImperative();
	}

	public final boolean isInterrogation() {
		return getSentence().getKind().isInterrogative();
	}

	public final List<Command> getCommands() {
		return this.commands;
	}

	public final SentenceFactory getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public final MemberRegistry getMemberRegistry() {
		return getSentence().getMemberRegistry();
	}

	public final boolean localsAvailable() {
		return !this.locals.isEmpty();
	}

	public final CommandTargets getTargets() {
		if (this.targets != null) {
			return this.targets;
		}
		executeInstructions();
		return this.targets = determineTargets();
	}

	public final EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
		for (Command cmd : getCommands()) {

			final EscapeFlag escapeFlag = cmd.escapeFlag(analyzer, scope);

			if (!escapeFlag.isEscapeImpossible()) {
				return escapeFlag;
			}
		}

		return analyzer.escapeImpossible();
	}

	public final void expression(RefBuilder expression) {
		assert checkSameContext(expression);

		final Ref ref = expression.buildRef(nextDistributor());

		if (ref == null) {
			dropStatement();
			return;
		}

		assert checkSameContext(ref);

		statement(ref.toCondition(this));
	}

	public final void returnValue(RefBuilder value) {
		returnValue(value, value);
	}

	public final void returnValue(LocationInfo location, RefBuilder value) {
		assert checkSameContext(location);
		assert checkSameContext(value);

		if (isInterrogation()) {
			prohibitedInterrogativeReturn(location);
			dropStatement();
			return;
		}

		final Ref ref = value.buildRef(nextDistributor());

		if (ref == null) {
			dropStatement();
			return;
		}

		assert checkSameContext(ref);

		statement(ref.toValue(location, this));
	}

	public void yield(LocationInfo location, RefBuilder value) {
		assert checkSameContext(location);
		assert checkSameContext(value);

		if (isInterrogation()) {
			prohibitedInterrogativeYield(location);
			dropStatement();
			return;
		}

		final Ref ref = value.buildRef(nextDistributor());

		if (ref == null) {
			dropStatement();
			return;
		}

		assert checkSameContext(ref);

		locals().convertToFields();
		statement(ref.toYield(location, this));
	}

	public final FieldBuilder field(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return field(declaration, definition, null);
	}

	public final FieldBuilder alias(FieldDeclaration declaration, Ref ref) {
		return field(declaration, null, ref);
	}

	private FieldBuilder field(
			FieldDeclaration declaration,
			FieldDefinition definition,
			Ref ref) {
		if (isInterrogation()) {
			prohibitedInterrogativeField(declaration);
			dropStatement();
			return null;
		}
		if (getSentence().isConditional()) {
			getLogger().error(
					"prohibited_conditional_field",
					declaration,
					"Field declaration can not be conditional");
			dropStatement();
			return null;
		}

		final FieldBuilder builder;

		if (ref != null) {
			builder = getMemberRegistry().newAlias(declaration, ref);
		} else {
			builder = getMemberRegistry().newField(declaration, definition);
		}
		if (builder == null) {
			dropStatement();
			return null;
		}

		return builder;
	}

	public final ClauseBuilder clause(ClauseDeclaration declaration) {
		assert declaration.getKind().isPlain() :
			"Plain clause declaration expected: " + declaration;

		final ClauseBuilder clause =
				getMemberRegistry().newClause(this, declaration);

		if (clause == null) {
			dropStatement();
		}

		return clause;
	}

	public Group group(LocationInfo location, ClauseDeclaration declaration) {
		assert declaration.getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + declaration;

		final ClauseBuilder builder =
				getMemberRegistry().newClause(this, declaration);

		if (builder == null) {
			dropStatement();
			return null;
		}

		return new Group(location, this, builder);
	}

	public Block parentheses(LocationInfo location) {
		return parentheses(location, nextContainer());
	}

	public Block parentheses(LocationInfo location, Container container) {
		return parentheses(
				-1,
				location,
				distributeIn(container));
	}

	public final ImperativeBlock braces(LocationInfo location) {
		return braces(location, null, nextContainer());
	}

	public final ImperativeBlock braces(LocationInfo location, Name name) {
		return braces(location, name, nextContainer());
	}

	public final ImperativeBlock braces(
			LocationInfo location,
			Name name,
			Container container) {
		if (isInterrogation()) {
			prohibitedInterrogativeBraces(location);
			dropStatement();
			return null;
		}
		if (name != null) {

			final NamedBlocks namedBlocks =
					getSentence().getBlock().getNamedBlocks();

			if (!namedBlocks.declareBlock(location, name)) {
				dropStatement();
				return null;
			}
		}

		final ImperativeBlock braces = getSentenceFactory().createBraces(
				location,
				distributeIn(container),
				this,
				name);

		if (braces != null) {
			statement(braces);
		} else {
			dropStatement();
		}

		return braces;
	}

	public final Local local(LocationInfo location, Name name, Ref ref) {

		final Locals newLocals =
				locals().declareLocal(this, location, name, ref);

		if (newLocals == null) {
			return null;
		}

		this.locals = newLocals;

		final Local local = newLocals.getLocal();

		statement(new RefCondition(local));

		return local;
	}

	public void loop(LocationInfo location, Name name) {
		if (isInterrogation()) {
			getLogger().error(
					"prohibited_interrogation_loop",
					location,
					"Can not loop from interrogation");
			dropStatement();
			return;
		}

		final Block block = blockByName(location, name);

		if (block == null) {
			return;
		}

		statement(new LoopStatement(location, this, name));
	}

	public final Container nextContainer() {
		return this.locals.getContainer();
	}

	public final Distributor nextDistributor() {
		return distributeIn(nextContainer());
	}

	public final void statement(Statement statement) {
		if (statement == null) {
			dropStatement();
			return;
		}
		addStatement(statement);
	}

	public final boolean assertInstructionsExecuted() {
		assert this.instructionsExecuted == getCommands().size() :
			"Instructions not executed yet";
		return true;
	}

	@Override
	public String toString() {

		final List<Command> commands = getCommands();

		if (commands.isEmpty()) {
			return "<no statements>";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		for (Command command : commands) {
			if (!comma) {
				comma = true;
			} else {
				out.append(", ");
			}
			out.append(command);
		}

		return out.toString();
	}

	protected final void dropStatement() {
		this.statementDropped = true;
		getSentence().dropStatement();
	}

	protected final void addStatement(Statement statement) {
		statement.assertSameScope(this);

		final CommandEnv env = getSentence().getBlock().statementsEnv();

		this.commands.add(statement.command(env));
	}

	protected final void replaceStatement(int index, Statement statement) {

		final Command old = this.commands.get(index);

		this.commands.set(index, old.replaceWith(statement));
	}

	protected final void removeStatement(int index) {
		this.commands.remove(index);
	}

	final Locals locals() {
		return this.locals;
	}

	void reproduce(Sentence sentence, Reproducer reproducer) {

		final Statements reproduction = sentence.alternative(this);
		final Reproducer statementsReproducer =
				reproducer.reproduceIn(reproduction);

		for (Command command : getCommands()) {

			final Statement statementReproduction =
					command.getStatement().reproduce(
							statementsReproducer.distributeBy(
									reproduction.nextDistributor()));

			if (statementReproduction != null) {
				reproduction.statement(statementReproduction);
			}
		}
	}

	Block parentheses(
			int index,
			LocationInfo location,
			Distributor distributor) {

		final Block parentheses =
				getSentence().getSentenceFactory().createParentheses(
						location,
						distributor,
						this);

		if (index < 0) {
			addStatement(parentheses);
		} else {
			replaceStatement(index, parentheses);
		}

		return parentheses;
	}

	final int getInstructionsExecuted() {
		return this.instructionsExecuted;
	}

	final void setInstructionsExecuted(int instructionsExecuted) {
		this.instructionsExecuted = instructionsExecuted;
	}

	final void executeInstructions() {
		new InstructionExecutor(this).executeAll();
	}

	TypeParameters<?> typeParameters(
			Scope scope,
			TypeParameters<?> expectedParameters) {
		executeInstructions();

		// Statements contain at most one value.
		for (Command command : getCommands()) {
			if (!command.getStatement().isValid()) {
				continue;
			}

			final TypeParameters<?> typeParameters =
					command.typeParameters(scope);

			if (typeParameters == null) {
				continue;
			}
			if (!expectedParameters.assignableFrom(typeParameters)) {
				if (!this.incompatibilityReported) {
					this.incompatibilityReported = true;
					scope.getLogger().incompatible(
							command.getLocation(),
							expectedParameters);
				}
				return null;
			}

			return typeParameters;
		}

		return null;
	}

	void reportEmptyAlternative() {
		if (!this.statementDropped) {
			getLogger().error(
					"prohibited_empty_alternative",
					this,
					"Empty alternative");
		}
	}

	private boolean checkSameContext(LocationInfo location) {
		assert location.getLocation().getContext() == getContext() :
			location + " has wrong context: "
				+ location.getLocation().getContext()
			+ ", but " + getContext() + " expected";
		return true;
	}

	private Block blockByName(LocationInfo location, Name name) {

		Block block = getSentence().getBlock();

		for (;;) {
			if (block.hasName(name)) {
				return block;
			}

			final Statements enclosing = block.getEnclosing();

			if (enclosing == null) {
				break;
			}
			block = enclosing.getSentence().getBlock();
		}

		dropStatement();
		if (name == null) {
			getLogger().error(
					"prohibited_declarative_loop",
					location,
					"Loops are only allowed within imperative blocks");
		} else {
			getLogger().error(
					"unresolved_block",
					location,
					"Imperative block with name '%s' does not exist",
					name);
		}

		return null;
	}

	private CommandTargets determineTargets() {

		CommandTargets result = noCommands();
		CommandTargets prev = noCommands();
		CommandTargets firstDeclaring = null;

		for (Command command : getCommands()) {

			final CommandTargets targets = command.getTargets();

			if (targets.declaring()) {
				if (firstDeclaring != null) {
					if (!result.haveError()) {
						declarationNotAlone(getLogger(), targets);
						result = result.addError();
					}
					continue;
				}
				firstDeclaring = targets;
				if (result.defining() && !result.haveError()) {
					declarationNotAlone(getLogger(), firstDeclaring);
					result = result.addError();
				}
				continue;
			}
			if (firstDeclaring != null && !targets.isEmpty()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), firstDeclaring);
					result = result.addError();
				}
				continue;
			}
			if (!prev.breaking() || prev.havePrerequisite()) {
				if (targets.breaking()) {
					prev = targets;
				} else {
					prev = targets.toPreconditions();
				}
				result = result.add(prev);
				continue;
			}
			if (result.haveError()) {
				continue;
			}
			result = result.addError();
			getLogger().error(
					"unreachable_statement",
					targets,
					"Unreachable statement");
		}

		if (firstDeclaring != null && result.isEmpty()) {
			return result.add(firstDeclaring);
		}

		return result;
	}

}
