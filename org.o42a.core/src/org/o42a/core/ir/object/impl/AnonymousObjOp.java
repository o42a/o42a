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
package org.o42a.core.ir.object.impl;

import static org.o42a.core.ir.field.Fld.FIELD_ID;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.dep.DepOp.DEP_ID;
import static org.o42a.core.ir.object.type.AscendantDescIR.ASCENDANT_DESC_IR;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.dep.DepOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public final class AnonymousObjOp extends ObjectOp {

	private final Obj wellKnownType;
	private ValueOp value;

	public AnonymousObjOp(
			CodeBuilder builder,
			ObjectIROp ptr,
			Obj wellKnownType) {
		super(builder, ptr, DERIVED);
		assert wellKnownType != null :
			"Object type not specified";
		this.wellKnownType = wellKnownType.getInterface();
	}

	@Override
	public final Obj getWellKnownType() {
		return this.wellKnownType;
	}

	@Override
	public ObjectOp phi(Code code, DataOp ptr) {
		return anonymousObject(getBuilder(), code, ptr, getWellKnownType());
	}

	@Override
	public final ValueOp value() {
		if (this.value != null) {
			return this.value;
		}

		final ValueIR valueIR =
				getWellKnownType().ir(getGenerator()).getValueIR();

		return this.value = valueIR.op(this);
	}

	@Override
	public ObjOp cast(ID id, CodeDirs dirs, Obj ascendant) {
		assert (getWellKnownType().type().derivedFrom(ascendant.type())
				|| ascendant.type().derivedFrom(getWellKnownType().type()))
			: "Can not cast " + getWellKnownType() + " to " + ascendant;
		if (ascendant.is(getContext().getVoid())) {
			// Everything is compatible with void.
			return ptr().op(getBuilder(), getWellKnownType(), COMPATIBLE);
		}
		return dynamicCast(id, dirs, ascendant);
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + memberKey + " of " + this);
		final ID hostId = FIELD_HOST_ID.sub(memberKey.getMemberId());
		final ObjOp ascendant = cast(
				hostId,
				subDirs,
				memberKey.getOrigin().toObject());
		final FldOp<?, ?> op = ascendant.field(subDirs, memberKey);

		if (!op.isOmitted()) {
			subDirs.code().dumpName("Field: ", op);
		} else {
			subDirs.code().debug("Final field: " + op.getId());
		}
		subDirs.done();

		return op;
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {

		final CodeDirs subDirs = dirs.begin(DEP_ID, dep.toString());
		final ObjOp ascendant = cast(
				DEP_HOST_ID.sub(dep),
				subDirs,
				dep.getDeclaredIn());
		final DepOp op = ascendant.dep(subDirs, dep);

		subDirs.done();

		return op;
	}

	private ObjOp dynamicCast(ID id, CodeDirs dirs, Obj ascendant) {

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());
		final CodeDirs subDirs = dirs.begin(
				id != null ? id : CAST_ID,
				"Dynamic cast " + this + " to " + ascendantIR.getId());

		final Block code = subDirs.code();

		final RelList.Op ascendants =
				objectData(code)
				.loadDesc(code)
				.ascendants(code);
		final Int32op ascendantIndex =
				code.int32(ascendantIR.typeBodies().getBodyIRs().size() - 1);

		// Is it enough ascendant records in type descriptor?
		ascendants.size(code)
		.load(null, code)
		.lt(null, code, ascendantIndex)
		.go(code, dirs.falseDir());

		// Does ascendant record match the required one?
		ascendants.loadList(code)
		.to(null, code, ASCENDANT_DESC_IR)
		.offset(null, code, ascendantIndex)
		.desc(code)
		.load(null, code)
		.ne(null, code, ascendantIR.getDescIR().ptr().op(null, code))
		.go(code, dirs.falseDir());

		final ObjOp result =
				ptr(code)
				.toData(null, code)
				.to(id, code, ascendantIR.getType())
				.op(getBuilder(), ascendantIR.getObject(), COMPATIBLE);

		subDirs.done();

		return result;
	}

}
