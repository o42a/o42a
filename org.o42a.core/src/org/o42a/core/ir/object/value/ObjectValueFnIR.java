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

import static org.o42a.core.ir.object.value.PredefObjValue.*;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.DefValue;
import org.o42a.util.string.ID;


public final class ObjectValueFnIR
		extends AbstractObjectValueFnIR<ObjectValueFn> {

	public ObjectValueFnIR(ObjectValueIR valueIR) {
		super(valueIR);
	}

	public final void call(DefDirs dirs, ObjOp host) {

		final DefDirs subDirs = dirs.begin(
				null,
				"Calculate value of " + getObjectIR().getId());
		final Block code = subDirs.code();

		if (!writeIfConstant(subDirs, getFinal())) {

			final ObjectValueFn func = get(host).op(suffix(), code);

			func.call(subDirs, getObjectIR().isExact() ? null : host);
		}

		subDirs.done();
	}

	@Override
	protected ID suffix() {
		return ValOp.VALUE_ID;
	}

	@Override
	protected FuncRec<ObjectValueFn> func(ObjectIRData data) {
		return data.valueFunc();
	}

	@Override
	protected DefValue determineConstant() {
		return getValueIR().def().getConstant();
	}

	@Override
	protected DefValue determineFinal() {
		return getValueIR().def().getFinal();
	}

	@Override
	protected ObjectValueFn.Signature signature() {
		return OBJECT_VALUE;
	}

	@Override
	protected FunctionBuilder<ObjectValueFn> builder() {
		return new ObjectValueBuilder(this);
	}

	@Override
	protected boolean canStub() {
		return getValueIR().def().canStub();
	}

	@Override
	protected FuncPtr<ObjectValueFn> stubFunc() {
		return predefined(STUB_OBJ_VALUE);
	}

	@Override
	protected FuncPtr<ObjectValueFn> unknownValFunc() {
		return falseValFunc();
	}

	@Override
	protected FuncPtr<ObjectValueFn> falseValFunc() {
		return predefined(FALSE_OBJ_VALUE);
	}

	@Override
	protected FuncPtr<ObjectValueFn> voidValFunc() {
		return predefined(VOID_OBJ_VALUE);
	}

	@Override
	protected void reuse() {
		if (getObjectIR().isExact()) {
			return;
		}
		reuse(predefined(DEFAULT_OBJ_VALUE));
	}

	private FuncPtr<ObjectValueFn> predefined(PredefObjValue value) {
		return value.valueFunction(
				getObject().getContext(),
				getGenerator(),
				getValueType());
	}

}
