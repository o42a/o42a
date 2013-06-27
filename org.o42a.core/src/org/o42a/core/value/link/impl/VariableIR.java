/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.value.link.impl;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.type.*;
import org.o42a.core.object.Obj;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;


final class VariableIR extends ValueIR {

	VariableIR(LinkValueTypeIR valueStructIR, ObjectIR objectIR) {
		super(valueStructIR, objectIR);
	}

	@Override
	public void allocateBody(ObjectIRBodyData data) {
	}

	@Override
	public void setInitialValue(ObjectTypeIR type) {

		final Obj object = type.getObjectIR().getObject();
		final Value<?> value = object.value().getValue();
		final ValType objectVal = type.getInstance().data().value();

		if (!value.getKnowledge().isInitiallyKnown()) {
			objectVal.set(INDEFINITE_VAL);
		} else if (value.getKnowledge().isFalse()) {
			objectVal.set(FALSE_VAL);
		} else {

			final LinkValueType linkType = getValueType().toLinkType();
			final KnownLink link =
					linkType.cast(value).getCompilerValue();
			final Obj target = link.getTarget().getWrapped();

			if (target.getConstructionMode().isRuntime()) {
				objectVal.set(INDEFINITE_VAL);
			} else {

				final ObjectIR targetIR = target.ir(getGenerator());

				objectVal.flags().setValue(VAL_CONDITION);
				objectVal.length().setValue(0);
				objectVal.value().setNativePtr(
						targetIR.getMainBodyIR()
						.pointer(getGenerator())
						.toAny());
			}
		}
	}

	@Override
	public ValueOp op(ObjectOp object) {
		return new VariableOp(this, object);
	}

	private static final class VariableOp extends StatefulValueOp {

		VariableOp(VariableIR variableIR, ObjectOp object) {
			super(variableIR, object);
		}

		@Override
		public StateOp state(CodeDirs dirs) {
			return new VarStateOp(object());
		}

	}

	private static final class VarStateOp extends StateOp {

		VarStateOp(ObjectOp host) {
			super(host);
		}

		@Override
		public void init(Block code, ValOp value) {

			final DataOp target =
					value.value(null, code)
					.toDataRec(null, code)
					.load(null, code);

			value().rawValue(null, code)
					.toAny(null, code)
					.toDataRec(null, code)
					.store(code, target, ATOMIC);

			code.releaseBarrier();

			flags().store(code, VAL_CONDITION);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {

			final Block code = dirs.code();

			start(code);
			tempObjHolder(code.getAllocator()).holdVolatile(code, host());

			final ObjectOp valueObject =
					value.target()
					.materialize(dirs, tempObjHolder(code.getAllocator()));

			value().rawValue(null, code)
					.toAny(null, code)
					.toDataRec(null, code)
					.store(
							code,
							valueObject.toData(null, code),
							ATOMIC);
			code.releaseBarrier();

			flags().store(code, VAL_CONDITION);
			code.dump("Assigned: ", value());
		}

	}

}
