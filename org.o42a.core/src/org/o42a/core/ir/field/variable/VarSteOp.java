/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;


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
	public TargetOp dereference(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TargetOp field(CodeDirs dirs, MemberKey memberKey) {
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
				.toRec(null, code)
				.load(null, code)
				.toData(null, code);
		final ObjectOp target = anonymousObject(
				getBuilder(),
				targetPtr,
				fld().interfaceType());

		final Block resultCode = valDirs.done().code();

		return holder.holdVolatile(resultCode, target);
	}

	private void assign(CodeDirs dirs, HostOp value) {

		final Block code = dirs.code();

		tempObjHolder(code.getAllocator()).holdVolatile(code, host());

		final ObjectOp valueObject =
				value.materialize(dirs, tempObjHolder(code.getAllocator()));

		ptr().object(null, code).store(
				code,
				valueObject.toData(null, code),
				ACQUIRE_RELEASE);
		code.dump("Assigned: ", this);
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
