/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.value.impl;

import org.o42a.codegen.data.Ptr;
import org.o42a.core.Distributor;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.*;


public final class Constant<T> extends ObjectConstructor {

	private final SingleValueType<T> valueType;
	private final T constant;

	public Constant(
			LocationInfo location,
			Distributor distributor,
			SingleValueType<T> valueType,
			T constant) {
		super(location, distributor);
		this.valueType = valueType;
		this.constant = constant;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return this.valueType.typeRef(location, getScope());
	}

	public final SingleValueType<T> getValueType() {
		return this.valueType;
	}

	public final T getConstant() {
		return this.constant;
	}

	@Override
	public ValueAdapter valueAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct,
			boolean adapt) {

		final SingleValueStruct<T> valueStruct = this.valueType.struct();

		if (adapt
				&& expectedStruct != null
				&& !expectedStruct.assignableFrom(valueStruct)) {
			return super.valueAdapter(ref, expectedStruct, adapt);
		}

		return new ConstantValueAdapter<T>(
				ref,
				getValueType(),
				this.constant);
	}

	@Override
	public FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return new ConstantFieldDefinition(path, distributor, this);
	}

	@Override
	public Constant<T> reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new Constant<T>(
				this,
				reproducer.distribute(),
				getValueType(),
				getConstant());
	}

	@Override
	public PathOp op(PathOp host) {
		return new Op<T>(host, this);
	}

	@Override
	public String toString() {
		if (this.constant == null) {
			return super.toString();
		}
		return this.valueType.struct().valueString(this.constant);
	}

	@Override
	protected Obj createObject() {
		return new ConstantObject<T>(
				this,
				distribute(),
				getValueType(),
				getConstant());
	}

	private static final class Op<T> extends PathOp {

		private final Constant<T> constant;

		Op(PathOp start, Constant<T> constant) {
			super(start);
			this.constant = constant;
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {

			final ValueStructIR<?, T> valueStructIR =
					this.constant.getValueType().struct().ir(getGenerator());
			final Ptr<ValType.Op> ptr =
					valueStructIR.valPtr(this.constant.getConstant());
			final ValType.Op op =
					ptr.op(ptr.getId(), dirs.code());

			return op.op(
					dirs.getBuilder(),
					valueStructIR.val(this.constant.getConstant()));
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final ObjectIR ir =
					this.constant.getConstructed().ir(getGenerator());

			return ir.op(getBuilder(), dirs.code());
		}

	}

}
