/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.Distributor;
import org.o42a.core.ir.object.AbstractObjectStoreOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.type.StaticsIR;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;
import org.o42a.util.string.ID;


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
	public boolean mayContainDeps() {
		return false;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return this.valueType.typeRef(location, getScope());
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref, ref);
	}

	public final SingleValueType<T> getValueType() {
		return this.valueType;
	}

	public final T getConstant() {
		return this.constant;
	}

	@Override
	public ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		if (request.getExpectedType().is(getValueType())) {
			return new ConstantValueAdapter<>(
					ref,
					getValueType(),
					this.constant);
		}
		return super.valueAdapter(ref, request);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ConstantFieldDefinition(ref, this);
	}

	@Override
	public Constant<T> reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new Constant<>(
				this,
				reproducer.distribute(),
				getValueType(),
				getConstant());
	}

	@Override
	public HostOp op(HostOp host) {
		return new ConstantOp<>(host, this);
	}

	@Override
	public String toString() {
		if (this.constant == null) {
			return super.toString();
		}
		return this.valueType.valueString(this.constant);
	}

	@Override
	protected Obj createObject() {
		return new ConstantObject<>(
				this,
				distribute(),
				getValueType(),
				getConstant());
	}

	private static final class ConstantOp<T>
			extends PathOp
			implements HostValueOp {

		private final Constant<T> constant;

		ConstantOp(HostOp host, Constant<T> constant) {
			super(host);
			this.constant = constant;
		}

		@Override
		public HostValueOp value() {
			return this;
		}

		@Override
		public HostTargetOp target() {
			return pathTargetOp();
		}

		@Override
		public void writeCond(CodeDirs dirs) {
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {

			final StaticsIR<T> staticsIR =
					this.constant.getValueType().ir(getGenerator()).staticsIR();
			final Ptr<ValType.Op> ptr =
					staticsIR.valPtr(this.constant.getConstant());
			final ValType.Op op =
					ptr.op(ptr.getId(), dirs.code());

			return op.op(
					dirs.getBuilder(),
					staticsIR.val(this.constant.getConstant()));
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException(
					"Can not assign to constant");
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {

			final ObjectIR ir =
					this.constant.getConstructed().ir(getGenerator());

			return ir.op(getBuilder(), dirs.code());
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {
			return new ConstantStoreOp(id, code, this.constant);
		}

	}

	private static final class ConstantStoreOp extends AbstractObjectStoreOp {

		private final Constant<?> constant;

		ConstantStoreOp(ID id, Code code, Constant<?> constant) {
			super(id, code);
			this.constant = constant;
		}

		@Override
		public Obj getWellKnownType() {
			return this.constant.getConstructed();
		}

		@Override
		protected ObjectOp object(CodeDirs dirs) {

			final ObjectIR ir =
					this.constant.getConstructed().ir(dirs.getGenerator());

			return ir.op(dirs.getBuilder(), dirs.code());
		}

	}

}
