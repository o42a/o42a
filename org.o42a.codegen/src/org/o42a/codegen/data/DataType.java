/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.codegen.data;


public enum DataType {

	VOID(0x80000000, "void"),
	STRUCT(0, "struct"),
	REL_PTR(0x02, "rptr"),
	PTR(0x12, "void*"),
	DATA_PTR(0x22, "struct*"),
	CODE_PTR(0x32, "(void*)()"),
	FUNC_PTR(0x42, "(func*)()"),
	BOOL(0x01, "bool"),
	INT32(0x11 | (2 << 8), "int32"),
	INT64(0x11 | (3 << 8), "int64"),
	FP64(0x21 | (3 << 8), "fp64");

	private final int code;
	private final String name;

	DataType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public final int getCode() {
		return this.code;
	}

	public final String getName() {
		return this.name;
	}

}
