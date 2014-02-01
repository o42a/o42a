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

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.codegen.data.AllocPlace.constantAllocPlace;
import static org.o42a.codegen.data.AllocPlace.defaultAllocPlace;
import static org.o42a.codegen.data.AllocPlace.staticAllocPlace;

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataPtrOp;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.Ptr;
import org.o42a.util.string.ID;


public abstract class DataPtrCOp<P extends DataPtrOp<P>>
		extends PtrCOp<P, Ptr<P>>
		implements DataPtrOp<P> {

	private final AllocPlace allocPlace;

	public DataPtrCOp(OpBE<P> backend, AllocPlace allocPlace) {
		super(backend);
		if (allocPlace != null) {
			this.allocPlace = allocPlace;
		} else {
			this.allocPlace = defaultAllocPlace();
		}
	}

	public DataPtrCOp(OpBE<P> backend, AllocPlace allocPlace, Ptr<P> constant) {
		super(backend, constant);
		if (constant != null) {
			if (constant.isPtrToConstant()) {
				this.allocPlace = constantAllocPlace();
			} else {
				this.allocPlace = staticAllocPlace();
			}
		} else {
			if (allocPlace != null) {
				this.allocPlace = allocPlace;
			} else {
				this.allocPlace = defaultAllocPlace();
			}
		}
	}

	@Override
	public final AllocPlace getAllocPlace() {
		return this.allocPlace;
	}

	@Override
	public AnyCOp toAny(ID id, Code code) {

		final ID resultId = code.getOpNames().castId(id, ANY_ID, this);

		return new AnyCOp(
				new OpBE<AnyOp>(resultId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected AnyOp write() {
						return backend().underlying().toAny(
								getId(),
								part().underlying());
					}
				},
				getAllocPlace());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final P offset(ID id, Code code, IntOp<?> index) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().indexId(id, this, index);
		final IntCOp<?, ?> idx = (IntCOp<?, ?>) index;

		if (idx.isConstant() && idx.getConstant().intValue() == 0) {
			if (part() == ccode.nextPart()) {
				return (P) this;
			}
			return create(
					new AliasBE<>(resultId, ccode, backend()),
					getConstant());
		}

		return create(
				new OpBE<P>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
						use(idx);
					}
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

}
