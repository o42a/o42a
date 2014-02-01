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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.ExternalTypeInfoOp.EXTERNAL_TYPE_INFO;

import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Type;


final class ExternalTypeInfo extends DebugTypeInfo {

	private final String name;
	private Ptr<AnyOp> pointer;

	ExternalTypeInfo(Type<?> type, String name, int code) {
		super(type, code);
		this.name = name;
	}

	@Override
	public Ptr<AnyOp> getPointer() {
		if (this.pointer != null) {
			return this.pointer;
		}
		return this.pointer =
				getType()
				.getGenerator()
				.externalGlobal()
				.setConstant()
				.link(this.name, EXTERNAL_TYPE_INFO)
				.toAny();
	}

}
