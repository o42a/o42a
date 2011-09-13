/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DECL_IP;
import static org.o42a.compiler.ip.member.ClauseKeyVisitor.CLAUSE_KEY_VISITOR;
import static org.o42a.compiler.ip.member.OverriderDeclarableVisitor.OVERRIDER_DECLARABLE_VISITOR;
import static org.o42a.compiler.ip.member.OverriderDefinitionVisitor.OVERRIDER_DEFINITION_VISITOR;

import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.clause.OutcomeNode;
import org.o42a.ast.clause.ReusedClauseNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.Group;
import org.o42a.core.st.sentence.Statements;


public class ClauseInterpreter {

	public static void clause(
			CompilerContext context,
			ClauseDeclaratorNode declarator,
			Statements<?> statements) {

		final Distributor distributor =
				new Placed(context, declarator, statements.nextDistributor())
				.distribute();
		ClauseDeclaration declaration =
				declarator.getClauseKey().accept(
						CLAUSE_KEY_VISITOR,
						distributor);

		if (declaration == null) {
			return;
		}
		if (declarator.requiresContinuation()) {
			declaration = declaration.requireContinuation();
		}

		final Statement result;
		final StatementNode content = declarator.getContent();

		if (content != null) {
			result = content.accept(
					new ClauseContentVisitor(declaration, declarator),
					statements);
		} else {

			final Group group = statements.group(
					new Location(context, declarator),
					declaration.setKind(ClauseKind.GROUP));

			if (group == null) {
				return;
			}

			declare(group.getBuilder(), declarator);
			result = group.parentheses();
		}

		if (result != null) {
			statements.statement(result);
		}
	}

	static ClauseBuilder buildOverrider(
			ClauseDeclaration declaration,
			DeclaratorNode declarator,
			Statements<?> p) {

		final DeclarationTarget target = declarator.getTarget();

		if (!target.isOverride() || target.isAbstract()) {
			p.getLogger().invalidClauseContent(
					declarator.getDefinitionAssignment());
			return null;
		}

		final ExpressionNode definition = declarator.getDefinition();

		if (definition == null) {
			return null;
		}

		final ClauseBuilder builder =
				p.clause(declaration.setKind(ClauseKind.OVERRIDER));

		declarator.getDeclarable().accept(
				OVERRIDER_DECLARABLE_VISITOR,
				builder);
		if (builder == null) {
			return null;
		}
		if (target.isPrototype()) {
			builder.prototype();
		}

		return definition.accept(OVERRIDER_DEFINITION_VISITOR, builder);
	}

	static ClauseBuilder declare(
			ClauseBuilder builder,
			ClauseDeclaratorNode declarator) {
		return reuseClauses(setOutcome(builder, declarator), declarator);
	}

	private static ClauseBuilder setOutcome(
			ClauseBuilder builder,
			ClauseDeclaratorNode declarator) {

		final OutcomeNode outcomeNode = declarator.getOutcome();

		if (outcomeNode == null) {
			return builder;
		}

		final RefNode outcomeValueNode = outcomeNode.getValue();

		if (outcomeValueNode == null) {
			return builder;
		}

		return builder.setOutcome(outcomeValueNode.accept(
				Interpreter.CLAUSE_DEF_IP.refVisitor(),
				builder.distribute()));
	}

	private static ClauseBuilder reuseClauses(
			ClauseBuilder builder,
			ClauseDeclaratorNode declarator) {
		for (ReusedClauseNode reused : declarator.getReused()) {

			final RefNode clause = reused.getClause();
			final Ref reusedRef;

			if (clause == null) {
				if (reused.getReuseContents() != null) {
					builder.reuseObject();
				}
				continue;
			}

			reusedRef = clause.accept(
					CLAUSE_DECL_IP.refVisitor(),
					builder.distribute());

			if (reusedRef == null) {
				continue;
			}

			builder.reuseClause(reusedRef, reused.getReuseContents() != null);
		}

		return builder;
	}

	private ClauseInterpreter() {
	}

}
