/*
    Compiler Code Generator
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
package org.o42a.codegen.code.backend;

import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.string.ID;


public interface AllocationWriter extends CodeWriter {

	AnyRecOp allocatePtr(ID id);

	<S extends StructOp<S>> StructRecOp<S> allocatePtr(
			ID id,
			DataAllocation<S> allocation);

	<S extends StructOp<S>> S allocateStruct(
			ID id,
			DataAllocation<S> allocation);

	void dispose(CodeWriter writer);

}
