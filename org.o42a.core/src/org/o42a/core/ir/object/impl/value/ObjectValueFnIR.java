/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ir.object.impl.value;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.object.impl.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.object.impl.value.PredefObjValue.FALSE_OBJ_VALUE;
import static org.o42a.core.ir.object.impl.value.PredefObjValue.STUB_OBJ_VALUE;
import static org.o42a.core.ir.object.impl.value.PredefObjValue.VOID_OBJ_VALUE;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;
import static org.o42a.core.object.value.ValueUsage.ALL_VALUE_USAGES;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ObjectSignature;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.DefValue;


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

		final DefValue finalValue = getFinal();

		if (!writeIfConstant(subDirs, finalValue)) {

			final ObjectValueFunc func = get(host).op(code.id(suffix()), code);

			func.call(subDirs, getObjectIR().isExact() ? null : host);
		}

		subDirs.done();
	}

	@Override
	public void build(Function<ObjectValueFunc> function) {
		if (isReused()) {
			return;
		}

		function.debug("Calculating value");

		final Block failure = function.addBlock("failure");
		final Block done = function.addBlock("done");
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				getObjectIR().isExact() ? EXACT : DERIVED);

		final ValOp result =
				function.arg(function, OBJECT_VALUE.value())
				.op(builder, getValueStruct())
				.setStoreMode(INITIAL_VAL_STORE);
		final ObjOp host = builder.host();
		/*final ObjectIRData.Op data;

		if (getObjectIR().isExact()) {
			data =
					getObjectIR()
					.getTypeIR()
					.getObjectData()
					.pointer(getGenerator())
					.op(function.id("data"), function);
		} else {
			data = function.arg(function, OBJECT_VALUE.data());
		}*/

		final DefDirs dirs =
				builder.dirs(function, failure.head())
				.value(result)
				.def(done.head());

		dirs.code().dumpName("Host: ", host);
		getValueIR().writeClaim(dirs, host, null);
		getValueIR().writeProposition(dirs, host, null);

		final Block code = dirs.done().code();

		if (code.exists()) {
			code.debug("Indefinite");
			code.returnVoid();
		}
		if (failure.exists()) {
			failure.debug("False");
			result.storeFalse(failure);
			failure.returnVoid();
		}
		if (done.exists()) {
			result.store(done, dirs.result());
			done.returnVoid();
		}
	}

	@Override
	protected String suffix() {
		return "value";
	}

	@Override
	protected FuncRec<ObjectValueFunc> func(ObjectIRData data) {
		return data.valueFunc();
	}

	@Override
	protected DefValue determineConstant() {

		final DefValue claim = getValueIR().claim().getConstant();

		if (claim.hasValue() || !claim.getCondition().isTrue()) {
			return claim;
		}

		return getValueIR().proposition().getConstant();
	}

	@Override
	protected DefValue determineFinal() {

		final DefValue claim = getValueIR().claim().getFinal();

		if (claim.hasValue() || !claim.getCondition().isTrue()) {
			return claim;
		}

		return getValueIR().proposition().getFinal();
	}

	@Override
	protected ObjectSignature<ObjectValueFunc> signature() {
		return OBJECT_VALUE;
	}

	@Override
	protected boolean canStub() {
		return getValueIR().claim().canStub()
				&& getValueIR().proposition().canStub()
				&& !getObject().value().isUsed(
						getGenerator().getAnalyzer(),
						ALL_VALUE_USAGES);
	}

	@Override
	protected FuncPtr<ObjectValueFunc> stubFunc() {
		return STUB_OBJ_VALUE.get(
				getObject().getContext(),
				getGenerator());
	}

	@Override
	protected FuncPtr<ObjectValueFunc> unknownValFunc() {
		return falseValFunc();
	}

	@Override
	protected FuncPtr<ObjectValueFunc> falseValFunc() {
		return FALSE_OBJ_VALUE.get(
				getObject().getContext(),
				getGenerator());
	}

	@Override
	protected FuncPtr<ObjectValueFunc> voidValFunc() {
		return VOID_OBJ_VALUE.get(
				getObject().getContext(),
				getGenerator());
	}

	@Override
	protected void reuse() {
	}

}
