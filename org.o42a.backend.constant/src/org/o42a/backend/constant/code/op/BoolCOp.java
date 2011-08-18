/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Op;


public final class BoolCOp extends BoolOp implements COp<BoolOp> {

	private final CCode<?> code;
	private final BoolOp underlying;

	public BoolCOp(CCode<?> code, BoolOp underlying) {
		this.code = code;
		this.underlying = underlying;
	}

	@Override
	public final CCode<?> getCode() {
		return this.code;
	}

	@Override
	public final BoolOp getUnderlying() {
		return this.underlying;
	}

	@Override
	public CodeId getId() {
		return getUnderlying().getId();
	}

	@Override
	public <O extends Op> O select(
			CodeId id,
			Code code,
			O trueValue,
			O falseValue) {

		final COp<O> trueVal = cast(trueValue);
		final CCode<?> ccode = cast(code);
		final O underlying = getUnderlying().select(
				id,
				ccode.getUnderlying(),
				trueVal.getUnderlying(),
				underlying(falseValue));

		return trueVal.create(ccode, underlying);
	}

	@Override
	public void returnValue(Code code) {

		final CCode<?> ccode = cast(code);

		ccode.beforeReturn();
		getUnderlying().returnValue(ccode.getUnderlying());
	}

	@Override
	public BoolOp create(CCode<?> code, BoolOp underlying) {
		return new BoolCOp(code, underlying);
	}

	@Override
	public String toString() {
		if (this.underlying == null) {
			return super.toString();
		}
		return this.underlying.toString();
	}

}
