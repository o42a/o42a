/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;

import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


final class PredefCondBuilder extends AbstractObjectCondBuilder {

	private final CompilerContext context;
	private final ID id;
	private final ValueType<?> valueType;

	PredefCondBuilder(CompilerContext context, ID id, ValueType<?> valueType) {
		this.context = context;
		this.id = id;
		this.valueType = valueType;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	@Override
	protected ValueType<?> getValueType() {
		return this.valueType;
	}

	@Override
	protected ObjBuilder createBuilder(
			Function<ObjectCondFunc> function,
			CodePos failureDir) {

		final Obj typeObject = typeObject();

		return new ObjBuilder(
				function,
				failureDir,
				typeObject.ir(function.getGenerator()),
				typeObject,
				DERIVED);
	}

	private Obj typeObject() {
		return this.valueType.typeObject(this.context.getIntrinsics());
	}

}
