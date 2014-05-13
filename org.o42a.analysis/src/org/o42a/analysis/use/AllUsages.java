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

import java.lang.reflect.Array;

import org.o42a.util.ArrayUtil;


public final class AllUsages<U extends Usage<U>> implements UseSelector<U> {

	private final Class<? extends U> usageClass;
	private final String name;
	private U usages[];
	private AlwaysUsed<U> alwaysUsed;
	private NeverUsed<U> neverUsed;
	private DummyUser<U> dummyUser;

	public AllUsages(Class<? extends U> usageClass) {
		this(usageClass, null);
	}

	@SuppressWarnings("unchecked")
	public AllUsages(Class<? extends U> usageClass, String name) {
		this.usageClass = usageClass;
		this.name = name != null ? name : usageClass.getSimpleName();
		this.usages = (U[]) Array.newInstance(usageClass, 0);
	}

	public final Class<? extends U> getUsageClass() {
		return this.usageClass;
	}

	public final U[] usages() {
		return this.usages;
	}

	public final int size() {
		return this.usages.length;
	}

	public final Uses<U> alwaysUsed() {
		if (this.alwaysUsed != null) {
			return this.alwaysUsed;
		}
		return this.alwaysUsed = new AlwaysUsed<>(this);
	}

	public final Uses<U> neverUsed() {
		if (this.neverUsed != null) {
			return this.neverUsed;
		}
		return this.neverUsed = new NeverUsed<>(this);
	}

	public final User<U> dummyUser() {
		if (this.dummyUser != null) {
			return this.dummyUser;
		}
		return this.dummyUser = new DummyUser<>(this);
	}

	public final Usable<U> usable(Object used) {
		return new SimpleUsable<>(this, null, used);
	}

	public final Usable<U> usable(String name, Object used) {
		return new SimpleUsable<>(this, name, used);
	}

	@Override
	public final boolean acceptUsage(U usage) {
		return true;
	}

	@Override
	public final UseSelector<U> and(UseSelector<U> other) {
		return other;
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return this.name;
	}

	final int addUsage(U usage) {

		final int ordinal = this.usages.length;

		this.usages = ArrayUtil.append(this.usages, usage);

		return ordinal;
	}

}
