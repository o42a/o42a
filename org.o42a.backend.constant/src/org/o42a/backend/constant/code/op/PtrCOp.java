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
import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.DEFAULT_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.STATIC_ALLOC_CLASS;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.ReturnBE;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AbstractPtr;
import org.o42a.codegen.data.AllocClass;


public abstract class PtrCOp<P extends PtrOp<P>, PT extends AbstractPtr>
		extends AbstractCOp<P, PT>
		implements PtrOp<P> {

	private final AllocClass allocClass;

	public PtrCOp(OpBE<P> backend, AllocClass allocClass) {
		super(backend);
		if (allocClass != null) {
			this.allocClass = allocClass;
		} else {
			this.allocClass = DEFAULT_ALLOC_CLASS;
		}
	}

	public PtrCOp(OpBE<P> backend, AllocClass allocClass, PT constant) {
		super(backend, constant);
		if (constant != null) {
			if (constant.isPtrToConstant()) {
				this.allocClass = CONSTANT_ALLOC_CLASS;
			} else {
				this.allocClass = STATIC_ALLOC_CLASS;
			}
		} else {
			if (allocClass != null) {
				this.allocClass = allocClass;
			} else {
				this.allocClass = DEFAULT_ALLOC_CLASS;
			}
		}
	}

	@Override
	public final AllocClass getAllocClass() {
		return this.allocClass;
	}

	@Override
	public final void returnValue(Block code) {
		new ReturnBE(cast(code).nextPart()) {
			@Override
			protected void emit() {
				backend().underlying().returnValue(part().underlying());
			}
		};
	}

	@Override
	public final BoolCOp isNull(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final CodeId resultId = code.getOpNames().unaryId(id, "is_null", this);

		if (isConstant()) {
			return new BoolCOp(resultId, ccode, getConstant().isNull());
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().isNull(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final BoolCOp eq(CodeId id, Code code, P other) {

		final CCode<?> ccode = cast(code);
		final CodeId resultId =
				code.getOpNames().binaryId(id, "eq", this, other);
		final COp<P, ?> o = cast(other);

		if (isConstant() && o.isConstant()) {
			return new BoolCOp(
					resultId,
					ccode,
					getConstant().equals(o.getConstant()));
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().eq(
						getId(),
						part().underlying(),
						o.backend().underlying());
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public final P offset(CodeId id, Code code, IntOp<?> index) {

		final CCode<?> ccode = cast(code);
		final CodeId resultId = code.getOpNames().indexId(id, this, index);
		final IntCOp<?, ?> idx = (IntCOp<?, ?>) index;

		if (idx.isConstant() && idx.getConstant().intValue() == 0) {
			if (part() == ccode.nextPart()) {
				return (P) this;
			}
			return create(
					new AliasBE<P>(resultId, ccode, backend()),
					getConstant());
		}

		return create(
				new OpBE<P>(resultId, ccode) {
					@Override
					protected P write() {
						return backend().underlying().offset(
								getId(),
								part().underlying(),
								idx.backend().underlying());
					}
				},
				null);
	}

	@Override
	public AnyCOp toAny(CodeId id, Code code) {

		final CodeId resultId = code.getOpNames().castId(id, "any", this);

		return new AnyCOp(
				new OpBE<AnyOp>(resultId, cast(code)) {
					@Override
					protected AnyOp write() {
						return backend().underlying().toAny(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

}
