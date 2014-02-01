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

import org.o42a.util.string.ID;


public enum RMWKind {

	XCHG(0),
	R_ADD_W(1),
	R_SUB_W(2),
	R_OR_W(3),
	R_AND_W(4),
	R_XOR_W(5),
	R_NAND_W(6);

	private final int code;
	private final ID id;

	RMWKind(int code) {
		this.code = code;
		this.id = ID.rawId(name().toLowerCase());
	}

	public final ID getId() {
		return this.id;
	}

	public final int code() {
		return this.code;
	}

}
