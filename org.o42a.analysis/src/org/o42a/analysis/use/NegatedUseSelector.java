/*
    Compilation Analysis
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.analysis.use;


final class NegatedUseSelector<U extends Usage<U>> extends UseSelector<U> {

	private final UseSelector<U> negated;

	NegatedUseSelector(UseSelector<U> negated) {
		this.negated = negated;
	}

	@Override
	public final boolean acceptUsage(U usage) {
		return !this.negated.acceptUsage(usage);
	}

	@Override
	public UseSelector<U> not() {
		return this.negated;
	}

	@Override
	public int hashCode() {
		return 31 + this.negated.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final NegatedUseSelector<?> other = (NegatedUseSelector<?>) obj;

		return this.negated.equals(other.negated);
	}

	@Override
	public String toString() {
		return "(--" + this.negated + ')';
	}

}
