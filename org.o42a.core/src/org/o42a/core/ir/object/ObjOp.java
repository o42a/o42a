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
import static org.o42a.core.ir.field.local.LocalIR.LOCAL_ID;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.dep.DepIR;
import org.o42a.core.ir.field.dep.DepOp;
import org.o42a.core.ir.field.inst.InstFld;
import org.o42a.core.ir.field.inst.InstFldKind;
import org.o42a.core.ir.field.inst.InstFldOp;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.StateOp;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public final class ObjOp extends ObjectOp {

	private final ObjectIR objectIR;
	private final Obj ascendant;
	private ValueOp value;

	ObjOp(
			CodeBuilder builder,
			ObjectIR objectIR,
			ObjectIROp ptr,
			Obj ascendant,
			ObjectPrecision precision) {
		super(builder, ptr, precision);
		this.objectIR = objectIR;
		assert ascendant != null :
			"Object ascendant not specified";
		this.ascendant = ascendant;
		assert validPrecision();
	}

	ObjOp(CodeBuilder builder, ObjectIR objectIR, ObjectIROp ptr) {
		super(builder, ptr, ObjectPrecision.EXACT);
		this.ascendant = objectIR.getObject();
		this.objectIR = objectIR;
		assert validPrecision();
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
	public ObjOp phi(Code code, DataOp ptr) {
		return new ObjOp(
				getBuilder(),
				getObjectIR(),
				ptr.to(null, code, ptr().getType()),
				getAscendant(),
				getPrecision());
	}

	public final VmtIRChain.Op vmtc(Code code) {
		return ptr(code).objectData(code).vmtc(code).load(null, code);
	}

	@Override
	public final ValueOp value() {
		if (this.value != null) {
			return this.value;
		}

		final ValueIR valueIR = getAscendant().ir(getGenerator()).getValueIR();
		final ValueOp value = valueIR.op(this);

		if (!getPrecision().isExact()) {
			return this.value = value;
		}

		return this.value = new ExactValueOp(value);
	}

	@Override
	public ObjOp cast(ID id, CodeDirs dirs, Obj ascendant) {
		getObjectIR().getObject().assertDerivedFrom(ascendant);
		if (ascendant.is(getAscendant())) {
			return this;
		}
		return new ObjOp(
				getBuilder(),
				getObjectIR(),
				ptr(dirs.code()),
				ascendant,
				COMPATIBLE);
	}

	@Override
	public InstFldOp<?, ?> instField(CodeDirs dirs, InstFldKind kind) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + kind + " of " + this);
		final Code code = subDirs.code();
		final InstFld<?, ?> fld = getObjectIR().bodies().instFld(kind);
		final InstFldOp<?, ?> op = fld.op(code, this);

		code.dumpName("Field: ", op);

		subDirs.done();

		return op;
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + memberKey + " of " + this);
		final Code code = subDirs.code();
		final Fld<?, ?> fld = getObjectIR().bodies().fld(memberKey);
		final ID hostId = FIELD_HOST_ID.sub(memberKey.getMemberId());
		final ObjOp host = cast(
				hostId,
				subDirs,
				memberKey.getOrigin().toObject());
		final FldOp<?, ?> op = fld.op(code, host);

		if (!op.isOmitted()) {
			code.dumpName("Field: ", op);
		} else {
			code.debug("Final field: " + op.getId());
		}

		subDirs.done();

		return op;
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {

		final CodeDirs subDirs = dirs.begin(DEP_ID, dep.toString());
		final Code code = subDirs.code();
		final DepIR ir = getObjectIR().bodies().dep(dep);
		final ObjOp host = cast(
				DEP_HOST_ID.sub(dep),
				subDirs,
				dep.getDeclaredIn());
		final DepOp op = ir.op(code, host);

		subDirs.done();

		return op;
	}

	@Override
	public LocalIROp local(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin(LOCAL_ID, memberKey.toString());
		final Code code = subDirs.code();
		final LocalIR local = getObjectIR().bodies().local(memberKey);
		final ID hostId = FIELD_HOST_ID.sub(memberKey.getMemberId());
		final ObjOp host = cast(
				hostId,
				subDirs,
				memberKey.getOrigin().toObject());
		final LocalIROp op = local.op(code, host);

		code.dump("Local: ", op);

		subDirs.done();

		return op;
	}

	@Override
	public TargetStoreOp allocateStore(ID id, Code code) {
		if (getPrecision().isExact()) {
			return new ExactObjectStoreOp(getObjectIR());
		}
		return super.allocateStore(id, code);
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

	private static final class ExactValueOp extends ValueOp {

		private final ValueOp value;

		ExactValueOp(ValueOp value) {
			super(value.getValueIR(), value.object());
			this.value = value;
		}

		@Override
		public StateOp state() {
			return this.value.state();
		}

		@Override
		public ValOp writeTypedValue(ValDirs dirs) {

			final DefDirs defDirs = dirs.nested().def();

			objectValueIR().writeValue(defDirs, obj());
			defDirs.done();

			return defDirs.result();
		}

		@Override
		protected void writeVoidValue(CodeDirs dirs) {
			defaultVoid(dirs);
		}

		private final ObjOp obj() {
			return (ObjOp) object();
		}

		private final ObjectIR objectIR() {
			return obj().getAscendant().ir(getGenerator());
		}

		private final ObjectValueIR objectValueIR() {
			return objectIR().getObjectValueIR();
		}

	}

	private static final class ExactObjectStoreOp implements TargetStoreOp {

		private final ObjectIR objectIR;

		ExactObjectStoreOp(ObjectIR objectIR) {
			this.objectIR = objectIR;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			return this.objectIR.op(dirs);
		}

		@Override
		public String toString() {
			if (this.objectIR == null) {
				return super.toString();
			}
			return this.objectIR.getObject().toString();
		}

	}

}
