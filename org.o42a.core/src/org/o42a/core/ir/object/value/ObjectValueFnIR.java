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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.object.value.PredefObjValue.*;

import org.o42a.codegen.code.*;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.ObjectIRData.Op;
import org.o42a.core.ir.op.ObjectSignature;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueStruct;


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
	protected FunctionBuilder<ObjectValueFunc> builder() {
		return new ObjectValueBuilder();
	}

	@Override
	protected boolean canStub() {
		return getValueIR().claim().canStub()
				&& getValueIR().proposition().canStub();
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
		return value.get(
				getObject().getContext(),
				getGenerator(),
				getValueType());
	}

	private final class ObjectValueBuilder extends AbstractObjectValueBuilder {

		@Override
		public String toString() {
			return String.valueOf(ObjectValueFnIR.this);
		}

		@Override
		protected ValueStruct<?, ?> getValueStruct() {
			return ObjectValueFnIR.this.getValueStruct();
		}

		@Override
		protected ObjBuilder createBuilder(
				Function<ObjectValueFunc> function,
				CodePos failureDir) {
			return new ObjBuilder(
					function,
					failureDir,
					getObjectIR().getMainBodyIR(),
					getObjectIR().getObject(),
					getObjectIR().isExact() ? EXACT : DERIVED);
		}

		@Override
		protected Op data(Code code, Function<ObjectValueFunc> function) {
			if (!getObjectIR().isExact()) {
				return function.arg(code, OBJECT_VALUE.data());
			}
			return getObjectIR()
					.getTypeIR()
					.getObjectData()
					.pointer(getGenerator())
					.op(code.id("data"), code);
		}

		@Override
		protected void writeValue(DefDirs dirs, ObjOp host, Op data) {
			if (!getObjectIR().isExact()) {
				dirs.code().debug("Exact host: " + getObjectIR().getId());
			} else {
				dirs.code().dumpName("Host: ", host);
			}

			getValueIR().writeClaim(dirs, host, null);
			getValueIR().writeProposition(dirs, host, null);
		}

	}

}
