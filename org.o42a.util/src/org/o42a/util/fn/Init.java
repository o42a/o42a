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
import java.util.function.Supplier;


public final class Init<V> implements Supplier<V> {

	public static <V> Init<V> init(Supplier<V> init) {
		return new Init<>(init);
	}

	private final Supplier<V> init;
	private V value;

	private Init(Supplier<V> init) {
		this.init = init;
	}

	public final boolean isInitialized() {
		return this.value != null;
	}

	public final V getKnown() {
		return this.value;
	}

	@Override
	public final V get() {
		if (this.value != null) {
			return this.value;
		}
		return this.value = this.init.get();
	}

	public final void set(V value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return Objects.toString(this.value, "???");
	}

}
