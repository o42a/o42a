/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.member.clause.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.Inclusions.noInclusions;
import static org.o42a.core.member.MemberRegistry.noDeclarations;
import static org.o42a.core.member.clause.impl.GroupRegistry.prohibitedContinuation;
import static org.o42a.core.object.def.Definitions.emptyDefinitions;

import org.o42a.core.Container;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.ValueType;


final class ClauseDefinition extends Obj {

	private DeclarativeBlock declarations;

	ClauseDefinition(DeclaredPlainClause clause) {
		super(clause);
	}

	@Override
	public final Member toMember() {
		return toClause().toMember();
	}

	@Override
	public DeclaredPlainClause toClause() {
		return (DeclaredPlainClause) getScope();
	}

	public DeclarativeBlock getDeclarations() {
		getMembers();// construct declarations
		return this.declarations;
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {

		final Obj clauseDeclaredIn;

		if (declaredIn != null) {
			clauseDeclaredIn = declaredIn;
		} else {
			clauseDeclaredIn = type().getLastDefinition();
		}

		return super.clause(memberId, clauseDeclaredIn);
	}

	@Override
	public String toString() {

		final DeclaredPlainClause clause = toClause();

		return clause != null ? clause.toString() : super.toString();
	}

	@Override
	protected Nesting createNesting() {
		return toClause().getDefinitionNesting();
	}

	@Override
	protected Ascendants buildAscendants() {

		final MemberKey overridden = toClause().getOverridden();

		if (overridden != null) {
			return overrideMember();
		}

		final Ascendants ascendants = new Ascendants(this);

		if (!toClause().getSubstitution().substitutes()) {
			return toClause().getAscendants().updateAscendants(ascendants);
		}

		return ascendants.setAncestor(
				ValueType.VOID.typeRef(this, getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {

		final BlockBuilder declarations =
				toClause().getBuilder().getDeclarations();

		if (declarations == null) {
			this.declarations =
					new DeclarativeBlock(this, this, noDeclarations());
			return;
		}

		final ObjectMemberRegistry registry;

		if (toClause().isTerminator()) {
			registry = new TerminatorRegistry(this);
		} else {
			registry = new ObjectMemberRegistry(noInclusions(), this);
		}

		this.declarations =
				new DeclarativeBlock(declarations, this, registry);
		declarations.buildBlock(this.declarations);
		registry.registerMembers(members);
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

	void define(Reproducer reproducer) {

		final Statements<?> statements = reproducer.getStatements();
		final DeclarativeBlock reproduction = getDeclarations().reproduce(
				reproducer.distributeBy(statements.nextDistributor()));

		if (reproduction != null) {
			statements.statement(reproduction);
		}
	}

	private Ascendants overrideMember() {

		Ascendants ascendants = new Ascendants(this);
		final MemberKey memberKey = toClause().getOverridden();
		final Obj container = toMember().getContainer().toObject();
		final Member overridden = container.member(memberKey);

		if (overridden == null) {
			return ascendants;
		}

		final Container substance = overridden.substance(dummyUser());
		final Obj object = substance.toObject();

		ascendants = ascendants.addMemberOverride(overridden);
		if (toClause().getSubstitution().substitutes()) {
			return ascendants;
		}
		if (!object.type().getValueType().isLink()) {
			return toClause().getAscendants().updateAscendants(ascendants);
		}

		return ascendants;
	}

	private static final class TerminatorRegistry extends ObjectMemberRegistry {

		TerminatorRegistry(ClauseDefinition owner) {
			super(noInclusions(), owner);
		}

		@Override
		public ClauseBuilder newClause(
				Statements<?> statements,
				ClauseDeclaration declaration) {
			prohibitedContinuation(declaration);
			return null;
		}

	}

}
