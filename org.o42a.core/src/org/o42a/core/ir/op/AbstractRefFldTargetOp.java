/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.object.Obj;


public abstract class AbstractRefFldTargetOp implements RefTargetOp {

	private final AbstractRefFldTargetIR ir;
	private final DataRecOp ptr;

	public AbstractRefFldTargetOp(AbstractRefFldTargetIR ir, DataRecOp ptr) {
		this.ir = ir;
		this.ptr = ptr;
	}

	public final AbstractRefFldTargetIR ir() {
		return this.ir;
	}

	@Override
	public final DataRecOp ptr() {
		return this.ptr;
	}

	public abstract Obj getWellKnownOwner();

	@Override
	public void storeTarget(CodeDirs dirs, HostOp host) {

		final Block noDep = dirs.addBlock("no_dep");
		final CodeDirs depDirs =
				dirs.getBuilder().dirs(dirs.code(), noDep.head());

		final ObjectOp object = host.target().materialize(
				depDirs,
				tempObjHolder(depDirs.getAllocator()));
		final Block code = depDirs.code();

		ptr().store(code, object.toData(null, code));

		if (noDep.exists()) {

			final CodeBuilder builder = dirs.getBuilder();
			final ObjectIR noneIR =
					builder.getContext().getNone().ir(builder.getGenerator());

			ptr().store(
					noDep,
					noneIR.op(builder, noDep).toData(null, noDep));
			noDep.go(code.tail());
		}

		depDirs.done();
	}

	@Override
	public final void copyTarget(CodeDirs dirs, TargetStoreOp store) {

		final Block code = dirs.code();

		ptr().store(code, copyObject(dirs, store).toData(null, code));
	}

	@Override
	public final TargetOp loadTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp owner = anonymousObject(
				dirs.getBuilder(),
				ptr().load(null, code),
				getWellKnownOwner());

		return fldOf(dirs, owner);
	}

	@Override
	public String toString() {
		if (this.ptr == null) {
			return super.toString();
		}
		return this.ptr.toString();
	}

	protected abstract TargetOp fldOf(CodeDirs dirs, ObjectOp owner);

	protected abstract ObjectOp copyObject(CodeDirs dirs, TargetStoreOp store);

}
