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

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.VOLATILE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRTypeOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.util.string.ID;


public final class VarSteOp extends FldOp {

	private final VarSte.Op ptr;

	VarSteOp(VarSte fld, ObjOp host, VarSte.Op ptr) {
		super(host, fld);
		this.ptr = ptr;
	}

	@Override
	public final VarSte fld() {
		return (VarSte) super.fld();
	}

	@Override
	public final VarSte.Op ptr() {
		return this.ptr;
	}

	@Override
	public HostValueOp value() {
		return new VarSteValueOp(this);
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
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
	}

	public ObjectOp target(CodeDirs dirs, ObjHolder holder) {

		final FldKind kind = fld().getKind();

		dirs.code().dumpName(kind + " field: ", this);
		dirs.code().dumpName(kind + " host: ", host());

		final ValDirs valDirs = dirs.nested().value(
				host().getAscendant().type().getValueType(),
				TEMP_VAL_HOLDER);
		final Block code = valDirs.code();
		final ValOp value = host().value().writeValue(valDirs);

		final DataOp targetPtr =
				value.value(null, code)
				.toPtr(null, code)
				.load(null, code)
				.toData(null, code);
		final ObjectOp target = anonymousObject(
				getBuilder(),
				targetPtr,
				fld().interfaceType());

		final Block resultCode = valDirs.done().code();

		return holder.holdVolatile(resultCode, target);
	}

	void assignValue(Block code, ObjectOp object) {

		final DataRecOp objectRec = ptr().object(null, code);

		objectRec.store(code, object.toData(null, code), ACQUIRE_RELEASE);
	}

	private void assign(CodeDirs dirs, HostOp value) {

		final Block code = dirs.code();
		final ObjectOp valueObject =
				value.materialize(dirs, tempObjHolder(code.getAllocator()));
		final StructRecOp<ObjectIRTypeOp> boundRec = ptr().bound(null, code);
		final ObjectIRTypeOp knownBound = boundRec.load(null, code, VOLATILE);

		// Bound is already known.
		final CondBlock boundUnknown =
				knownBound.isNull(null, code)
				.branch(code, "bound_unknown", "bound_known");
		final Block boundKnown = boundUnknown.otherwise();

		boundKnown.dumpName("Known bound: ", knownBound);

		final CodeDirs boundKnownDirs = dirs.sub(boundKnown);
		final ObjectOp castObject = valueObject.dynamicCast(
				ID.id("cast_target"),
				boundKnownDirs,
				knownBound.op(getBuilder(), DERIVED),
				fld().interfaceType(),
				true);

		assignValue(boundKnown, castObject);

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

	private static final class VarSteValueOp implements HostValueOp {

		private final VarSteOp fld;

		VarSteValueOp(VarSteOp fld) {
			this.fld = fld;
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			this.fld.assign(dirs, value);
		}

		@Override
		public String toString() {
			if (this.fld == null) {
				return super.toString();
			}
			return this.fld.toString() + '`';
		}

	}

}
