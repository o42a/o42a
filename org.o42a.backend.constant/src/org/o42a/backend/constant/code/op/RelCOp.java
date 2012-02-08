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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.RelPtr;


public final class RelCOp extends AbstractCOp<RelOp, RelPtr> implements RelOp {

	public RelCOp(OpBE<RelOp> backend) {
		super(backend);
	}

	public RelCOp(OpBE<RelOp> backend, RelPtr constant) {
		super(backend, constant);
	}

	@Override
	public final AnyCOp offset(
			final CodeId id,
			final Code code,
			final PtrOp<?> from) {
		return new AnyCOp(
				new OpBE<AnyOp>(id, cast(code)) {
					@Override
					protected AnyOp write() {
						return backend().underlying().offset(
								getId(),
								code().getUnderlying(),
								cast(from).backend().underlying());
					}
				},
				from.getAllocClass()/* Points to the same allocation unit. */);
	}

	@Override
	public RelCOp create(OpBE<RelOp> backend, RelPtr constant) {
		return new RelCOp(backend, constant);
	}

	@Override
	public final Int32cOp toInt32(CodeId id, Code code) {
		return new Int32cOp(new OpBE<Int32op>(id, cast(code)) {
			@Override
			protected Int32op write() {
				return backend().underlying().toInt32(
						getId(),
						code().getUnderlying());
			}
		});
	}

}
