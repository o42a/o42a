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

	STRUCT(0),
	REL_PTR(0x02),
	PTR(0x12),
	DATA_PTR(0x22),
	CODE_PTR(0x32),
	INT32(0x11 | (4 << 8)),
	INT64(0x11 | (8 << 8)),
	FP64(0x21 | (8 << 8));

	private final int code;

	DataType(int code) {
		this.code = code;
	}

	public final int getCode() {
		return this.code;
	}

}
