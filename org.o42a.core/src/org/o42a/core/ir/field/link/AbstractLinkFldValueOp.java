/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.link;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


public abstract class AbstractLinkFldValueOp<F extends RefFldOp<?, ?>>
		implements HostValueOp {

	private final F fld;

	public AbstractLinkFldValueOp(F fld) {
		this.fld = fld;
	}

	public final F fld() {
		return this.fld;
	}

	@Override
	public void writeCond(CodeDirs dirs) {
		object(dirs);
	}

	@Override
	public ValOp writeValue(ValDirs dirs) {

		final Block code = dirs.code();
		final AnyOp objectPtr = object(dirs.dirs()).toAny(null, code);

		return dirs.value().store(code, objectPtr);
	}

	@Override
	public String toString() {
		if (this.fld == null) {
			return super.toString();
		}
		return this.fld.toString();
	}

	protected final ObjectOp object(CodeDirs dirs) {
		return this.fld.target(
				dirs,
				tempObjHolder(dirs.getAllocator()));
	}

}
