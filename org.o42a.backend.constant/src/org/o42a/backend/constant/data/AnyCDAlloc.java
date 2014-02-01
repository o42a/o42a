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
package org.o42a.backend.constant.data;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.AnyCOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Ptr;
import org.o42a.util.string.ID;


public final class AnyCDAlloc extends CDAlloc<AnyOp> {

	public AnyCDAlloc(
			ConstBackend backend,
			Ptr<AnyOp> pointer,
			UnderAlloc<AnyOp> underAlloc) {
		super(backend, pointer, underAlloc);
	}

	@Override
	public AnyCOp op(ID id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);

		return new AnyCOp(
				new OpBE<AnyOp>(id, ccode) {
					@Override
					public void prepare() {
					}
					@Override
					protected AnyOp write() {
						return getUnderlyingPtr().op(
								getId(),
								part().underlying());
					}
				},
				allocClass.allocPlace(ccode.code()),
				getPointer());
	}

	@Override
	public AnyCDAlloc toAny() {
		return this;
	}

}
