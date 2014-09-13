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
import java.util.function.*;


public final class CondInit<T, V> implements Function<T, V> {

	public static <V> CondInit<Void, V> condInit(
			Predicate<V> condition,
			Supplier<V> init) {
		return condInit(
				(n, v) -> condition.test(v),
				n -> init.get());
	}

	public static <T, V> CondInit<T, V> condInit(
			BiPredicate<T, V> condition,
			Function<T, V> init) {
		return new CondInit<>(condition, init);
	}

	private final BiPredicate<T, V> condition;
	private final Function<T, V> init;
	private V value;

	private CondInit(BiPredicate<T, V> condition, Function<T, V> init) {
		this.condition = condition;
		this.init = init;
	}

	public final boolean isInitialized() {
		return this.value != null;
	}

	public final V getKnown() {
		return this.value;
	}

	@Override
	public V apply(T t) {
		if (this.value != null && this.condition.test(t, this.value)) {
			return this.value;
		}
		return this.value = this.init.apply(t);
	}

	public final void set(V value) {
		this.value = value;
	}

	@Override
	public final String toString() {
		return Objects.toString(this.value);
	}

}
