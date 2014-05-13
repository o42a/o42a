/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.clause;

import static org.o42a.compiler.ip.clause.ClauseInterpreter.invalidClauseName;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.string.Name;


final class NameExtractor
		implements StatementNodeVisitor<Name, CompilerContext> {

	private static final NameExtractor NAME_EXTRACTOR = new NameExtractor();

	public static Name extractName(
			CompilerContext context,
			StatementNode node) {
		return node.accept(NAME_EXTRACTOR, context);
	}

	public static Name extractNameOrImplied(
			CompilerContext context,
			ExpressionNode node) {

		final RefNode ref = node.toRef();

		if (ref != null) {

			final ScopeRefNode scopeRef = ref.toScopeRef();

			if (scopeRef != null) {
				if (scopeRef.getType() != ScopeType.IMPLIED) {
					invalidClauseName(context, node);
				}
				return null;
			}
		}

		return extractName(context, node);
	}

	private NameExtractor() {
	}

	@Override
	public Name visitMemberRef(MemberRefNode ref, CompilerContext p) {
		if (ref.getDeclaredIn() != null) {
			p.getLogger().prohibitedDeclaredIn(ref.getDeclaredIn());
			return null;
		}
		if (ref.getOwner() != null) {
			invalidClauseName(p, ref);
			return null;
		}

		final NameNode name = ref.getName();

		if (name == null) {
			invalidClauseName(p, ref);
			return null;
		}

		return name.getName();
	}

	@Override
	public Name visitStatement(StatementNode statement, CompilerContext p) {
		invalidClauseName(p, statement);
		return null;
	}

}
