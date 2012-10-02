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
package org.o42a.core.ir.field.variable;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.field.variable.VarSte.CAST_TARGET_ID;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.field.link.AbstractLinkFldValueOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRTypeOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectRefFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.value.link.LinkValueStruct;


public final class VarFldOp extends RefFldOp<VarFld.Op, ObjectRefFunc> {

	private final VarFld.Op ptr;

	VarFldOp(VarFld fld, ObjOp host, VarFld.Op ptr) {
		super(fld, host);
		this.ptr = ptr;
	}

	@Override
	public final VarFld fld() {
		return (VarFld) super.fld();
	}

	@Override
	public final VarFld.Op ptr() {
		return this.ptr;
	}

	@Override
	public HostValueOp value() {
		return new VarFldValueOp(this);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	@Override
	public ObjectOp target(CodeDirs dirs, ObjHolder holder) {
		return super.target(dirs, holder.toVolatile());
	}

	@Override
	protected ObjectOp findTarget(CodeDirs dirs, ObjHolder holder) {
		return loadOrConstructTarget(dirs, holder, true);
	}

	private void assign(CodeDirs dirs, HostOp value) {

		final Obj object = fld().getField().toObject();
		final ObjectValue objectValue = object.value();
		final LinkValueStruct linkStruct =
				objectValue.getValueStruct().toLinkStruct();
		final Obj targetType = linkStruct.getTypeRef().getType();

		final Block code = dirs.code();

		tempObjHolder(code.getAllocator()).holdVolatile(code, host());

		final ObjectOp valueObject =
				value.materialize(dirs, tempObjHolder(code.getAllocator()));
		final StructRecOp<ObjectIRTypeOp> boundRec = ptr().bound(null, code);

		code.acquireBarrier();

		final ObjectIRTypeOp knownBound = boundRec.load(null, code, ATOMIC);

		// Bound is already known.
		final CondBlock boundUnknown =
				knownBound.isNull(null, code)
				.branch(code, "bound_unknown", "bound_known");
		final Block boundKnown = boundUnknown.otherwise();

		boundKnown.dumpName("Known bound: ", knownBound);

		final CodeDirs boundKnownDirs = dirs.sub(boundKnown);
		final ObjectOp castObject = valueObject.dynamicCast(
				CAST_TARGET_ID,
				boundKnownDirs,
				knownBound.op(getBuilder(), DERIVED),
				targetType,
				true);

		ptr().object(null, boundKnown).store(
				boundKnown,
				castObject.toData(null, boundKnown),
				ACQUIRE_RELEASE);
		boundKnown.dump("Assigned: ", this);
		boundKnown.go(code.tail());

		// Bound is not known yet.
		final VariableAssignerFunc assigner =
				ptr().assigner(null, boundUnknown).load(null, boundUnknown);
		final BoolOp assigned =
				assigner.assign(boundUnknown, host(), valueObject);

		assigned.goUnless(boundUnknown, dirs.falseDir());
		boundUnknown.go(code.tail());
	}

	private static final class VarFldValueOp
			extends AbstractLinkFldValueOp<VarFldOp> {

		VarFldValueOp(VarFldOp fld) {
			super(fld);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			fld().assign(dirs, value);
		}

	}

}
