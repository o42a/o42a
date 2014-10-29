/*
    Utilities
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.util.fn;

import java.util.Objects;
import java.util.function.Function;


public final class AfterInit<T, V> implements Initializer<V> {

	private final Initializer<T> init;
	private final Function<T, V> apply;
	private V value;
	private boolean applied;

	AfterInit(Initializer<T> init, Function<T, V> apply) {
		this.init = init;
		this.apply = apply;
	}

	@Override
	public V get() {
		if (this.applied) {
			return this.value;
		}
		this.applied = true;
		return this.value = this.apply.apply(this.init.get());
	}

	@Override
	public final boolean isInitialized() {
		return this.applied;
	}

	@Override
	public final V getKnown() {
		return this.value;
	}

	@Override
	public final void set(V value) {
		this.applied = true;
		this.value = value;
	}

	public final void setSource(T value) {
		this.applied = false;
		this.value = null;
		this.init.set(value);
	}

	@Override
	public String toString() {
		if (this.applied) {
			return "???";
		}
		return Objects.toString(this.value);
	}

}
