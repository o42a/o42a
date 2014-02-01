/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.codegen.data.backend;

import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.RelPtr;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


public interface DataAllocation<P extends PtrOp<P>> {

	<RR extends RecOp<RR, PP>, PP extends P> void write(
			DataWriter writer,
			DataAllocation<RR> destination);

	DataLayout getLayout();

	RelAllocation relativeTo(RelPtr pointer, DataAllocation<?> allocation);

	P op(ID id, AllocClass allocClass, CodeWriter writer);

	DataAllocation<AnyOp> toAny();

	DataAllocation<DataOp> toData();

}
