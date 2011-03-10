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
package org.o42a.codegen.debug;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.CodePtr;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Ptr;


final class DbgFunc implements Content<DbgFuncType> {

	private final CodeId id;
	private final CodePtr<?> function;
	private Ptr<AnyOp> namePtr;

	DbgFunc(CodeId id, Signature<?> signature, CodePtr<?> function) {
		this.id = id;
		this.function = function;
	}

	public final CodeId getId() {
		return this.id;
	}

	@Override
	public void allocated(DbgFuncType instance) {
	}

	@Override
	public void fill(DbgFuncType instance) {

		final Generator generator = instance.getGenerator();
		final Debug debug = generator;

		if (this.namePtr != null) {
			instance.name().setValue(this.namePtr);
		} else {
			debug.setName(
					instance.name(),
					generator
					.id("DEBUG")
					.sub("FUNC_NAME")
					.sub(this.id),
					this.id.getId());
		}
		instance.function().setValue(this.function.toAny());
	}

	void setNamePtr(Ptr<AnyOp> namePtr) {
		this.namePtr = namePtr;
	}

}
