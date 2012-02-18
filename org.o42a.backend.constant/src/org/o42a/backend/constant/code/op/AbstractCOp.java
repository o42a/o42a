/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.Op;


public abstract class AbstractCOp<U extends Op, T> implements COp<U, T> {

	private final OpBE<U> backend;
	private final T constant;

	public AbstractCOp(OpBE<U> backend) {
		this(backend, null);
	}

	public AbstractCOp(OpBE<U> backend, T constant) {
		this.backend = backend;
		this.constant = constant;
		this.backend.init(this);
	}

	public final ConstBackend getBackend() {
		return part().code().getBackend();
	}

	@Override
	public final CCodePart<?> part() {
		return backend().part();
	}

	@Override
	public final OpBE<U> backend() {
		return this.backend;
	}

	@Override
	public final CodeId getId() {
		return backend().getId();
	}

	@Override
	public final boolean isConstant() {
		return getConstant() != null;
	}

	@Override
	public final T getConstant() {
		return this.constant;
	}

	@Override
	public final U create(OpBE<U> backend) {
		return create(backend, null);
	}

	@Override
	public String toString() {
		if (this.backend == null) {
			return super.toString();
		}
		return this.backend.toString();
	}

}
