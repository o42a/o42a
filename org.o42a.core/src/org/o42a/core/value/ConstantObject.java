/*
    Compiler Core
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
package org.o42a.core.value;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


final class ConstantObject<T> extends PlainObject {

	private final Value<T> value;

	ConstantObject(
			LocationInfo location,
			Distributor enclosing,
			ValueType<T> valueType,
			T value) {
		super(location, enclosing);
		setValueType(valueType);
		this.value = valueType.constantValue(value);
	}

	public final Value<T> getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return this.value.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new ConstantValueDef<T>(this).toDefinitions();
	}

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	private static final class ValueIR
			extends ObjectValueIR
			implements ObjValOp {

		ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		public ObjValOp op(CodeBuilder builder, Code code) {
			return this;
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return value().op(dirs.code());
		}

		@Override
		protected ValOp writeProposition(
				ValDirs dirs,
				ObjOp host,
				ObjectOp body) {
			return value().op(dirs.code());
		}

		@Override
		protected ValOp buildProposition(
				ValDirs dirs,
				ObjOp host,
				Definitions definitions) {
			return value().op(dirs.code());
		}

		private final Value<?> value() {

			final ConstantObject<?> object =
				(ConstantObject<?>) getObjectIR().getObject();

			return object.getValue();
		}

	}

}
