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
package org.o42a.core.ir.object.state;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


public abstract class KeeperIROp<O extends KeeperIROp<O>> extends StructOp<O> {

	public KeeperIROp(StructWriter<O> writer) {
		super(writer);
	}

	@Override
	public KeeperIRType<O> getType() {
		return (KeeperIRType<O>) super.getType();
	}

	protected abstract void writeCond(KeeperOp keeper, CodeDirs dirs);

	protected abstract ValOp writeValue(KeeperOp keeper, ValDirs dirs);

	protected abstract ObjectOp dereference(
			KeeperOp keeper,
			CodeDirs dirs,
			ObjHolder holder);

}