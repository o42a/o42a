/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import java.util.HashSet;
import java.util.Iterator;

import org.o42a.util.use.UseInfo;
import org.o42a.util.use.Uses;


final class MemberUses extends Uses {

	private final String name;
	private final Member member;
	private final HashSet<UseInfo> uses = new HashSet<UseInfo>();

	MemberUses(String name, Member member) {
		this.name = name;
		this.member = member;
	}

	public void useBy(UseInfo use) {
		this.uses.add(use);
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return this.name + '[' + this.member + ']';
	}

	@Override
	protected Iterator<? extends UseInfo> usedBy() {
		return this.uses.iterator();
	}

}
