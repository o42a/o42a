/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.field.variable;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.codegen.code.op.Atomicity.VOLATILE;
import static org.o42a.codegen.code.op.RMWKind.R_OR_W;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.Val.VAL_ASSIGN;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRType;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.member.MemberKey;


public final class AssignerFldOp extends FldOp {

	private final AssignerFld.Op ptr;

	AssignerFldOp(AssignerFld fld, ObjOp host, AssignerFld.Op ptr) {
		super(fld, host);
		this.ptr = ptr;
	}

	@Override
	public final AssignerFld fld() {
		return (AssignerFld) super.fld();
	}

	@Override
	public final AssignerFld.Op ptr() {
		return this.ptr;
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {

		final Block code = dirs.code();
		final ObjectOp valueObject =
				value.materialize(dirs, tempObjHolder(code.getAllocator()));
		final StructRecOp<ObjectIRType.Op> boundRec = ptr().bound(null, code);
		final ObjectIRType.Op knownBound = boundRec.load(null, code, VOLATILE);

		// Bound is already known.
		final CondBlock boundUnknown =
				knownBound.isNull(null, code)
				.branch(code, "bound_unknown", "bound_known");
		final Block boundKnown = boundUnknown.otherwise();

		boundKnown.dumpName("Known bound: ", knownBound);

		final CodeDirs boundKnownDirs = dirs.sub(boundKnown);
		final ObjectOp castObject = valueObject.dynamicCast(
				boundKnown.id("cast_target"),
				boundKnownDirs,
				knownBound.op(getBuilder(), DERIVED),
				fld().linkStruct().getTypeRef().typeObject(dummyUser()),
				true);

		assignValue(boundKnown, castObject);

		boundKnown.dump("Assigned: ", this);
		castObject.value().writeCond(boundKnownDirs);
		boundKnown.go(code.tail());

		// Bound is not known yet.
		final VariableAssignerFunc assigner =
				ptr().assigner(null, boundUnknown).load(null, boundUnknown);
		final BoolOp assigned =
				assigner.assign(boundUnknown, host(), valueObject);

		assigned.goUnless(boundUnknown, dirs.falseDir());
		boundUnknown.go(code.tail());
	}

	@Override
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
	}

	public ObjectOp target(CodeDirs dirs, ObjHolder holder) {

		final FldKind kind = fld().getKind();

		dirs.code().dumpName(kind + " field: ", this);
		dirs.code().dumpName(kind + " host: ", host());

		final ValDirs valDirs = dirs.nested().value(
				host().getAscendant().value().getValueStruct());
		final Block code = valDirs.code();
		final ValOp value = host().value().writeValue(valDirs);

		final DataOp targetPtr =
				value.value(null, code)
				.toPtr(null, code)
				.load(code.id("ptarget"), code)
				.toData(null, code);
		final ObjectOp target = anonymousObject(
				getBuilder(),
				targetPtr,
				fld().linkStruct().getTypeRef().typeObject(dummyUser()));

		final Block resultCode = valDirs.done().code();

		return holder.holdVolatile(resultCode, target);
	}

	void assignValue(Block code, ObjectOp object) {

		final ValType.Op value =
				host().objectType(code).ptr().data(code).value(code);
		final Block skip = code.addBlock("skip");
		final ValFlagsOp flags = value.flags(code, ACQUIRE_RELEASE);

		code.acquireBarrier();

		final ValFlagsOp old =
				flags.atomicRMW(code.id("old"), code, R_OR_W, VAL_ASSIGN);

		old.assigning(null, code).go(code, skip.head());

		value.rawValue(null, code)
		.toAny(null, code)
		.toPtr(null, code)
		.store(code, object.toAny(null, code), ATOMIC);

		flags.store(code, VAL_CONDITION);

		skip.go(code.tail());
	}

}
