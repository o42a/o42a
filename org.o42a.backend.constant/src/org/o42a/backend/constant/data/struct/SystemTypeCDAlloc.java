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

import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.UnderAlloc;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SystemType;
import org.o42a.util.string.ID;


public class SystemTypeCDAlloc extends CDAlloc<SystemOp> {

	private static final SystemUnderAlloc SYSTEM_UNDER_ALLOC =
			new SystemUnderAlloc();

	private final CSystemType underlyingType;

	public SystemTypeCDAlloc(ConstBackend backend, SystemType systemType) {
		super(
				backend,
				systemType.getPointer(),
				SYSTEM_UNDER_ALLOC);
		this.underlyingType = new CSystemType(backend, systemType);
	}

	public final CSystemType getUnderlyingType() {
		return this.underlyingType;
	}

	@Override
	public SystemOp op(ID id, AllocClass allocClass, CodeWriter writer) {
		throw new UnsupportedOperationException();
	}

	private static final class SystemUnderAlloc extends UnderAlloc<SystemOp> {

		private SystemUnderAlloc() {
		}

		@Override
		public Ptr<SystemOp> allocateUnderlying(CDAlloc<SystemOp> alloc) {

			final SystemTypeCDAlloc typeAlloc = (SystemTypeCDAlloc) alloc;
			final Ptr<SystemOp> pointer =
					typeAlloc.getUnderlyingType().getPointer();

			return pointer;
		}

		@Override
		public String toString() {
			return "DefaultUnderAlloc";
		}

	}

}
