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
package org.o42a.core.ir.field.owner;

import static org.o42a.core.ir.object.ObjectOp.approximateObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.owner.OwnerFld.Op;
import org.o42a.core.ir.field.owner.OwnerFld.Type;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.ir.op.OpPresets;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;


public class OwnerFldOp extends FldOp<OwnerFld.Op, OwnerFld.Type> {

	OwnerFldOp(ObjOp host, OwnerFld fld, OpMeans<OwnerFld.Op> ptr) {
		super(host, fld, ptr);
	}

	private OwnerFldOp(FldOp<Op, Type> proto, OpPresets presets) {
		super(proto, presets);
	}

	@Override
	public final OwnerFldOp setPresets(OpPresets presets) {
		if (presets.is(getPresets())) {
			return this;
		}
		return new OwnerFldOp(this, presets);
	}

	@Override
	public final OwnerFld fld() {
		return (OwnerFld) super.fld();
	}

	@Override
	public HostValueOp value() {
		return objectFldValueOp();
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {
		return scope(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return scope(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return scope(dirs, tempObjHolder(dirs.getAllocator()))
				.dereference(dirs, holder);
	}

	private ObjectOp scope(CodeDirs dirs, ObjHolder holder) {
		if (isOmitted()) {

			final Obj target = fld().getField().toObject();
			final ObjectIR targetIR = target.ir(getGenerator());

			return targetIR.exactOp(getBuilder(), dirs.code())
					.setPresets(getPresets());
		}

		final Block code = dirs.code();
		final DataOp targetPtr = ptr().object(code).load(null, code);

		return holder.hold(
				code,
				approximateObject(dirs, targetPtr, fld().getAscendant())
				.setPresets(getPresets()));
	}

}
