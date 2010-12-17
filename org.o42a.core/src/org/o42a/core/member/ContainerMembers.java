/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.member;

import java.util.Map;

import org.o42a.core.Container;
import org.o42a.core.member.field.Field;


public abstract class ContainerMembers {

	private final Container container;

	public ContainerMembers(Container container) {
		this.container = container;
	}

	public final Container getContainer() {
		return this.container;
	}

	public abstract Map<MemberKey, Member> members();

	public abstract Map<MemberId, Symbol> symbols();

	public void addMembers(Iterable<? extends Member> members) {
		for (Member member : members) {
			member.put(this);
		}
	}

	public void addFields(Iterable<? extends Field<?>> fields) {
		for (Field<?> field : fields) {
			field.put(this);
		}
	}

	@Override
	public String toString() {
		return "ContainerMembers" + members();
	}

}
