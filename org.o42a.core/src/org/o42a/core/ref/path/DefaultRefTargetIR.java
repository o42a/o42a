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
package org.o42a.core.ref.path;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.ir.op.*;


final class DefaultRefTargetIR implements RefTargetIR {

	private final RefIR refIR;
	private final Step step;

	DefaultRefTargetIR(RefIR refIR, Step step) {
		this.refIR = refIR;
		this.step = step;
	}

	@Override
	public boolean isOmitted() {
		return false;
	}

	@Override
	public RefTargetOp op(Code code, DepIR depIR, DataRecOp data) {

		final DataRecOp ptr =
				data.offset(null, code, code.int32(depIR.getIndex()));

		return new DefaultRefTargetOp(this, ptr);
	}

	private static final class DefaultRefTargetOp implements RefTargetOp {

		private final DefaultRefTargetIR ir;
		private final DataRecOp ptr;

		DefaultRefTargetOp(DefaultRefTargetIR ir, DataRecOp ptr) {
			this.ptr = ptr;
			this.ir = ir;
		}

		@Override
		public final DataRecOp ptr() {
			return this.ptr;
		}

		@Override
		public void storeTarget(CodeDirs dirs, HostOp host) {

			final Block noDep = dirs.addBlock("no_dep");
			final CodeDirs depDirs =
					dirs.getBuilder().dirs(dirs.code(), noDep.head());

			final DataOp object = createObject(depDirs, host);
			final Block code = depDirs.code();

			ptr().store(code, object);

			if (noDep.exists()) {

				final CodeBuilder builder = dirs.getBuilder();
				final ObjectIR noneIR =
						builder.getContext().getNone().ir(
								builder.getGenerator());

				ptr().store(
						noDep,
						noneIR.op(builder, noDep).toData(null, noDep));
				noDep.go(code.tail());
			}

			depDirs.done();
		}

		@Override
		public void copyTarget(CodeDirs dirs, TargetStoreOp store) {

			final Block code = dirs.code();
			final ObjectOp object =
					store.loadTarget(dirs).materialize(dirs, tempObjHolder(dirs.getAllocator()));

			ptr().store(code, object.toData(null, code));
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {

			final Block code = dirs.code();

			return anonymousObject(
					dirs.getBuilder(),
					ptr().load(null, code),
					this.ir.refIR.ref().getResolution().resolveTarget());
		}

		@Override
		public String toString() {
			if (this.ptr == null) {
				return super.toString();
			}
			return this.ptr.toString();
		}

		private DataOp createObject(CodeDirs dirs, HostOp host) {

			final HostTargetOp target = this.ir.step.op(host).target();

			return target.materialize(dirs, tempObjHolder(dirs.getAllocator()))
					.toData(null, dirs.code());
		}

	}

}
