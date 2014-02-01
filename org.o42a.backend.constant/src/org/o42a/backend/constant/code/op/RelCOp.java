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
import org.o42a.backend.constant.data.AnyCDAlloc;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.RelPtr;
import org.o42a.util.string.ID;


public final class RelCOp extends AbstractCOp<RelOp, RelPtr> implements RelOp {

	public RelCOp(OpBE<RelOp> backend) {
		super(backend);
	}

	public RelCOp(OpBE<RelOp> backend, RelPtr constant) {
		super(backend, constant);
	}

	@Override
	public final AnyCOp offset(
			final ID id,
			final Code code,
			final DataPtrOp<?> from) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().offsetId(id, from, this);

		if (isConstant() && from.equals(getConstant().getRelativeTo())) {

			final Ptr<AnyOp> target = getConstant().getPointer().toAny();

			return new AnyCOp(
					new ConstBE<AnyOp, Ptr<AnyOp>>(resultId, ccode, target) {
						@Override
						protected AnyOp write() {

							final AnyCDAlloc alloc =
									(AnyCDAlloc) constant().getAllocation();

							return alloc.getUnderlyingPtr().op(
									getId(),
									part().underlying());
						}
					},
					null,
					target);
		}

		return new AnyCOp(
				new OpBE<AnyOp>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected AnyOp write() {
						return backend().underlying().offset(
								getId(),
								part().underlying(),
								cast(from).backend().underlying());
					}
				},
				from.getAllocPlace()/* Points to the same allocation unit. */);
	}

	@Override
	public RelCOp create(OpBE<RelOp> backend, RelPtr constant) {
		return new RelCOp(backend, constant);
	}

	@Override
	public final Int32cOp toInt32(ID id, Code code) {

		final ID resultId = code.getOpNames().castId(id, INT32_ID, this);

		return new Int32cOp(new OpBE<Int32op>(resultId, cast(code)) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected Int32op write() {
				return backend().underlying().toInt32(
						getId(),
						part().underlying());
			}
		});
	}

}
