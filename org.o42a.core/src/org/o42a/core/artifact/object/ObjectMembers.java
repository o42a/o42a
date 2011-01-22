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

	public void deriveMembers(Obj ascendant) {
		for (Member member : ascendant.getMembers()) {
			propagateMember(member);
		}
	}

	@Override
	protected final Map<MemberKey, Member> members() {
		return getObject().members();
	}

	@Override
	protected final Map<MemberId, Symbol> symbols() {
		return getObject().symbols();
	}

	@Override
	protected boolean register(Member member) {
		if (member.isAbstract()) {

			final Obj object = getObject();
			final boolean abstractAllowed =
				object.isAbstract()
				|| object.isPrototype()
				|| object.toClause() != null;

			if (!abstractAllowed) {
				object.getLogger().abstractNotOverridden(
						object,
						member.getDisplayName());
				return false;
			}
		}

		return super.register(member);
	}

}
