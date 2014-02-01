/*
    Compiler Core
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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


final class PredefValueBuilder extends AbstractObjectValueBuilder {

	private final CompilerContext context;
	private final ID id;
	private final ValueType<?> valueType;
	private final boolean steteful;

	PredefValueBuilder(
			CompilerContext context,
			ID id,
			ValueType<?> valueType,
			boolean steteful) {
		this.context = context;
		this.id = id;
		this.valueType = valueType;
		this.steteful = steteful;
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
	protected boolean isStateful() {
		return this.steteful;
	}

	@Override
	protected ObjBuilder createBuilder(
			Function<ObjectValueFunc> function,
			CodePos failureDir) {

		final Obj typeObject = typeObject();

		return new ObjBuilder(
				function,
				failureDir,
				typeObject.ir(function.getGenerator()).getMainBodyIR(),
				typeObject,
				DERIVED);
	}

	@Override
	protected ObjectIRDataOp data(
			Code code,
			Function<ObjectValueFunc> function) {
		return function.arg(code, OBJECT_VALUE.data());
	}

	@Override
	protected void writeValue(
			DefDirs dirs,
			ObjOp host,
			ObjectIRDataOp data) {

		final Block code = dirs.code();
		final ObjectOp owner = dirs.getBuilder().host();

		data.claimFunc(code).load(null, code).call(dirs, owner);
		data.propositionFunc(code).load(null, code).call(dirs, owner);
	}

	private Obj typeObject() {
		return this.valueType.typeObject(this.context.getIntrinsics());
	}

}
