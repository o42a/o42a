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
package org.o42a.backend.constant.data.struct;

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.struct.StructStore.allocStructStore;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.UnderAlloc;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


public final class ExternCDAlloc<S extends StructOp<S>> extends CDAlloc<S> {

	private final Type<S> type;

	public ExternCDAlloc(
			final ConstBackend backend,
			final Ptr<S> pointer,
			final Type<S> type,
			final GlobalAttributes attributes) {
		super(backend, pointer, new UnderAlloc<S>() {
			@Override
			public Ptr<S> allocateUnderlying(CDAlloc<S> alloc) {

				final ConstBackend backend = alloc.getBackend();

				return backend.getUnderlyingGenerator()
						.getGlobals()
						.externalGlobal()
						.set(attributes)
						.link(
								pointer.getId().toString(),
								backend.underlying(type));
			}
		});
		this.type = type;
	}

	@Override
	public S op(ID id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);

		return this.type.op(new CStruct<>(
				new OpBE<S>(id, ccode) {
					@Override
					public void prepare() {
					}
					@Override
					protected S write() {
						return getUnderlyingPtr().op(
								getId(),
								part().underlying());
					}
				},
				allocStructStore(allocClass.allocPlace(ccode.code())),
				this.type,
				getPointer()));
	}

}
