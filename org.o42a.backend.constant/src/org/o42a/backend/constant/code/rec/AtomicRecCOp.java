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
package org.o42a.backend.constant.code.rec;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.COp;
import org.o42a.backend.constant.code.op.InstrBE;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AtomicRecOp;
import org.o42a.codegen.code.op.Atomicity;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.data.Ptr;
import org.o42a.util.string.ID;


public abstract class AtomicRecCOp<
		R extends AtomicRecOp<R, O>,
		O extends Op,
		T> extends RecCOp<R, O, T> implements AtomicRecOp<R, O> {

	public AtomicRecCOp(OpBE<R> backend, RecStore store) {
		super(backend, store);
	}

	public AtomicRecCOp(OpBE<R> backend, RecStore store, Ptr<R> constant) {
		super(backend, store, constant);
	}

	@Override
	public O testAndSet(ID id, Code code, O expected, O value) {

		final ID resultId =
				code.getOpNames().binaryId(id, TEST_AND_SET_ID, this, value);
		final CCode<?> ccode = cast(code);
		final COp<O, ?> cExpected = cast(expected);
		final COp<O, ?> cValue = cast(value);

		return loaded(
				new OpBE<O>(resultId, ccode) {
					@Override
					public void prepare() {
						alwaysEmit();
						store().store(
								this,
								AtomicRecCOp.this, cValue.backend());
						store().load(AtomicRecCOp.this, this);
						use(cValue);
					}
					@Override
					protected O write() {
						return backend().underlying().testAndSet(
								getId(),
								part().underlying(),
								cExpected.backend().underlying(),
								cValue.backend().underlying());
					}
				},
				null);
	}

	@Override
	protected O loadUnderlying(OpBE<O> be, Atomicity atomicity) {
		return backend().underlying().load(
				be.getId(),
				be.part().underlying(),
				atomicity);
	}

	@Override
	protected void storeUnderlying(
			InstrBE be,
			COp<O, ?> value,
			Atomicity atomicity) {
		backend().underlying().store(
				be.part().underlying(),
				value.backend().underlying(),
				atomicity);
	}

}
