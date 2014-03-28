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
package org.o42a.codegen.code.op;

import org.o42a.util.string.ID;


public interface Op {

	ID BOOL_ID = ID.rawId("bool");

	ID INT8_ID = ID.rawId("int8");
	ID INT16_ID = ID.rawId("int16");
	ID INT32_ID = ID.rawId("int32");
	ID INT64_ID = ID.rawId("int64");

	ID FP32_ID = ID.rawId("fp32");
	ID FP64_ID = ID.rawId("fp64");

	ID ANY_ID = ID.rawId("any");
	ID DATA_ID = ID.rawId("data");
	ID REL_ID = ID.rawId("rel");
	ID CODE_ID = ID.rawId("code");

	ID PHI_ID = ID.rawId("phi");

	ID EQ_ID = ID.rawId("eq");
	ID NE_ID = ID.rawId("ne");

	ID getId();

}
