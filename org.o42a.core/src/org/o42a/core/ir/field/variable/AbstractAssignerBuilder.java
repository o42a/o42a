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
package org.o42a.core.ir.field.variable;

import static org.o42a.core.ir.field.variable.AssignerFld.CAST_TARGET_ID;
import static org.o42a.core.ir.field.variable.VariableAssignerFunc.VARIABLE_ASSIGNER;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;


abstract class AbstractAssignerBuilder<F extends FldOp>
		implements FunctionBuilder<VariableAssignerFunc> {

	@Override
	public void build(Function<VariableAssignerFunc> assigner) {

		final Block failure = assigner.addBlock("failure");
		final ObjBuilder builder = new ObjBuilder(
				assigner,
				failure.head(),
				getBodyIR(),
				getBodyIR().getAscendant(),
				getBodyIR().getObjectIR().isExact() ? EXACT : COMPATIBLE);
		final CodeDirs dirs =
				builder.dirs(assigner, failure.head());

		final F fld = op(assigner, builder.host());
		final TypeRef typeRef = getTypeRef();
		final Obj typeObject = typeRef.getType();
		final RefOp boundRef = typeRef.op(builder.host());
		final ObjectTypeOp bound =
				boundRef.target(dirs)
				.materialize(dirs, tempObjHolder(dirs.getAllocator()))
				.objectType(assigner);

		storeBound(assigner, fld, bound.ptr());

		final ObjectOp valueObject = anonymousObject(
				builder,
				assigner.arg(assigner, VARIABLE_ASSIGNER.value()),
				builder.getContext().getVoid());

		final ObjectOp castObject = valueObject.dynamicCast(
				CAST_TARGET_ID,
				dirs,
				typeObject.ir(assigner.getGenerator())
				.getTypeIR().op(builder, assigner),
				typeObject,
				true);

		// Evaluate the condition prior to assigning to the target
		// to prevent infinite looping in situations like this:
		// I := I + 1
		castObject.value().writeCond(dirs);
		storeObject(assigner, fld, castObject);
		assigner.bool(true).returnValue(assigner);

		if (failure.exists()) {

			final ObjectIR noneIR =
					builder.getContext().getNone().ir(assigner.getGenerator());

			storeBound(
					failure,
					fld,
					noneIR.getTypeIR()
					.getInstance()
					.pointer(assigner.getGenerator())
					.op(null, failure));
			storeObject(failure, fld, noneIR.op(builder, failure));

			failure.bool(false).returnValue(failure);
		}
	}

	protected abstract TypeRef getTypeRef();

	protected abstract ObjectIRBody getBodyIR();

	protected abstract F op(Code code, ObjOp host);

	protected abstract void storeBound(Code code, F fld, ObjectIRTypeOp bound);

	protected abstract void storeObject(Block code, F fld, ObjectOp object);

}
