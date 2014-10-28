/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.object;


public enum ObjectValueIRKind {

	FALSE_VALUE_IR("o42a_obj_value_false"),
	UNKNOWN_VALUE_IR("o42a_obj_value_unknown"),
	VOID_VALUE_IR("o42a_obj_value_void"),
	EAGER_VALUE_IR("o42a_obj_value_eager"),
	STUB_VALUE_IR("o42a_obj_value_stub"),
	VALUE_IR(null);

	private final String functionName;

	ObjectValueIRKind(String functionName) {
		this.functionName = functionName;
	}

	public final String getFunctionName() {
		return this.functionName;
	}

	public final boolean isStub() {
		return this == STUB_VALUE_IR;
	}

	public final boolean isPredefined() {
		return this != VALUE_IR;
	}

}
