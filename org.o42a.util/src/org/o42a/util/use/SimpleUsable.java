/*
    Utilities
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.util.use;


final class SimpleUsable<U extends Usage<U>> extends Usable<U> {

	private final String name;
	private final Object usable;
	private final UsableUser<U> user;

	SimpleUsable(AllUsages<U> allUsages, String name, Object usable) {
		super(allUsages);
		this.name = name;
		this.usable = usable;
		this.user = new UsableUser<U>(this);
	}

	@Override
	public final User<U> toUser() {
		return this.user;
	}

	@Override
	public String toString() {
		if (this.usable == null) {
			return super.toString();
		}
		if (this.name == null) {
			return this.usable.toString();
		}
		return this.name + '[' + this.usable + ']';
	}

}
