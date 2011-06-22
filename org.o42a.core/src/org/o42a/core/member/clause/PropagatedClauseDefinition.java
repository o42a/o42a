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
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.UserInfo;


final class PropagatedClauseDefinition extends PlainObject {

	private final PlainClause clause;

	public static Ascendants deriveSamples(
			UserInfo user,
			Clause clause,
			Ascendants ascendants) {

		final ObjectType containerType =
			clause.getScope().getEnclosingContainer().toObject().type(user);
		final TypeRef containerAncestor = containerType.getAncestor();

		if (containerAncestor != null) {

			final Member overriddenMember =
				containerAncestor.type(user)
				.getObject().member(clause.getKey());

			if (overriddenMember != null) {
				ascendants = ascendants.addMemberOverride(overriddenMember);
			}
		}

		final Sample[] containerSamples = containerType.getSamples();

		for (int i = containerSamples.length - 1; i >= 0; --i) {

			final Member overriddenMember =
				containerSamples[i].type(user)
				.getObject().member(clause.getKey());

			if (overriddenMember != null) {
				ascendants = ascendants.addMemberOverride(overriddenMember);
			}
		}

		return ascendants;
	}

	PropagatedClauseDefinition(PlainClause clause, PlainClause overridden) {
		super(clause, overridden.getObject());
		this.clause = clause;
	}

	@Override
	public final Member toMember() {
		return toClause().toMember();
	}

	@Override
	public final Clause toClause() {
		return this.clause;
	}

	@Override
	public boolean isPropagated() {
		return true;
	}

	@Override
	public String toString() {
		return this.clause != null ? this.clause.toString() : super.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return deriveSamples(
				type(dummyUser()),
				this.clause,
				new Ascendants(this));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

}
