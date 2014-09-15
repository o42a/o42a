/*
    Compiler Core
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
package org.o42a.core.source;

import static org.o42a.util.fn.CondInit.condInit;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.o42a.util.fn.CondInit;


public final class Intrinsic<T> implements Supplier<T> {

	public static <T> BiPredicate<Intrinsics, Intrinsic<T>> hasIntrinsics() {
		return (ics, ic) -> ic.getIntrinsics().is(ics);
	}

	public static <T> CondInit<Intrinsics, Intrinsic<T>> intrInit(
			Function<Intrinsics, T> init) {
		return condInit(
				hasIntrinsics(),
				ics -> new Intrinsic<>(ics, init.apply(ics)));
	}

	private final Intrinsics intrinsics;
	private final T item;

	public Intrinsic(Intrinsics intrinsics, T item) {
		this.intrinsics = intrinsics;
		this.item = item;
	}

	public final Intrinsics getIntrinsics() {
		return this.intrinsics;
	}

	@Override
	public final T get() {
		return this.item;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.intrinsics.hashCode();
		result =
				prime * result
				+ ((this.item == null) ? 0 : this.item.hashCode());

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

		final Intrinsic<?> other = (Intrinsic<?>) obj;

		if (!this.intrinsics.is(other.intrinsics)) {
			return false;
		}
		if (this.item == null) {
			if (other.item != null) {
				return false;
			}
		} else if (!this.item.equals(other.item)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return Objects.toString(this.item);
	}

}
