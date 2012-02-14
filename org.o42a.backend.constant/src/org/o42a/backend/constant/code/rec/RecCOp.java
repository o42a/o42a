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
package org.o42a.backend.constant.code.rec;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.data.rec.RecCDAlloc;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Ptr;


public abstract class RecCOp<
		R extends RecOp<R, O>,
		O extends Op,
		T> extends PtrCOp<R, Ptr<R>> implements RecOp<R, O> {

	public RecCOp(OpBE<R> backend, AllocClass allocClass) {
		super(backend, allocClass);
	}

	public RecCOp(OpBE<R> backend, AllocClass allocClass, Ptr<R> constant) {
		super(backend, allocClass, constant);
	}

	public final T getConstantValue() {
		if (!isConstant()) {
			return null;
		}

		@SuppressWarnings("unchecked")
		final RecCDAlloc<?, ?, T> alloc =
				(RecCDAlloc<?, ?, T>) getConstant().getAllocation();

		if (!alloc.isConstant()) {
			return null;
		}

		return alloc.getValue();
	}

	@Override
	public final O load(CodeId id, Code code) {

		final CodeId derefId = code.getOpNames().derefId(id, this);
		final CCode<?> ccode = cast(code);
		final T constant = getConstantValue();

		if (constant != null) {
			return loaded(
					new ConstBE<O, T>(derefId, ccode, constant) {
						@Override
						protected O write() {
							return underlyingConstant(part(), constant());
						}
					},
					constant);
		}

		return loaded(
				new OpBE<O>(derefId, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected O write() {
						return backend().underlying().load(
								getId(),
								part().underlying());
					}
				},
				null);
	}

	@Override
	public final void store(final Code code, final O value) {

		final COp<O, ?> cValue = cast(value);

		new InstrBE(cast(code)) {
			@Override
			public void prepare() {
				alwaysEmit();
				use(backend());
				use(cValue);
			}
			@Override
			public String toString() {
				return RecCOp.this + " = " + value;
			}
			@Override
			protected void emit() {
				backend().underlying().store(
						part().underlying(),
						cValue.backend().underlying());
			}
		};
	}

	protected abstract O loaded(OpBE<O> backend, T constant);

	protected abstract O underlyingConstant(CCodePart<?> part, T constant);

}
