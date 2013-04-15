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
package org.o42a.core.ref.path;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.object.state.DepIR.DEP_IR;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.state.DepIR;
import org.o42a.core.ir.object.state.DepIR.Op;
import org.o42a.core.ir.object.state.DepIR.Type;
import org.o42a.core.ir.op.*;
import org.o42a.util.string.ID;


final class DefaultRefTargetIR implements RefTargetIR {

	private final RefIR refIR;
	private final Step step;
	private Type instance;

	DefaultRefTargetIR(RefIR refIR, Step step) {
		this.refIR = refIR;
		this.step = step;
	}

	@Override
	public DepIR.Op ptr(Code code, StructOp<?> data) {
		return data.struct(null, code, this.instance);
	}

	@Override
	public Data<?> allocate(ID id, SubData<?> data) {
		this.instance = data.addInstance(id, DEP_IR);
		this.instance.object().setNull();
		return this.instance.data(data.getGenerator());
	}

	@Override
	public void storeTarget(CodeDirs dirs, PathOp start, StructOp<?> data) {

		final Op ptr = ptr(dirs.code(), data);
		final DataRecOp objectRec = ptr.object(dirs.code());
		final Block noDep = dirs.addBlock("no_dep");
		final CodeDirs depDirs =
				dirs.getBuilder().dirs(dirs.code(), noDep.head());

		final DataOp object = createObject(depDirs, start);
		final Block code = depDirs.code();

		objectRec.store(code, object);

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
	public TargetOp loadTarget(CodeDirs dirs, StructOp<?> data) {

		final Block code = dirs.code();
		final Op ptr = ptr(code, data);

		return anonymousObject(
				dirs.getBuilder(),
				ptr.object(code).load(null, code),
				this.refIR.ref().getResolution().resolveTarget());
	}

	private DataOp createObject(CodeDirs dirs, PathOp start) {

		final HostTargetOp target = this.step.op(start).target();

		return target.materialize(dirs, tempObjHolder(dirs.getAllocator()))
				.toData(null, dirs.code());
	}

}
