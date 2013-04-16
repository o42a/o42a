/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.state.DepIR;
import org.o42a.core.ir.object.state.DepIR.Op;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class AbstractRefFldTargetOp implements RefTargetOp {

	private final AbstractRefFldTargetIR ir;
	private final DepIR.Op ptr;

	public AbstractRefFldTargetOp(AbstractRefFldTargetIR ir, Op ptr) {
		this.ir = ir;
		this.ptr = ptr;
	}

	public final AbstractRefFldTargetIR ir() {
		return this.ir;
	}

	@Override
	public final DepIR.Op ptr() {
		return this.ptr;
	}

	public abstract Obj getWellKnownOwner();

	@Override
	public void storeTarget(CodeDirs dirs, HostOp host) {

		final DataRecOp objectRec = this.ptr.object(dirs.code());
		final Block noDep = dirs.addBlock("no_dep");
		final CodeDirs depDirs =
				dirs.getBuilder().dirs(dirs.code(), noDep.head());

		final ObjectOp object = host.target().materialize(
				depDirs,
				tempObjHolder(depDirs.getAllocator()));
		final Block code = depDirs.code();

		objectRec.store(code, object.toData(null, code));

		if (noDep.exists()) {

			final CodeBuilder builder = dirs.getBuilder();
			final ObjectIR noneIR =
					builder.getContext().getNone().ir(builder.getGenerator());

			objectRec.store(
					noDep,
					noneIR.op(builder, noDep).toData(null, noDep));
			noDep.go(code.tail());
		}

		depDirs.done();
	}

	@Override
	public TargetOp loadTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp owner = anonymousObject(
				dirs.getBuilder(),
				this.ptr.object(code).load(null, code),
				getWellKnownOwner());

		return fldOf(dirs, owner);
	}

	@Override
	public DataOp toData(ID id, Code code) {
		return ptr().toData(id, code);
	}

	@Override
	public AnyOp toAny(ID id, Code code) {
		return ptr().toAny(id, code);
	}

	@Override
	public String toString() {
		if (this.ptr == null) {
			return super.toString();
		}
		return this.ptr.toString();
	}

	protected abstract TargetOp fldOf(CodeDirs dirs, ObjectOp owner);

}
