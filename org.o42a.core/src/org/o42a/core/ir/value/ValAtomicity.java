/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.value;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;

import org.o42a.codegen.code.op.Atomicity;


public enum ValAtomicity {

	NOT_ATOMIC_VAL(NOT_ATOMIC),
	ATOMIC_VAL(ATOMIC),
	VAR_ASSIGNMENT(ATOMIC);

	private final Atomicity atomicity;

	private ValAtomicity(Atomicity atomicity) {
		this.atomicity = atomicity;
	}

	public final boolean isAtomic() {
		return this.atomicity.isAtomic();
	}

	public final boolean isVarAssignment() {
		return this == VAR_ASSIGNMENT;
	}

	public final Atomicity toAtomicity() {
		return this.atomicity;
	}

}
