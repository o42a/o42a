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

import java.util.Arrays;

import org.o42a.util.ArrayUtil;


final class AnyUseSelector<U extends Usage<U>> extends UseSelector<U> {

	private final UseSelector<U>[] selectors;

	@SafeVarargs
	AnyUseSelector(UseSelector<U>... selectors) {
		this.selectors = selectors;
	}

	@Override
	public UseSelector<U> or(UseSelector<U> other) {
		if (other.equals(this)) {
			return this;
		}
		if (other.getClass() != AnyUseSelector.class) {
			return append(other);
		}

		final AnyUseSelector<U> o = (AnyUseSelector<U>) other;
		AnyUseSelector<U> result = this;

		for (UseSelector<U> s : o.selectors) {
			result = result.append(s);
		}

		return result;
	}

	@Override
	public boolean acceptUsage(U usage) {
		for (UseSelector<U> selector : this.selectors) {
			if (selector.acceptUsage(usage)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.selectors);
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

		final AnyUseSelector<?> other = (AnyUseSelector<?>) obj;

		return Arrays.equals(this.selectors, other.selectors);
	}

	@Override
	public String toString() {
		if (this.selectors == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('(');
		if (this.selectors.length > 0) {
			out.append(this.selectors[0]);
			for (int i = 1; i < this.selectors.length; ++i) {
				out.append(" | ").append(this.selectors[i]);
			}
		}
		out.append(')');

		return out.toString();
	}

	final AnyUseSelector<U> append(UseSelector<U> selector) {
		for (UseSelector<U> s : this.selectors) {
			if (selector.equals(s)) {
				return this;
			}
		}
		return new AnyUseSelector<>(
				ArrayUtil.append(this.selectors, selector));
	}

}
