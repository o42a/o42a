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

import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.StructOp;


public abstract class AbstractCOp<O extends Op, T> implements COp<O, T> {

	private final CCode<?> code;
	private final O underlying;
	private final T constant;

	public AbstractCOp(CCode<?> code, O underlying, T constant) {
		this.code = code;
		this.underlying = underlying;
		this.constant = constant;
	}

	public final ConstBackend getBackend() {
		return getCode().getBackend();
	}

	@Override
	public final CCode<?> getCode() {
		return this.code;
	}

	@Override
	public final O getUnderlying() {
		return this.underlying;
	}

	@Override
	public final CodeId getId() {
		return getUnderlying().getId();
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
	public void allocated(AllocationCode code, StructOp<?> enclosing) {
		getUnderlying().allocated(
				underlying(code),
				underlying(enclosing));
	}

	@Override
	public String toString() {
		if (this.underlying == null) {
			return super.toString();
		}
		return this.underlying.toString();
	}

}
