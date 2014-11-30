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

import static org.o42a.core.ir.field.inst.InstFldKind.INST_RESUME_FROM;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import java.util.function.Function;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.dep.DepOp;
import org.o42a.core.ir.field.inst.InstFldKind;
import org.o42a.core.ir.field.inst.InstFldOp;
import org.o42a.core.ir.field.inst.ResumeFromOp;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.impl.ApproximateObjOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.string.ID;


public abstract class ObjectOp extends DefiniteIROp<ObjectIROp>
		implements HostOp {

	public static final ID ANCESTOR_ID = ID.rawId("ancestor");
	public static final ID NEW_OBJECT_ID = ID.rawId("new_object");
	protected static final ID CAST_ID = ID.id("cast");
	protected static final ID TARGET_ID = ID.id("target");
	protected static final ID FIELD_HOST_ID = ID.id("field_host");
	protected static final ID DEP_HOST_ID = ID.id("dep_host");
	protected static final ID KEEPER_HOST_ID = ID.id("keeper_host");

	public static ObjectOp approximateObject(
			BuilderCode code,
			OpMeans<DataOp> ptr,
			Obj wellKnownType) {
		return approximateObject(
				code.getBuilder(),
				code.code(),
				ptr,
				wellKnownType);
	}

	public static ObjectOp approximateObject(
			CodeBuilder builder,
			Code code,
			OpMeans<DataOp> ptr,
			Obj wellKnownType) {

		final ObjectIR ir = wellKnownType.ir(builder.getGenerator());

		if (ir.isExact()) {
			return ir.exactOp(builder, code);
		}

		return approximateObject(
				builder,
				code.means(c -> ptr.op().to(null, c, ir.getType())),
				wellKnownType);
	}

	public static ObjectOp approximateObject(
			CodeBuilder builder,
			OpMeans<ObjectIROp> ptr,
			Obj wellKnownType) {
		return new ApproximateObjOp(builder, ptr, wellKnownType);
	}

	public static ObjectOp objectAncestor(
			CodeDirs dirs,
			Obj object,
			ObjHolder holder) {
		return objectAncestor(dirs, dirs.getBuilder().host(), object, holder);
	}

	public static ObjectOp objectAncestor(
			CodeDirs dirs,
			HostOp host,
			Obj object,
			ObjHolder holder) {

		final CodeDirs subDirs = dirs.begin(ANCESTOR_ID, "Ancestor");
		final RefOp ancestor = object.type().getAncestor().op(host);

		final ObjectOp result = ancestor.path().materialize(subDirs, holder);

		subDirs.done();

		dirs.code().dumpName("Ancestor: ", result);

		return result;
	}

	private final OpPresets presets;
	private final ObjectPrecision precision;
	private final boolean stackAllocated;

	protected ObjectOp(
			CodeBuilder builder,
			OpMeans<ObjectIROp> ptr,
			ObjectPrecision precision) {
		super(builder, ptr);
		this.presets = builder.getDefaultPresets();
		this.precision = precision;
		this.stackAllocated = false;
	}

	protected ObjectOp(ObjectOp proto, OpPresets presets) {
		super(proto.getBuilder(), proto.means());
		this.presets = presets;
		this.precision = proto.precision;
		this.stackAllocated = proto.stackAllocated;
	}

	protected ObjectOp(ObjectOp proto, boolean stackAllocated) {
		super(proto.getBuilder(), proto.means());
		this.presets = proto.presets;
		this.precision = proto.precision;
		this.stackAllocated = stackAllocated;
	}

	@Override
	public final OpPresets getPresets() {
		return this.presets;
	}

	@Override
	public abstract ObjectOp setPresets(OpPresets presets);

	public final boolean isStackAllocated() {
		return this.stackAllocated;
	}

	public abstract ObjectOp setStackAllocated(boolean stackAllocated);

	public abstract Obj getWellKnownType();

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	public VmtIRChain.Op vmtc(Code code) {
		return objectData(code).ptr().vmtc(code).load(null, code);
	}

	public ObjectValueFn valueFunc(Code code) {
		return objectData(code).ptr().valueFunc(code).load(null, code);
	}

	public abstract ObjOp cast(ID id, CodeDirs dirs, Obj ascendant);

	public final ObjOp castToWellKnown(ID id, CodeDirs dirs) {
		return cast(id, dirs, getWellKnownType());
	}

	@Override
	public abstract ValueOp<?> value();

	public abstract ObjectOp phi(Code code, OpMeans<DataOp> ptr);

	public final ObjectDataOp objectData(Code code) {
		return ptr().objectData(code).op(getBuilder());
	}

	public final ResumeFromOp resumeFrom(CodeDirs dirs) {
		return (ResumeFromOp) instField(dirs, INST_RESUME_FROM);
	}

	public final ObjectIRLock.Op lock(CodeDirs dirs) {

		final Block code = dirs.code();

		return ptr().objectData(code).lock(code);
	}

	public abstract InstFldOp<?, ?> instField(CodeDirs dirs, InstFldKind kind);

	public abstract DepOp dep(CodeDirs dirs, Dep dep);

	public abstract LocalIROp local(CodeDirs dirs, MemberKey memberKey);

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
				approximateObject(
						valDirs,
						ptr,
						linkType.interfaceRef(typeParameters).getType()));

		valDirs.done();

		return result;
	}

	@Override
	public TargetStoreOp allocateStore(ID id, Code code) {
		return new ObjectStoreOp(id, code, this);
	}

	@Override
	public TargetStoreOp localStore(
			ID id,
			Function<CodeDirs, LocalIROp> getLocal) {
		return new ObjectStoreOp(id, getLocal, this);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(');

		switch (getPrecision()) {
		case EXACT_OBJECT:
			break;
		case COMPATIBLE_OBJECT:
			out.append("(*) ");
			break;
		case APPROXIMATE_OBJECT:
			out.append("(?) ");
			break;
		}

		out.append(getWellKnownType());
		out.append(") ");
		out.append(ptr());

		return out.toString();
	}

	private static final class ObjectStoreOp extends AbstractObjectStoreOp {

		private final ObjectOp object;

		ObjectStoreOp(ID id, Code code, ObjectOp object) {
			super(id, code);
			this.object = object;
		}

		ObjectStoreOp(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal,
				ObjectOp object) {
			super(id, getLocal);
			this.object = object;
		}

		@Override
		public Obj getWellKnownType() {
			return this.object.getWellKnownType();
		}

		@Override
		protected ObjectOp object(CodeDirs dirs, Allocator allocator) {
			return this.object;
		}

	}

}
