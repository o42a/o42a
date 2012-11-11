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
import static org.o42a.compiler.ip.member.ClauseVisibility.IMPLICIT_CLAUSE;
import static org.o42a.compiler.ip.member.ClauseVisibility.clauseVisibilityByName;
import static org.o42a.compiler.ip.member.ClauseVisibility.clauseVisibilityByPrefix;
import static org.o42a.compiler.ip.ref.RefInterpreter.ADAPTER_FIELD_REF_IP;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberName.clauseName;
import static org.o42a.core.member.clause.ClauseDeclaration.anonymousClauseDeclaration;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.clause.AbstractClauseIdVisitor;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.ref.Ref;


final class ClauseIdVisitor
		extends AbstractClauseIdVisitor<ClauseDeclaration, Distributor> {

	public static final ClauseIdVisitor CLAUSE_ID_VISITOR =
			new ClauseIdVisitor();

	private ClauseIdVisitor() {
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

		final ClauseVisibility visibility = clauseVisibilityByName(ref);

		if (visibility == null) {
			p.getLogger().invalidDeclaration(ref);
			return null;
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

		return visibility.applyTo(declaration);
	}

	@Override
	public ClauseDeclaration visitDeclarableAdapter(
			DeclarableAdapterNode adapter,
			Distributor p) {

		final Ref adapterId = adapter.getMember().accept(
				ADAPTER_FIELD_REF_IP.bodyRefVisitor(),
				p);

		if (adapterId == null) {
			return null;
		}

		return clauseDeclaration(
				location(p, adapter),
				p,
				null,
				adapterId(adapterId.toStaticTypeRef()));
	}

	@Override
	public ClauseDeclaration visitPhrase(PhraseNode phrase, Distributor p) {

		final ExpressionNode prefix = phrase.getPrefix();

		if (prefix == null) {
			return super.visitPhrase(phrase, p);
		}
		if (clauseVisibilityByPrefix(prefix) != IMPLICIT_CLAUSE) {
			p.getContext().getLogger().invalidDeclaration(phrase);
			return null;
		}

		final PhrasePartNode[] clauses = phrase.getClauses();

		if (clauses.length != 1) {
			return super.visitPhrase(phrase, p);
		}

		return clauses[0].accept(new PhraseClauseIdVisitor(phrase), p);
	}

	@Override
	protected ClauseDeclaration visitClauseId(
			ClauseIdNode clauseId,
			Distributor p) {
		p.getLogger().invalidDeclaration(clauseId);
		return null;
	}

}
