/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.object.op.ObjectRefFn.OBJECT_REF;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjectRefFn;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


public abstract class ObjectRefFld<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>>
				extends RefFld<F, T, ObjectRefFn> {

	private FuncPtr<ObjectRefFn> cloneFunc;

	public ObjectRefFld(Field field, Obj target) {
		super(field, target);
	}

	@Override
	protected final ObjectRefFn.Signature getConstructorSignature() {
		return OBJECT_REF;
	}

	@Override
	protected FuncPtr<ObjectRefFn> cloneFunc() {
		if (this.cloneFunc != null) {
			return this.cloneFunc;
		}
		return this.cloneFunc = getGenerator().newFunction().create(
				getField().getId().detail(CLONE_ID),
				getConstructorSignature(),
				new ConstructorBuilder(this::buildCloneFunc)).getPointer();
	}

	private void buildCloneFunc(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjOp host = builder.host();
		final VmtIRChain.Op vmtc = builder.getFunction().arg(
				code,
				getConstructorSignature().vmtc());
		final VmtIRChain.Op prevVmtc = vmtc.prev(null, code).load(null, code);
		final CondBlock refer =
				prevVmtc.isNull(null, code)
				.branch(code, "refer", "delegate");

		constructor()
		.op(null, refer)
		.call(refer, host, vmtc)
		.returnValue(refer);

		final Block delegate = refer.otherwise();
		final VmtIROp prevVmt =
				prevVmtc.loadVmt(delegate, getObjectIR().getVmtIR());

		prevVmt.compatible(delegate).goUnless(delegate, refer.head());
		prevVmt.func(null, delegate, vmtConstructor())
		.load(null, delegate)
		.call(delegate, host, prevVmtc)
		.returnValue(delegate);
	}

}
