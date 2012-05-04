/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.codegen.code.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.AllocClass;


public interface PtrOp<P extends PtrOp<P>> extends Op {

	AllocClass getAllocClass();

	void allocated(AllocationCode code, StructOp<?> enclosing);

	BoolOp isNull(CodeId id, Code code);

	BoolOp eq(CodeId id, Code code, P other);

	P offset(CodeId id, Code code, IntOp<?> index);

	AnyOp toAny(CodeId id, Code code);

	void returnValue(Block code);

}
