/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.ReturnBE;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public final class BoolCOp extends COp<BoolOp, Boolean> implements BoolOp {

	public BoolCOp(OpBE<BoolOp> backend, Boolean constant) {
		super(backend, constant);
	}

	public BoolCOp(OpBE<BoolOp> backend) {
		super(backend);
	}

	public BoolCOp(ID id, CCode<?> code, boolean constant) {
		this(new BoolConstBE(id, code, constant), constant);
	}

	@Override
	public <O extends Op> O select(
			final ID id,
			final Code code,
			final O trueValue,
			final O falseValue) {

		final CCode<?> ccode = cast(code);
		final ID selectId = id != null ? id : getId().sub("select");

		if (isConstant()) {
			if (getConstant()) {
				return create(selectId, ccode, trueValue);
			}
			return create(selectId, ccode, falseValue);
		}

		final COp<O, ?> trueVal = cast(trueValue);
		final COp<O, ?> falseVal = cast(falseValue);

		return trueVal.create(new OpBE<O>(selectId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(trueVal);
				use(falseVal);
			}
			@Override
			protected O write() {
				return backend().underlying().select(
						getId(),
						part().underlying(),
						trueVal.backend().underlying(),
						falseVal.backend().underlying());
			}
		});
	}

	@Override
	public void returnValue(Block code, boolean dispose) {
		new ReturnBE(cast(code).nextPart(), dispose) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected void emit() {
				backend().underlying().returnValue(part().underlying());
			}
		};
	}

	@Override
	public BoolOp create(OpBE<BoolOp> backend, Boolean constant) {
		return new BoolCOp(backend, constant);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <O extends Op> O create(ID id, CCode<?> code, O value) {

		final COp val = cast(value);

		if (val.part() == code.nextPart()) {
			return value;
		}

		return (O) val.create(
				new AliasBE(id, code, val.backend()),
				val.getConstant());
	}

	private static final class BoolConstBE extends ConstBE<BoolOp, Boolean> {

		BoolConstBE(ID id, CCode<?> code, boolean constant) {
			super(id, code, constant);
		}

		@Override
		protected BoolOp write() {
			return part().underlying().bool(constant());
		}

	}

}
