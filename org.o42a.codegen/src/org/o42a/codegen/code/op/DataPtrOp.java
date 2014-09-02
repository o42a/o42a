/*
    Compiler Code Generator
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
package org.o42a.codegen.code.op;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public interface DataPtrOp<P extends DataPtrOp<P>> extends PtrOp<P> {

	AllocPlace getAllocPlace();

	P offset(ID id, Code code, IntOp<?> index);

	default P offset(ID id, Code code, RelOp index) {
		return offset(id, code, index.toInt32(null, code));
	}

}
