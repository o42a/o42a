/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code.op;

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public abstract class ConstBE<U extends Op, T> extends OpBE<U> {

	private final T constant;

	public ConstBE(ID id, CCode<?> code, T constant) {
		super(id, code);
		this.constant = constant;
	}

	@Override
	public final boolean isNoOp() {
		return true;
	}

	public final T constant() {
		return this.constant;
	}

	@Override
	public void prepare() {
	}

	@Override
	public String toString() {
		return this.constant.toString();
	}

}
