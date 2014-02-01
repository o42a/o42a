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


final class CompoundUseSelector<U extends Usage<U>> extends UseSelector<U> {

	private final UseSelector<U> first;
	private final UseSelector<U> second;

	CompoundUseSelector(UseSelector<U> first, UseSelector<U> second) {
		assert second != null :
			"Second use selector not specified";
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean acceptUsage(U usage) {
		return this.first.acceptUsage(usage) && this.second.acceptUsage(usage);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.first.hashCode();
		result = prime * result + this.second.hashCode();

		return result;
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

		final CompoundUseSelector<?> other = (CompoundUseSelector<?>) obj;

		if (!this.first.equals(other.first)) {
			return false;
		}
		if (!this.second.equals(other.second)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (this.second == null) {
			return super.toString();
		}
		return "(" + this.first + " & " + this.second + ')';
	}

}
