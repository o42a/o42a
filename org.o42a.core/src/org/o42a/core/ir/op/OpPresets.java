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
package org.o42a.core.ir.op;


public final class OpPresets {

	public static final OpPresets DEFAULT_OP_PRESETS = new OpPresets(false);

	private static final OpPresets STACK_ALLOCATION_ALLOWED =
			new OpPresets(true);

	private boolean stackAllocationAllowed;

	private OpPresets(boolean stackAllocationAllowed) {
		this.stackAllocationAllowed = stackAllocationAllowed;
	}

	public boolean isStackAllocationAllowed() {
		return this.stackAllocationAllowed;
	}

	public OpPresets setStackAllocationAllowed(boolean stackAllocationAllowed) {
		if (stackAllocationAllowed) {
			return STACK_ALLOCATION_ALLOWED;
		}
		return DEFAULT_OP_PRESETS;
	}

	public final boolean is(OpPresets other) {
		return this == other;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("OpPresets[stackAllocationAllowed=");
		out.append(this.stackAllocationAllowed);
		out.append("]");

		return out.toString();
	}

}
