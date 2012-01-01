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


public abstract class UseSelector<U extends Usage<U>> {

	public abstract boolean acceptUsage(U usage);

	public UseSelector<U> and(UseSelector<U> other) {
		if (equals(other)) {
			return this;
		}
		return new CompoundUseSelector<U>(this, other);
	}

	@SuppressWarnings("unchecked")
	public UseSelector<U> or(UseSelector<U> other) {
		if (equals(other)) {
			return this;
		}
		if (other.getClass() == AnyUseSelector.class) {
			return ((AnyUseSelector<U>) other).append(this);
		}
		return new AnyUseSelector<U>(this, other);
	}

	public UseSelector<U> not() {
		return new NegatedUseSelector<U>(this);
	}

}
