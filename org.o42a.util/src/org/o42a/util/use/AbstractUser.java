/*
    Utilities
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
package org.o42a.util.use;

import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.Set;


public abstract class AbstractUser extends User {

	private HashSet<Object> userOf;

	@Override
	public final Set<?> getUserOf() {
		if (this.userOf == null) {
			return emptySet();
		}
		return this.userOf;
	}

	@Override
	<U> U use(Usable<U> usable) {

		final U use = usable.useBy(this);

		if (this.userOf == null) {
			this.userOf = new HashSet<Object>();
		}
		this.userOf.add(use);

		return use;
	}

}
