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
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.object.dep.DepOp.DEP_ID;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.ir.object.dep.DepOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
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
	private final ObjectIRBodyOp ptr;
	private final Obj ascendant;
	private ValueOp value;

	ObjOp(
			ObjectIR objectIR,
			ObjectIRBodyOp ptr,
			Obj ascendant,
			ObjectDataOp data) {
		super(data);
		this.objectIR = objectIR;
		this.ptr = ptr;
		assert ascendant != null :
			"Object ascendant not specified";
		this.ascendant = ascendant;
		assert validPrecision();
	}

	ObjOp(
			CodeBuilder builder,
			ObjectIR objectIR,
			ObjectIRBodyOp ptr,
			Obj ascendant,
			ObjectPrecision precision) {
		super(builder, precision);
		this.ptr = ptr;
		this.objectIR = objectIR;
		assert ascendant != null :
			"Object ascendant not specified";
		this.ascendant = ascendant;
		assert validPrecision();
	}

	ObjOp(CodeBuilder builder, ObjectIR objectIR, ObjectIRBodyOp ptr) {
		super(builder, ObjectPrecision.EXACT);
		this.ptr = ptr;
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
	public final ObjectIRBodyOp ptr() {
		return this.ptr;
	}

	@Override
	public final ObjectIRBodyOp ptr(Code code) {
		return ptr();
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
		if (ascendant.is(getContext().getVoid())) {
			return this;
		}
		if (getPrecision().isExact()) {
			return staticCast(dirs.code(), ascendant);
		}
		if (ascendant.cloneOf(ptr().getAscendant())) {
			// Clone shares the body with it`s origin.
			return this;
		}
		return dynamicCast(id, dirs, ascendant);
	}

	@Override
	public FldOp<?> field(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + memberKey + " of " + this);
		final Code code = subDirs.code();
		final Fld<?> fld = getObjectIR().fld(memberKey);
		final ID hostId = FIELD_HOST_ID.sub(memberKey.getMemberId());
		final ObjOp host = cast(
				hostId,
				subDirs,
				memberKey.getOrigin().toObject());
		final FldOp<?> op = fld.op(code, host);

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
		final DepIR ir = getObjectIR().dep(dep);
		final ObjOp host = cast(
				DEP_HOST_ID.sub(dep),
				subDirs,
				dep.getDeclaredIn());
		final DepOp op = ir.op(code, host);

		code.dumpName("Dep: ", op);

		subDirs.done();

		return op;
	}

	public FldOp<?> declaredField(Code code, MemberKey memberKey) {
		return ptr().declaredField(code, this, memberKey);
	}

	private boolean validPrecision() {
		assert getPrecision().isCompatible() :
			"Wrong object precision: " + this;
		assert (!getPrecision().isExact()
				|| getAscendant().cloneOf(ptr().getAscendant())) :
				getAscendant() + " is not a clone of "
				+ ptr().getAscendant();
		return true;
	}

	private ObjOp staticCast(Code code, Obj ascendant) {

		final ObjectIRBody ascendantBodyIR =
				getObjectIR().bodyIR(ascendant);
		final ObjectIRBodyOp ascendantBody =
				ascendantBodyIR.pointer(getGenerator()).op(null, code);

		final ObjectDataOp cachedData = cachedData();

		if (cachedData != null) {
			return ascendantBody.op(getObjectIR(), cachedData, ascendant);
		}

		return ascendantBody.op(getBuilder(), getObjectIR(), ascendant, EXACT);
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

}
