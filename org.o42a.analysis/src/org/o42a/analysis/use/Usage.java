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


public abstract class Usage<U extends Usage<U>> extends UseSelector<U> {

	private final AllUsages<U> all;
	private final String name;
	private final int ordinal;

	public Usage(AllUsages<U> all, String name) {
		assert all != null :
			"All usages not specified";
		assert name != null :
			"Usage name not specified";
		this.all = all;
		this.name = name;
		this.ordinal = all.addUsage(self());
	}

	public final AllUsages<U> all() {
		return this.all;
	}

	public final String name() {
		return this.name;
	}

	public final int ordinal() {
		return this.ordinal;
	}

	@SuppressWarnings("unchecked")
	public final U self() {
		return (U) this;
	}

	public final User<U> selectiveUser(User<U> user) {
		if (all().size() == 1) {
			return user;
		}
		return new SelectiveUser<>(user, this);
	}

	@Override
	public final boolean acceptUsage(U usage) {
		return usage == self();
	}

	@Override
	public final UseSelector<U> and(UseSelector<U> other) {
		if (other == all()) {
			return this;
		}
		return super.and(other);
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return this.name;
	}

}
