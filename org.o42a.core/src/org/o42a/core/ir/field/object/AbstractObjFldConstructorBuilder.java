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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.field.object.ObjFldConfigureFn.OBJ_FLD_CONFIGURE;
import static org.o42a.core.ir.object.ObjectOp.objectAncestor;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE_OBJECT;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.core.ir.field.RefVmtRecord;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.CodeDirs;


abstract class AbstractObjFldConstructorBuilder
		implements FunctionBuilder<ObjectConstructorFn> {

	private final AbstractObjFld<?, ?> fld;

	AbstractObjFldConstructorBuilder(AbstractObjFld<?, ?> fld) {
		this.fld = fld;
	}

	public final Generator getGenerator() {
		return fld().getGenerator();
	}

	public final AbstractObjFld<?, ?> fld() {
		return this.fld;
	}

	@Override
	public void build(Function<ObjectConstructorFn> constructor) {

		final Block failure = constructor.addBlock("failure");
		final ObjBuilder builder = new ObjBuilder(
				constructor,
				failure.head(),
				fld().getObjectIR(),
				COMPATIBLE_OBJECT);
		final CodeDirs dirs = builder.dirs(constructor, failure.head());
		final CodeDirs subDirs =
				dirs.begin(
						RefVmtRecord.CONSTRUCT_ID,
						"Constructing field `" + fld().getField() + "`");

		buildConstructor(builder, subDirs);

		subDirs.done();

		if (failure.exists()) {
			failure.nullDataPtr().returnValue(failure);
		}
	}

	protected abstract void buildConstructor(
			ObjBuilder builder,
			CodeDirs dirs);

	final void configure(CodeDirs dirs, VmtIRChain.Op vmtc, ObjectOp object) {

		final Block code = dirs.code();

		getGenerator()
		.externalFunction()
		.link("o42a_fld_obj_configure", OBJ_FLD_CONFIGURE)
		.op(null, code)
		.configure(
				dirs,
				vmtc,
				object.ptr(),
				fld().vmtRecord().recordOffset().op(null, code));
	}

	final ObjectOp allocate(CodeDirs dirs, CtrOp ctr, boolean allocateObject) {
		ctr.ancestor(ancestor(dirs));

		final Block code = dirs.code();

		if (allocateObject) {
			ctr.allocateObject(dirs);
		}
		ctr.fillAncestor(code);

		final VmtIRChain.Op inheritedVmtc = ctr.getAncestor().vmtc(code);
		final ObjectOp object = ctr.object(code);

		object.objectData(code).ptr().vmtc(code).store(code, inheritedVmtc);

		return object;
	}

	private ObjectOp ancestor(CodeDirs dirs) {
		return objectAncestor(
				dirs,
				fld().getTarget(),
				tempObjHolder(dirs.getAllocator()));
	}

}
