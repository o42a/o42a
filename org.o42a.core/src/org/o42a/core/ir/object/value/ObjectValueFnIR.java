/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.object.value.PredefObjValue.*;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.DefValue;
import org.o42a.util.string.ID;


public final class ObjectValueFnIR
		extends AbstractObjectValueFnIR<ObjectValueFunc> {

	public ObjectValueFnIR(ObjectValueIR valueIR) {
		super(valueIR);
	}

	public final void call(DefDirs dirs, ObjOp host) {

		final DefDirs subDirs = dirs.begin(
				null,
				"Calculate value of " + getObjectIR().getId());
		final Block code = subDirs.code();

		if (!writeIfConstant(subDirs, getFinal())) {

			final ObjectValueFunc func = get(host).op(suffix(), code);

			func.call(subDirs, getObjectIR().isExact() ? null : host);
		}

		subDirs.done();
	}

	@Override
	protected ID suffix() {
		return ValOp.VALUE_ID;
	}

	@Override
	protected FuncRec<ObjectValueFunc> func(ObjectIRData data) {
		return data.valueFunc();
	}

	@Override
	protected DefValue determineConstant() {
		return getValueIR().defs().getConstant();
	}

	@Override
	protected DefValue determineFinal() {
		return getValueIR().defs().getFinal();
	}

	@Override
	protected ObjectSignature<ObjectValueFunc> signature() {
		return OBJECT_VALUE;
	}

	@Override
	protected FunctionBuilder<ObjectValueFunc> builder() {
		return new ObjectValueBuilder(this);
	}

	@Override
	protected boolean canStub() {
		return getValueIR().defs().canStub();
	}

	@Override
	protected FuncPtr<ObjectValueFunc> stubFunc() {
		return predefined(STUB_OBJ_VALUE);
	}

	@Override
	protected FuncPtr<ObjectValueFunc> unknownValFunc() {
		return falseValFunc();
	}

	@Override
	protected FuncPtr<ObjectValueFunc> falseValFunc() {
		return predefined(FALSE_OBJ_VALUE);
	}

	@Override
	protected FuncPtr<ObjectValueFunc> voidValFunc() {
		return predefined(VOID_OBJ_VALUE);
	}

	@Override
	protected void reuse() {
		if (getObjectIR().isExact()) {
			return;
		}
		reuse(predefined(DEFAULT_OBJ_VALUE));
	}

	private FuncPtr<ObjectValueFunc> predefined(PredefObjValue value) {
		return value.valueFunction(
				getObject().getContext(),
				getGenerator(),
				getValueType(),
				getObject().value().getStatefulness().isStateful());
	}

}
