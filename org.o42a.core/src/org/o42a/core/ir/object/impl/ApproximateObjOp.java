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
import static org.o42a.core.ir.object.ObjectPrecision.APPROXIMATE_OBJECT;
import static org.o42a.core.ir.object.desc.AscendantDescIR.ASCENDANT_DESC_IR;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.dep.DepOp;
import org.o42a.core.ir.field.inst.InstFldKind;
import org.o42a.core.ir.field.inst.InstFldOp;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public final class ApproximateObjOp extends ObjectOp {

	private final Obj wellKnownType;

	public ApproximateObjOp(
			CodeBuilder builder,
			OpMeans<ObjectIROp> ptr,
			Obj wellKnownType) {
		super(builder, ptr, APPROXIMATE_OBJECT);
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
		return approximateObject(getBuilder(), code, ptr, getWellKnownType());
	}

	@Override
	public ObjOp cast(ID id, CodeDirs dirs, Obj ascendant) {
		assert (getWellKnownType().type().derivedFrom(ascendant.type())
				|| ascendant.type().derivedFrom(getWellKnownType().type()))
			: "Can not cast " + getWellKnownType() + " to " + ascendant;
		if (ascendant.is(getContext().getVoid())) {
			// Everything is compatible with void.
			return getWellKnownType()
					.ir(getGenerator())
					.compatibleOp(dirs);
		}
		return dynamicCast(id, dirs, ascendant);
	}

	@Override
	public InstFldOp<?, ?> instField(CodeDirs dirs, InstFldKind kind) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + kind + " of " + this);
		final ObjOp ascendant = cast(null, subDirs, getWellKnownType());
		final InstFldOp<?, ?> op = ascendant.instField(subDirs, kind);

		subDirs.code().dumpName("Field: ", op);
		subDirs.done();

		return op;
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {

		final ID hostId = FIELD_HOST_ID.sub(memberKey.getMemberId());
		final ObjOp ascendant = cast(
				hostId,
				dirs,
				memberKey.getOrigin().toObject());

		return ascendant.field(dirs, memberKey);
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {

		final ObjOp ascendant = cast(
				DEP_HOST_ID.sub(dep),
				dirs,
				dep.getDeclaredIn());

		return ascendant.dep(dirs, dep);
	}

	@Override
	public LocalIROp local(CodeDirs dirs, MemberKey memberKey) {

		final ID hostId = FIELD_HOST_ID.sub(memberKey.getMemberId());
		final ObjOp ascendant = cast(
				hostId,
				dirs,
				memberKey.getOrigin().toObject());

		return ascendant.local(dirs, memberKey);
	}

	@Override
	protected ValueOp createValue() {
		return getWellKnownType().ir(getGenerator()).getValueIR().op(this);
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

		final ObjOp result = ascendantIR.compatibleOp(
				getBuilder(),
				code.means(
						c -> ptr()
						.toData(null, c)
						.to(id, c, ascendantIR.getType())));

		subDirs.done();

		return result;
	}

}
