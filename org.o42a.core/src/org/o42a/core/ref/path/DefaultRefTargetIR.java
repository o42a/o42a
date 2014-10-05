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
import org.o42a.codegen.code.op.DumpablePtrOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.dep.DepOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
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
	public RefTargetOp op(Code code, DepOp dep) {
		return new DefaultRefTargetOp(this, dep);
	}

	private static final class DefaultRefTargetOp implements RefTargetOp {

		private final DefaultRefTargetIR ir;
		private final DepOp dep;

		DefaultRefTargetOp(DefaultRefTargetIR ir, DepOp dep) {
			this.dep = dep;
			this.ir = ir;
		}

		@Override
		public final DumpablePtrOp<?> ptr() {
			return this.dep.ptr();
		}

		@Override
		public void storeTarget(CodeDirs dirs, HostOp host) {

			final Block noDep = dirs.addBlock("no_dep");
			final CodeDirs depDirs =
					dirs.getBuilder().dirs(dirs.code(), noDep.head());

			final DataOp object = createObject(depDirs, host);
			final Block code = depDirs.code();

			this.dep.dep().object(code).store(code, object);

			if (noDep.exists()) {

				final CodeBuilder builder = dirs.getBuilder();
				final ObjectIR noneIR =
						builder.getContext().getNone().ir(
								builder.getGenerator());

				this.dep.dep().object(noDep).store(
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
					store.loadTarget(dirs).target().materialize(
							dirs,
							tempObjHolder(dirs.getAllocator()));

			this.dep.dep().object(code).store(code, object.toData(null, code));
		}

		@Override
		public ObjectOp loadTarget(CodeDirs dirs) {

			final Block code = dirs.code();

			return anonymousObject(
					dirs,
					this.dep.dep().object(code).load(null, code),
					this.ir.refIR.ref().getResolution().resolveTarget());
		}

		@Override
		public String toString() {
			if (this.dep == null) {
				return super.toString();
			}
			return this.dep.toString();
		}

		private DataOp createObject(CodeDirs dirs, HostOp host) {

			final HostTargetOp target = this.ir.step.op(host).target();

			return target.materialize(dirs, tempObjHolder(dirs.getAllocator()))
					.toData(null, dirs.code());
		}

	}

}
