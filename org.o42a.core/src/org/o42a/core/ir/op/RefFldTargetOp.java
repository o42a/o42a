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
import org.o42a.codegen.code.op.DumpablePtrOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.dep.DepOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.object.Obj;


public abstract class RefFldTargetOp implements RefTargetOp {

	private final RefTargetIR ir;
	private final DepOp dep;

	public RefFldTargetOp(RefTargetIR ir, DepOp dep) {
		this.ir = ir;
		this.dep = dep;
	}

	public final RefTargetIR ir() {
		return this.ir;
	}

	@Override
	public final DumpablePtrOp<?> ptr() {
		return this.dep.ptr();
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

		this.dep.dep().object(code).store(code, object.toData(null, code));

		if (noDep.exists()) {

			final CodeBuilder builder = dirs.getBuilder();
			final ObjectIR noneIR =
					builder.getContext().getNone().ir(builder.getGenerator());

			this.dep.dep().object(noDep).store(
					noDep,
					noneIR.op(builder, noDep).toData(null, noDep));
			noDep.go(code.tail());
		}

		depDirs.done();
	}

	@Override
	public final void copyTarget(CodeDirs dirs, TargetStoreOp store) {

		final Block code = dirs.code();

		this.dep.dep().object(code).store(
				code,
				loadOwner(dirs, store).toData(null, code));
	}

	@Override
	public final FldOp<?, ?> loadTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp owner = anonymousObject(
				dirs,
				this.dep.dep().object(code).load(null, code),
				getWellKnownOwner());

		return fldOf(dirs, owner);
	}

	@Override
	public String toString() {
		if (this.dep == null) {
			return super.toString();
		}
		return this.dep.toString();
	}

	protected abstract FldOp<?, ?> fldOf(CodeDirs dirs, ObjectOp owner);

	protected abstract ObjectOp loadOwner(CodeDirs dirs, TargetStoreOp store);

}
