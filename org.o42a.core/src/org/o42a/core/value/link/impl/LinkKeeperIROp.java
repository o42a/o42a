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
package org.o42a.core.value.link.impl;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.KeeperIROp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.LinkValueType;


final class LinkKeeperIROp extends KeeperIROp<LinkKeeperIROp> {

	LinkKeeperIROp(StructWriter<LinkKeeperIROp> writer) {
		super(writer);
	}

	public final DataRecOp object(Code code) {
		return ptr(null, code, getType().object());
	}

	@Override
	public LinkKeeperIRType getType() {
		return (LinkKeeperIRType) super.getType();
	}

	@Override
	protected void writeCond(KeeperOp<LinkKeeperIROp> keeper, CodeDirs dirs) {
		new LinkKeeperEval(keeper, this).writeCond(dirs);
	}

	@Override
	protected ValOp writeValue(KeeperOp<LinkKeeperIROp> keeper, ValDirs dirs) {
		return new LinkKeeperEval(keeper, this).writeValue(dirs);
	}

	@Override
	protected ObjectOp dereference(
			KeeperOp<LinkKeeperIROp> keeper,
			CodeDirs dirs,
			ObjHolder holder) {

		final TypeParameters<?> typeParameters =
				keeper.keeperIR().getTypeParameters();
		final LinkValueType linkType =
				typeParameters.getValueType().toLinkType();

		final ValDirs valDirs =
				dirs.value(linkType, TEMP_VAL_HOLDER);
		final Block valCode = valDirs.code();
		final ValOp value = writeValue(keeper, valDirs);
		final DataOp objectPtr =
				value.value(null, valCode)
				.toRec(null, valCode)
				.load(null, valCode)
				.toData(null, valCode);

		valDirs.done();

		return anonymousObject(
				dirs.getBuilder(),
				objectPtr,
				linkType.interfaceRef(typeParameters).getType());
	}

}
