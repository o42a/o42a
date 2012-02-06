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

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.CBlock;
import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.StructOp;


public final class BoolCOp extends BoolOp implements COp<BoolOp, Boolean> {

	private final CCode<?> code;
	private final BoolOp underlying;
	private final Boolean constant;

	public BoolCOp(CCode<?> code, BoolOp underlying, Boolean constant) {
		this.code = code;
		this.underlying = underlying;
		this.constant = constant;
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
	public final boolean isConstant() {
		return getConstant() != null;
	}

	@Override
	public final Boolean getConstant() {
		return this.constant;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends Op> O select(
			CodeId id,
			Code code,
			O trueValue,
			O falseValue) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {
			if (getConstant()) {
				return create(id, ccode, trueValue);
			}
			return create(id, ccode, falseValue);
		}

		if (trueValue instanceof StructOp) {

			final CStruct<?> trueVal = cast((StructOp<?>) trueValue);
			final StructOp<?> underlying = getUnderlying().select(
					id,
					ccode.getUnderlying(),
					trueVal.getUnderlying(),
					underlying((StructOp<?>) falseValue));
			@SuppressWarnings("rawtypes")
			final CStruct res = trueVal;

			return (O) res.create(ccode, underlying, null);
		}

		final COp<O, ?> trueVal = cast(trueValue);
		final O underlying = getUnderlying().select(
				id,
				ccode.getUnderlying(),
				trueVal.getUnderlying(),
				underlying(falseValue));

		return trueVal.create(ccode, underlying, null);
	}

	@Override
	public void returnValue(Block code) {

		final CBlock<?> ccode = cast(code);

		ccode.beforeReturn();
		getUnderlying().returnValue(ccode.getUnderlying());
	}

	@Override
	public BoolOp create(CCode<?> code, BoolOp underlying, Boolean constant) {
		return new BoolCOp(code, underlying, constant);
	}

	@Override
	public String toString() {
		if (this.underlying == null) {
			return super.toString();
		}
		return this.underlying.toString();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <O extends Op> O create(
			CodeId id,
			CCode<?> code,
			O value) {
		if (value instanceof StructOp) {

			final StructOp<?> struct = (StructOp<?>) value;
			final CStruct<?> str = cast(struct);

			if (str.getCode() == code) {
				return (O) struct;
			}

			return (O) struct.getType().op(new CStruct(
					code,
					str.getUnderlying(),
					struct.getType(),
					str.getConstant()));
		}

		final COp val = cast(value);

		if (val.getCode() == code) {
			return value;
		}

		return (O) val.create(code, val.getUnderlying(), val.getConstant());
	}

}
