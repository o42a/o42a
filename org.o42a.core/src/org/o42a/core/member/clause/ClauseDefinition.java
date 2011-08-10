/*
    Compiler Core
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
package org.o42a.core.member.clause;

import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.core.member.Inclusions.noInclusions;
import static org.o42a.core.member.MemberRegistry.noDeclarations;

import org.o42a.core.artifact.common.ObjectMemberRegistry;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
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
	public String toString() {

		final DeclaredPlainClause clause = toClause();

		return clause != null ? clause.toString() : super.toString();
	}

	@Override
	protected Ascendants buildAscendants() {

		Ascendants ascendants = new Ascendants(this);
		final Ref overridden = toClause().getBuilder().getOverridden();

		if (overridden != null) {
			ascendants = ascendants.setAncestor(overridden.toTypeRef());
		}
		if (!toClause().isSubstitution()) {
			return toClause().getAscendants().updateAscendants(ascendants);
		}
		if (overridden != null) {
			return ascendants;
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

		final ObjectMemberRegistry registry =
				new ObjectMemberRegistry(noInclusions(), this);

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

}
