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

import static org.o42a.core.ir.field.Fld.FIELD_ID;
import static org.o42a.core.ir.field.dep.DepOp.DEP_ID;
import static org.o42a.core.ir.field.inst.InstFldKind.INST_LOCK;
import static org.o42a.core.ir.field.inst.InstFldKind.INST_RESUME_FROM;
import static org.o42a.core.ir.field.local.LocalIR.LOCAL_ID;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE_OBJECT;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT_OBJECT;
import static org.o42a.util.fn.Init.init;

import java.util.function.Function;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.dep.DepIR;
import org.o42a.core.ir.field.dep.DepOp;
import org.o42a.core.ir.field.inst.*;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.StateOp;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public final class ObjOp extends ObjectOp {

	private final ObjectIR objectIR;
	private final Obj ascendant;
	private final Init<ValueOp<ObjOp>> value = init(this::createValue);

	ObjOp(
			CodeBuilder builder,
			ObjectIR objectIR,
			OpMeans<ObjectIROp> ptr,
			Obj ascendant,
			ObjectPrecision precision) {
		super(
				builder,
				ptr,
				objectIR.isExact() && ascendant.is(objectIR.getObject())
				? EXACT_OBJECT : precision);
		assert ascendant != null :
			"Object ascendant not specified";
		this.objectIR = objectIR;
		this.ascendant = ascendant;
		assert validPrecision();
	}

	ObjOp(
			CodeBuilder builder,
			ObjectIR objectIR,
			OpMeans<ObjectIROp> ptr,
			ObjectPrecision precision) {
		super(builder, ptr, objectIR.isExact() ? EXACT_OBJECT : precision);
		this.ascendant = objectIR.getObject();
		this.objectIR = objectIR;
		assert validPrecision();
	}

	private ObjOp(ObjOp proto, OpPresets presets) {
		super(proto, presets);
		this.objectIR = proto.objectIR;
		this.ascendant = proto.ascendant;
	}

	private ObjOp(ObjOp proto, boolean stackAllocated) {
		super(proto, stackAllocated);
		this.objectIR = proto.objectIR;
		this.ascendant = proto.ascendant;
	}

	@Override
	public final ObjOp setPresets(OpPresets presets) {
		if (presets.is(getPresets())) {
			return this;
		}
		return new ObjOp(this, presets);
	}

	@Override
	public final ObjOp setStackAllocated(boolean stackAllocated) {
		if (isStackAllocated() == stackAllocated) {
			return this;
		}
		return new ObjOp(this, stackAllocated);
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final Obj getAscendant() {
		return this.ascendant;
	}

	@Override
	public final Obj getWellKnownType() {
		return getAscendant();
	}

	@Override
	public ObjOp phi(Code code, OpMeans<DataOp> ptr) {
		return new ObjOp(
				getBuilder(),
				getObjectIR(),
				code.means(c -> ptr.op().to(null, c, ptr().getType())),
				getAscendant(),
				getPrecision());
	}

	@Override
	public final VmtIRChain.Op vmtc(Code code) {
		if (!getPrecision().isExact()) {
			return super.vmtc(code);
		}
		return getObjectIR()
				.getVmtIR()
				.terminator()
				.pointer(getGenerator())
				.op(null, code);
	}


	@Override
	public ObjectValueFn valueFunc(Code code) {
		if (!getPrecision().isExact()) {
			return super.valueFunc(code);
		}
		return getObjectIR().getObjectValueIR().ptr().op(null, code);
	}

	public void fillDeps(CodeDirs dirs, HostOp host, Obj sample) {

		final Block code = dirs.code();
		final ObjectIRBody mainBodyIR =
				sample.ir(getGenerator()).typeBodies().getMainBodyIR();

		for (DepIR depIR : mainBodyIR.allDeps()) {
			uninitDep(code, depIR.getDep()).fill(dirs, host);
		}
	}

	public ObjOp cast(Obj ascendant) {
		getObjectIR().getObject().assertDerivedFrom(ascendant);
		if (ascendant.is(getAscendant())) {
			return this;
		}
		return new ObjOp(
				getBuilder(),
				getObjectIR(),
				this,
				ascendant,
				COMPATIBLE_OBJECT)
		.setPresets(getPresets());
	}

	@Override
	public ObjOp cast(ID id, CodeDirs dirs, Obj ascendant) {
		return cast(ascendant);
	}

	@Override
	public final ValueOp<ObjOp> value() {
		return this.value.get();
	}

	public final ResumeFromOp resumeFrom(Code code) {
		return (ResumeFromOp) instField(code, INST_RESUME_FROM);
	}

	public final LockFldOp lock(Code code) {
		return (LockFldOp) instField(code, INST_LOCK);
	}

	public InstFldOp<?, ?> instField(Code code, InstFldKind kind) {

		final InstFld<?, ?> fld = getObjectIR().bodies().instFld(kind);
		final InstFldOp<?, ?> op = fld.op(code, this);

		code.dumpName("Field: ", op);

		return op;
	}

	@Override
	public InstFldOp<?, ?> instField(CodeDirs dirs, InstFldKind kind) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + kind + " of " + this);
		final InstFldOp<?, ?> op = instField(subDirs.code(), kind);

		subDirs.done();

		return op;
	}

	public FldOp<?, ?> field(Code code, MemberKey memberKey) {

		final Fld<?, ?> fld = getObjectIR().bodies().fld(memberKey);
		final ObjOp host = cast(memberKey.getOrigin().toObject());
		final FldOp<?, ?> op = fld.op(code, host);

		if (fld.isStateless()) {
			code.debug("Stateless field: " + fld.getId());
		} else if (!op.isOmitted()) {
			code.dumpName("Field: ", op);
		} else {
			code.debug("Final field: " + op.getId());
		}

		return op;
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + memberKey + " of " + this);
		final Code code = subDirs.code();
		final FldOp<?, ?> op = field(code, memberKey);

		subDirs.done();

		return op;
	}

	public DepOp dep(Code code, Dep dep) {

		final DepIR ir = getObjectIR().bodies().dep(dep);
		final ObjOp host = cast(dep.getDeclaredIn());
		final DepOp op = ir.op(code, host);

		code.dump("Dep: ", op);

		return op;
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {

		final CodeDirs subDirs = dirs.begin(DEP_ID, dep.toString());
		final DepOp op = dep(subDirs.code(), dep);

		subDirs.done();

		return op;
	}

	public DepOp uninitDep(Code code, Dep dep) {

		final DepIR ir = getObjectIR().bodies().dep(dep);
		final ObjOp host = cast(dep.getDeclaredIn());
		final DepOp op = ir.op(code, host);

		code.dumpName("Uninit dep: ", op);

		return op;
	}

	public LocalIROp local(Code code, MemberKey memberKey) {

		final LocalIR local = getObjectIR().bodies().local(memberKey);
		final ObjOp host = cast(memberKey.getOrigin().toObject());
		final LocalIROp op = local.op(code, host);

		code.dump("Local: ", op);

		return op;
	}

	@Override
	public LocalIROp local(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin(LOCAL_ID, memberKey.toString());
		final Code code = subDirs.code();
		final LocalIROp op = local(code, memberKey);

		subDirs.done();

		return op;
	}

	@Override
	public TargetStoreOp allocateStore(ID id, Code code) {
		if (getPrecision().isExact()) {
			return getObjectIR().exactTargetStore(id, getPresets());
		}
		return super.allocateStore(id, code);
	}

	@Override
	public TargetStoreOp localStore(
			ID id,
			Function<CodeDirs, LocalIROp> getLocal) {
		if (getPrecision().isExact()) {
			return getObjectIR().exactTargetStore(id, getPresets());
		}
		return super.localStore(id, getLocal);
	}

	private boolean validPrecision() {
		assert getPrecision().isCompatible() :
			"Wrong object precision: " + this;
		assert (!getPrecision().isExact()
				|| getAscendant()
						.ir(getGenerator())
						.getSampleDeclaration().is(
								ptr()
								.getSampleDeclaration()
								.ir(getGenerator())
								.getSampleDeclaration())) :
				getAscendant() + " declaration differs from "
				+ ptr().getSampleDeclaration();
		return true;
	}

	private ValueOp<ObjOp> createValue() {

		final ValueIR valueIR = getAscendant().ir(getGenerator()).getValueIR();
		final ValueOp<ObjOp> value = valueIR.op(this);

		if (!getPrecision().isExact()) {
			return value;
		}

		return new ExactValueOp(value);
	}

	private static final class ExactValueOp extends ValueOp<ObjOp> {

		private final ValueOp<ObjOp> value;

		ExactValueOp(ValueOp<ObjOp> value) {
			super(value.getValueIR(), value.object());
			this.value = value;
		}

		@Override
		public StateOp<ObjOp> state() {
			return this.value.state();
		}

		@Override
		public ValOp writeTypedValue(ValDirs dirs) {

			final DefDirs defDirs = dirs.nested().def();

			objectValueIR().writeValue(defDirs, object());
			defDirs.done();

			return defDirs.result();
		}

		@Override
		protected void writeVoidValue(CodeDirs dirs) {
			defaultVoid(dirs);
		}

		private final ObjectIR objectIR() {
			return object().getAscendant().ir(getGenerator());
		}

		private final ObjectValueIR objectValueIR() {
			return objectIR().getObjectValueIR();
		}

	}

}
