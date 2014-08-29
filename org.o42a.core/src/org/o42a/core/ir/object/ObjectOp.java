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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.op.CastObjectFunc.CAST_OBJECT;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.ir.object.dep.DepOp;
import org.o42a.core.ir.object.impl.AnonymousObjOp;
import org.o42a.core.ir.object.op.CastObjectFunc;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.type.ObjectIRDescOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.string.ID;


public abstract class ObjectOp extends DefiniteIROp implements TargetOp {

	protected static final ID CAST_ID = ID.id("cast");
	protected static final ID TARGET_ID = ID.id("target");
	protected static final ID FIELD_HOST_ID = ID.id("field_host");
	protected static final ID DEP_HOST_ID = ID.id("dep_host");
	protected static final ID KEEPER_HOST_ID = ID.id("keeper_host");

	public static ObjectOp anonymousObject(
			CodeBuilder builder,
			DataOp ptr,
			Obj wellKnownType) {
		return new AnonymousObjOp(builder, ptr, wellKnownType);
	}

	private final ObjectPrecision precision;
	private final ObjectDataOp objectData;

	protected ObjectOp(CodeBuilder builder, ObjectPrecision precision) {
		super(builder);
		this.precision = precision;
		this.objectData = null;
	}

	protected ObjectOp(ObjectDataOp objectType) {
		super(objectType.getBuilder());
		this.objectData = objectType;
		this.precision = objectType.getPrecision();
	}

	public abstract Obj getWellKnownType();

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	public void fillDeps(CodeDirs dirs, HostOp host, Obj sample) {
		for (DepIR dep : sample.ir(getGenerator()).existingDeps()) {
			fillDep(dirs, host, dep.getDep());
		}
	}

	public abstract ObjOp cast(ID id, CodeDirs dirs, Obj ascendant);

	@Override
	public abstract ValueOp value();

	@Override
	public HostTargetOp target() {
		return this;
	}

	@Override
	public final ObjectOp op(CodeDirs dirs) {
		return this;
	}

	public final ObjectIRDescOp declaredIn(Code code) {
		return body(code).declaredIn(code).load(null, code);
	}

	public final ObjectDataOp objectData(Code code) {
		if (this.objectData != null) {
			return this.objectData;
		}
		return body(code).loadObjectData(code).op(getBuilder(), getPrecision());
	}

	@Override
	public abstract FldOp<?> field(CodeDirs dirs, MemberKey memberKey);

	public abstract DepOp dep(CodeDirs dirs, Dep dep);

	@Override
	public final ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return holder.hold(dirs.code(), this);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {

		final TypeParameters<?> typeParameters =
				getWellKnownType().type().getParameters();
		final LinkValueType linkType =
				typeParameters.getValueType().toLinkType();

		assert linkType != null :
			"Not a link: " + this;

		final ValDirs valDirs =
				dirs.nested().value("link", linkType, TEMP_VAL_HOLDER);
		final ValOp value = value().writeValue(valDirs);
		final Block code = valDirs.code();

		final DataOp ptr =
				value.value(null, code)
				.toRec(null, code)
				.load(null, code)
				.toData(TARGET_ID, code);
		final ObjectOp result = holder.holdVolatile(
				code,
				anonymousObject(
						getBuilder(),
						ptr,
						linkType.interfaceRef(typeParameters).getType()));

		valDirs.done();

		return result;
	}

	@Override
	public TargetStoreOp allocateStore(ID id, Code code) {
		if (getPrecision().isExact()) {
			return new ExactObjectStoreOp(this);
		}
		return new ObjectStoreOp(id, code, this);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(');

		switch (getPrecision()) {
		case EXACT:
			break;
		case COMPATIBLE:
			out.append("(*) ");
			break;
		case DERIVED:
			out.append("(?) ");
			break;
		}

		out.append(getWellKnownType());
		out.append(") ");
		out.append(ptr());

		return out.toString();
	}

	protected ObjOp dynamicCast(ID id, CodeDirs dirs, Obj ascendant) {

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());
		final CodeDirs subDirs = dirs.begin(
				id != null ? id : CAST_ID,
				"Dynamic cast " + this + " to " + ascendantIR.getId());

		final Block code = subDirs.code();
		final ObjOp ascendantObj = ascendantIR.op(getBuilder(), code);
		final ObjectDataOp ascendantData = ascendantObj.objectData(code);

		final DataOp resultPtr =
				castFunc()
				.op(null, code)
				.cast(
						id != null ? id.detail("ptr") : null,
						code,
						this,
						ascendantData);

		resultPtr.isNull(null, code).go(code, subDirs.falseDir());

		final ObjOp result =
				resultPtr.to(id, code, ascendantIR.getBodyType())
				.op(getBuilder(), ascendantIR.getObject(), COMPATIBLE);

		subDirs.done();

		return result;
	}

	protected final ObjectDataOp cachedData() {
		return this.objectData;
	}

	private FuncPtr<CastObjectFunc> castFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_cast", CAST_OBJECT);
	}

	private final ObjectIRBodyOp body(Code code) {
		return ptr().toAny(null, code).to(
				null,
				code,
				getWellKnownType().ir(getGenerator()).getBodyType());
	}

	private final void fillDep(CodeDirs dirs, HostOp host, Dep dep) {

		final CodeDirs depDirs = dirs.begin(getId(), "Fill " + dep);

		dep(depDirs, dep).fill(depDirs, host);

		depDirs.done();
	}

	private static final class ObjectStoreOp extends AbstractObjectStoreOp {

		private final ObjectOp object;

		ObjectStoreOp(ID id, Code code, ObjectOp object) {
			super(id, code);
			this.object = object;
		}

		@Override
		public Obj getWellKnownType() {
			return this.object.getWellKnownType();
		}

		@Override
		protected ObjectOp object(CodeDirs dirs) {
			tempObjHolder(getAllocator())
			.holdVolatile(dirs.code(), this.object);
			return this.object;
		}

	}

	private static final class ExactObjectStoreOp implements TargetStoreOp {

		private final ObjectOp object;

		ExactObjectStoreOp(ObjectOp object) {
			this.object = object;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			return this.object;
		}

		@Override
		public String toString() {
			if (this.object == null) {
				return super.toString();
			}
			return this.object.toString();
		}

	}

}
