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
import org.o42a.backend.constant.code.op.IntCOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.IntRecOp;
import org.o42a.codegen.code.op.RMWKind;
import org.o42a.codegen.data.Ptr;
import org.o42a.util.string.ID;


public abstract class IntRecCOp<
		R extends IntRecOp<R, O>,
		O extends IntOp<O>,
		T extends Number>
				extends AtomicRecCOp<R, O, T>
				implements IntRecOp<R, O> {

	public IntRecCOp(OpBE<R> backend, RecStore store) {
		super(backend, store);
	}

	public IntRecCOp(OpBE<R> backend, RecStore store, Ptr<R> constant) {
		super(backend, store, constant);
	}

	@Override
	public O atomicRMW(
			final ID id,
			final Code code,
			final RMWKind kind,
			final O operand) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, kind.getId(), this, operand);
		@SuppressWarnings("unchecked")
		final COp<O, T> cOperand = (IntCOp<O, T>) operand;

		return loaded(
				new OpBE<O>(resultId, ccode) {
					@Override
					public void prepare() {
						alwaysEmit();
						use(backend());
						use(cOperand);
					}
					@Override
					protected O write() {
						return backend().underlying().atomicRMW(
								getId(),
								part().underlying(),
								kind,
								cOperand.backend().underlying());
					}
				},
				null);
	}

}
