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


public final class ReInit<V> implements Supplier<V> {

	public static <V> ReInit<V> reInit(Supplier<V> init, Supplier<V> retry) {
		return new ReInit<>(init, retry);
	}

	private final Supplier<V> init;
	private final Supplier<V> retry;
	private V value;
	private byte status;

	private ReInit(Supplier<V> init, Supplier<V> retry) {
		this.init = init;
		this.retry = retry;
	}

	public final boolean isInitialized() {
		return this.status > 0;
	}

	public final boolean isInitializing() {
		return this.status < 0;
	}

	public final V getKnown() {
		return this.value;
	}

	@Override
	public final V get() {
		if (isInitialized()) {
			return this.value;
		}
		if (!isInitializing()) {
			this.status = -1;
			try {

				final V value = this.init.get();

				if (value != null && !isInitialized()) {
					return this.value = value;
				}
			} finally {
				this.status = 1;
			}
		}

		return this.value = this.retry.get();
	}

	public final void set(V value) {
		this.status = 1;
		this.value = value;
	}

	@Override
	public String toString() {
		return Objects.toString(this.value, isInitialized() ? "null" : "???");
	}

}
