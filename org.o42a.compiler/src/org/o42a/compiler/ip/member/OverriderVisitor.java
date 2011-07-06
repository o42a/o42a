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

import static org.o42a.compiler.ip.AncestorVisitor.parseAscendants;
import static org.o42a.compiler.ip.Interpreter.CLAUSE_DEF_IP;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.member.ClauseExpressionVisitor.PHRASE_DECLARATIONS_VISITOR;
import static org.o42a.compiler.ip.member.ClauseExpressionVisitor.PHRASE_PREFIX_VISITOR;
import static org.o42a.compiler.ip.member.FieldDeclarableVisitor.declaredIn;
import static org.o42a.compiler.ip.member.FieldInterpreter.ADAPTER_FIELD_VISITOR;
import static org.o42a.core.member.AdapterId.adapterId;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.statement.AbstractDeclarableVisitor;
import org.o42a.ast.statement.DeclarableAdapterNode;
import org.o42a.ast.statement.DeclarableNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.MemberById;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;


final class OverriderVisitor
		extends AbstractExpressionVisitor<ClauseBuilder, ClauseBuilder> {

	static final DeclarableVisitor DECLARABLE_VISITOR =
		new DeclarableVisitor();
	static final OverriderVisitor OVERRIDER_VISITOR =
		new OverriderVisitor();

	private OverriderVisitor() {
	}

	@Override
	public ClauseBuilder visitScopeRef(ScopeRefNode ref, ClauseBuilder p) {
		if (ref.getType() != ScopeType.IMPLIED) {
			return super.visitScopeRef(ref, p);
		}

		return p.setAscendants(new AscendantsDefinition(
				location(p, ref),
				p.distribute()));
	}

	@Override
	public ClauseBuilder visitAscendants(
			AscendantsNode ascendants,
			ClauseBuilder p) {

		final AscendantsDefinition ascendantsDefinition =
			parseAscendants(CLAUSE_DEF_IP, ascendants, p.distribute());

		if (ascendantsDefinition == null) {
			return null;
		}

		return p.setAscendants(ascendantsDefinition);
	}

	@Override
	public ClauseBuilder visitPhrase(PhraseNode node, ClauseBuilder p) {

		final ClauseNode[] clauses = node.getClauses();

		if (clauses.length != 1) {
			p.getContext().getLogger().invalidClauseContent(node);
			return null;
		}

		final ClauseBuilder prefixed =
				node.getPrefix().accept(PHRASE_PREFIX_VISITOR, p);

		if (prefixed == null) {
			return null;
		}

		return clauses[0].accept(PHRASE_DECLARATIONS_VISITOR, prefixed);
	}

	@Override
	protected ClauseBuilder visitExpression(
			ExpressionNode expression,
			ClauseBuilder p) {

		final Distributor distributor = p.distribute();
		final Ref ref = expression.accept(
				Interpreter.CLAUSE_DEF_IP.expressionVisitor(),
				distributor);

		if (ref == null) {
			return null;
		}

		return p.setAscendants(
				new AscendantsDefinition(ref, distributor, ref.toTypeRef()));
	}

	private static final class DeclarableVisitor
			extends AbstractDeclarableVisitor<Ref, Distributor> {

		@Override
		public Ref visitMemberRef(MemberRefNode ref, Distributor p) {
			return Interpreter.PLAIN_IP.refVisitor().visitMemberRef(ref, p);
		}

		@Override
		public Ref visitDeclarableAdapter(
				DeclarableAdapterNode adapter,
				Distributor p) {

			final MemberRefNode member = adapter.getMember();
			final Ref adapterType = member.accept(ADAPTER_FIELD_VISITOR, p);

			if (adapterType == null) {
				return null;
			}

			return new MemberById(
					location(p, adapter),
					p,
					adapterId(adapterType.toStaticTypeRef()),
					declaredIn(Interpreter.CLAUSE_DEF_IP, member, p));
		}

		@Override
		protected Ref visitDeclarable(
				DeclarableNode declarable,
				Distributor p) {
			p.getLogger().invalidClauseContent(declarable);
			return null;
		}

	}

}
