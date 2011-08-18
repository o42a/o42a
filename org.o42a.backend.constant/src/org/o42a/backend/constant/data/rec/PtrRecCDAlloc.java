/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.backend.constant.data.rec;

import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.RecCDAlloc;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.PtrRec;


public abstract class PtrRecCDAlloc<R extends PtrRec<P>, P extends PtrOp<P>>
		extends RecCDAlloc<R, P, Ptr<P>> {

	public PtrRecCDAlloc(ContainerCDAlloc<?> enclosing, CDAlloc<P, R> type) {
		super(enclosing, type);
	}

	public PtrRecCDAlloc(ContainerCDAlloc<?> enclosing, Ptr<P> underlyingPtr) {
		super(enclosing, underlyingPtr);
	}

}
