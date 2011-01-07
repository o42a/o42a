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
package org.o42a.core.artifact.object;

import java.util.Map;

import org.o42a.core.member.*;


public class ObjectMembers extends ContainerMembers {

	public ObjectMembers(Obj object) {
		super(object);
	}

	public final Obj getObject() {
		return (Obj) getContainer();
	}

	@Override
	public final Map<MemberKey, Member> members() {
		return getObject().members();
	}

	@Override
	public final Map<MemberId, Symbol> symbols() {
		return getObject().symbols();
	}

	public void inheritMembers(Obj ascendant) {

		final Obj object = getObject();
		final Map<MemberKey, Member> allFields = members();
		final boolean abstractFieldsAllowed =
			object.isAbstract()
			|| object.isPrototype()
			|| object.toClause() != null;

		for (Member member : ascendant.getMembers()) {
			if (allFields.containsKey(member.getKey())) {
				continue;// field already overridden
			}

			final Member propagated = member.propagateTo(object.getScope());

			if (propagated == null) {
				continue;
			}
			if (!propagated.put(this)) {
				continue;
			}
			if (!abstractFieldsAllowed && member.isAbstract()) {
				object.getLogger().abstractNotOverridden(
						object,
						member.getDisplayName());
			}
		}
	}

	@Override
	public String toString() {
		return "ObjectMembers" + members();
	}

}
