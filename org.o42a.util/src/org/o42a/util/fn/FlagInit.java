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
import java.util.function.BooleanSupplier;


public class FlagInit implements BooleanSupplier {

	public static FlagInit boolInit(BooleanSupplier init) {
		return new FlagInit(init);
	}

	private final BooleanSupplier init;
	private byte value;

	private FlagInit(BooleanSupplier init) {
		this.init = init;
	}

	public final boolean isInitialized() {
		return this.value != 0;
	}

	public final Boolean getKnown() {
		return this.value == 0 ? null : this.value > 0;
	}

	@Override
	public final boolean getAsBoolean() {
		return get();
	}

	public final boolean get() {
		if (this.value != 0) {
			return this.value > 0;
		}

		final boolean result = this.init.getAsBoolean();

		set(result);

		return result;
	}

	public final void set(boolean value) {
		this.value = value ? (byte) 1 : (byte) -1;
	}

	@Override
	public String toString() {
		return Objects.toString(this.value, "???");
	}

}
