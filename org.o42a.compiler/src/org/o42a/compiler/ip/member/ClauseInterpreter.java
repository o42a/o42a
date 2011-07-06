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
import static org.o42a.compiler.ip.member.OverriderVisitor.DECLARABLE_VISITOR;
import static org.o42a.compiler.ip.member.OverriderVisitor.OVERRIDER_VISITOR;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.*;
import org.o42a.core.*;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.Group;
import org.o42a.core.st.sentence.Statements;


public class ClauseInterpreter {

	public static Ref clause(
			CompilerContext context,
			ClauseDeclaratorNode declarator,
			Statements<?> statements) {

		final Distributor distributor =
			new Placed(context, declarator, statements.nextDistributor())
			.distribute();
		final ClauseDeclaration declaration =
			declarator.getClauseKey().accept(CLAUSE_KEY_VISITOR, distributor);

		if (declaration == null) {
			return null;
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
				return null;
			}

			reuseClauses(group.getBuilder(), declarator);
			result = group.parentheses();
		}

		if (result != null) {
			statements.statement(result);
		}

		return null;
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

		final Ref overridden = declarator.getDeclarable().accept(
				DECLARABLE_VISITOR,
				declaration.distribute());

		if (overridden == null) {
			return null;
		}

		final ClauseBuilder builder =
			p.clause(declaration.setKind(ClauseKind.OVERRIDER));

		if (builder == null) {
			return null;
		}

		if (target.isPrototype()) {
			builder.prototype();
		}
		builder.setOverridden(overridden);

		return definition.accept(OVERRIDER_VISITOR, builder);
	}

	static ClauseBuilder reuseClauses(
			ClauseBuilder builder,
			ClauseDeclaratorNode declarator) {
		for (ReusedClauseNode reused : declarator.getReused()) {

			final RefNode clause = reused.getClause();

			if (clause == null) {
				continue;
			}

			final Ref reusedRef =
					clause.accept(CLAUSE_DECL_IP.refVisitor(), builder.distribute());

			if (reusedRef == null) {
				continue;
			}

			builder.reuseClause(reusedRef);
		}

		return builder;
	}

	private ClauseInterpreter() {
	}

}
