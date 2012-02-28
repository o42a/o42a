/*
    Compiler
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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.ADAPTER_FIELD_REF_IP;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.clauseName;
import static org.o42a.core.member.clause.ClauseDeclaration.anonymousClauseDeclaration;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.clause.AbstractClauseKeyVisitor;
import org.o42a.ast.clause.ClauseKeyNode;
import org.o42a.ast.clause.ClauseNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseDeclaration;


final class ClauseKeyVisitor
		extends AbstractClauseKeyVisitor<ClauseDeclaration, Distributor> {

	public static final ClauseKeyVisitor CLAUSE_KEY_VISITOR =
			new ClauseKeyVisitor();

	private static final ImpliedScopeChecker IMPLIED_SCOPE_CHECKER =
			new ImpliedScopeChecker();

	private ClauseKeyVisitor() {
	}

	@Override
	public ClauseDeclaration visitScopeRef(ScopeRefNode ref, Distributor p) {
		if (ref.getType() != ScopeType.IMPLIED) {
			return super.visitScopeRef(ref, p);
		}
		return anonymousClauseDeclaration(
				location(p, ref),
				p).implicit();
	}

	@Override
	public ClauseDeclaration visitMemberRef(MemberRefNode ref, Distributor p) {
		if (ref.getDeclaredIn() != null) {
			p.getLogger().prohibitedDeclaredIn(ref.getDeclaredIn());
		}

		final boolean implicit;
		final ExpressionNode owner = ref.getOwner();

		if (owner == null) {
			implicit = false;
		} else {
			implicit = owner.accept(IMPLIED_SCOPE_CHECKER, null);
			if (!implicit) {
				p.getLogger().invalidDeclaration(ref);
				return null;
			}
		}

		final NameNode name = ref.getName();
		final ClauseDeclaration declaration;

		if (name == null) {
			declaration = anonymousClauseDeclaration(
					location(p, ref),
					p);
		} else {
			declaration = clauseDeclaration(
					location(p, ref),
					p,
					name.getName(),
					clauseName(name.getName()));
		}

		if (!implicit) {
			return declaration;
		}

		return declaration.implicit();
	}

	@Override
	public ClauseDeclaration visitDeclarableAdapter(
			DeclarableAdapterNode adapter,
			Distributor p) {
		return clauseDeclaration(
				location(p, adapter),
				p,
				null,
				adapterId(adapter.getMember().accept(
						ADAPTER_FIELD_REF_IP.refVisitor(),
						p).toStaticTypeRef()));
	}

	@Override
	public ClauseDeclaration visitPhrase(PhraseNode phrase, Distributor p) {

		final ExpressionNode prefix = phrase.getPrefix();

		if (prefix == null) {
			return super.visitPhrase(phrase, p);
		}
		if (!prefix.accept(IMPLIED_SCOPE_CHECKER, null)) {
			p.getContext().getLogger().invalidDeclaration(phrase);
			return null;
		}

		final ClauseNode[] clauses = phrase.getClauses();

		if (clauses.length != 1) {
			return super.visitPhrase(phrase, p);
		}

		return clauses[0].accept(new PhraseClauseKeyVisitor(phrase), p);
	}

	@Override
	protected ClauseDeclaration visitClauseKey(
			ClauseKeyNode clauseKey,
			Distributor p) {
		p.getLogger().invalidDeclaration(clauseKey);
		return null;
	}

	private static final class ImpliedScopeChecker
			extends AbstractExpressionVisitor<Boolean, Void> {

		@Override
		public Boolean visitScopeRef(ScopeRefNode ref, Void p) {
			return ref.getType() == ScopeType.IMPLIED;
		}

		@Override
		protected Boolean visitExpression(ExpressionNode expression, Void p) {
			return Boolean.FALSE;
		}

	}

}
