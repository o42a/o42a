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

import static org.o42a.core.ir.object.value.ObjectCondFn.OBJECT_COND;
import static org.o42a.core.ir.object.value.PredefObjValue.*;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.st.DefValue;
import org.o42a.util.string.ID;


public final class ObjectCondFnIR
		extends AbstractObjectValueFnIR<ObjectCondFn> {

	private static final ID SUFFIX = ID.id("condition");

	public ObjectCondFnIR(ObjectValueIR valueIR) {
		super(valueIR);
	}

	public final void call(CodeDirs dirs, ObjOp host) {

		final CodeDirs subDirs = dirs.begin(
				"cond",
				"Calculate condition of " + getObjectIR().getId());
		final Block code = subDirs.code();

		final DefValue finalValue = getFinal();

		if (!writeIfConstant(subDirs, finalValue)) {

			final ObjectCondFn func = get(host).op(suffix(), code);

			func.call(subDirs, getObjectIR().isExact() ? null : host);
		}

		subDirs.done();
	}

	@Override
	protected ID suffix() {
		return SUFFIX;
	}

	@Override
	protected FuncRec<ObjectCondFn> func(ObjectIRData data) {
		return data.condFunc();
	}

	@Override
	protected DefValue determineConstant() {
		return getValueIR().value().getConstant();
	}

	@Override
	protected DefValue determineFinal() {
		return getValueIR().value().getFinal();
	}

	@Override
	protected final ObjectSignature<ObjectCondFn> signature() {
		return OBJECT_COND;
	}

	@Override
	protected FunctionBuilder<ObjectCondFn> builder() {
		return new ObjectCondBuilder(this);
	}

	@Override
	protected boolean canStub() {
		return getValueIR().value().canStub();
	}

	@Override
	protected FuncPtr<ObjectCondFn> stubFunc() {
		return predefined(STUB_OBJ_VALUE);
	}

	@Override
	protected FuncPtr<ObjectCondFn> unknownValFunc() {
		return falseValFunc();
	}

	@Override
	protected FuncPtr<ObjectCondFn> falseValFunc() {
		return predefined(FALSE_OBJ_VALUE);
	}

	@Override
	protected FuncPtr<ObjectCondFn> voidValFunc() {
		return predefined(VOID_OBJ_VALUE);
	}

	@Override
	protected void reuse() {
		if (getObjectIR().isExact()) {
			return;
		}
		reuse(predefined(DEFAULT_OBJ_VALUE));
	}

	private FuncPtr<ObjectCondFn> predefined(PredefObjValue value) {
		return value.condFunction(
				getObject().getContext(),
				getGenerator(),
				getValueType());
	}

	private boolean writeIfConstant(CodeDirs dirs, DefValue value) {
		if (!isConstantValue(value)) {
			return false;
		}

		final Block code = dirs.code();

		code.debug(suffix() + " = " + value.getCondition());

		if (value.getCondition().isFalse()) {
			code.go(dirs.falseDir());
			return true;
		}

		return true;
	}

}
