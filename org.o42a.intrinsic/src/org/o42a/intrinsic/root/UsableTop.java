/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import org.o42a.core.Scope;
import org.o42a.util.use.Usable;
import org.o42a.util.use.User;


final class UsableTop extends Usable<Scope> {

	private final Top top;

	UsableTop(Top top) {
		this.top = top;
	}

	@Override
	public String toString() {
		if (this.top == null) {
			return super.toString();
		}
		return this.top.toString();
	}

	@Override
	protected Scope createUsed(User user) {
		return this.top;
	}

}
