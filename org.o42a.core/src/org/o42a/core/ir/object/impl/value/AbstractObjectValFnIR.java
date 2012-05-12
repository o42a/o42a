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
package org.o42a.core.ir.object.impl.value;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.impl.ObjectFnIR;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class AbstractObjectValFnIR<F extends ObjectFunc<F>>
		extends ObjectFnIR
		implements FunctionBuilder<F> {

	private final ObjectValueIR valueIR;
	private final CodeId id;
	private FuncPtr<F> funcPtr;
	private FuncRec<F> func;
	private byte reused;
	private DefValue constant;
	private DefValue finalValue;

	AbstractObjectValFnIR(ObjectValueIR valueIR) {
		super(valueIR.getObjectIR());
		this.valueIR = valueIR;
		this.id = getObjectIR().getId().setLocal(
				getGenerator().id().detail(suffix()));
	}

	public final ObjectValueIR getValueIR() {
		return this.valueIR;
	}

	public final CodeId getId() {
		return this.id;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return getObject().value().getValueStruct();
	}

	public final boolean isReused() {
		return this.reused > 0;
	}

	public final boolean isStub() {
		return this.reused == 2;
	}

	public final FuncPtr<F> get() {

		final FuncPtr<F> ptr = getNotStub();

		assert ptr != null :
			"Attempt to call a stub function: " + this;

		return this.funcPtr;
	}

	public final FuncPtr<F> getNotStub() {
		if (this.funcPtr == null) {
			create();
		}

		assert this.funcPtr != null :
			"Can't call " + this;

		return isStub() ? null : this.funcPtr;
	}

	public final DefValue getConstant() {
		if (this.constant != null) {
			return this.constant;
		}
		return this.constant = determineConstant();
	}

	public final DefValue getFinal() {
		if (this.finalValue != null) {
			return this.finalValue;
		}

		final DefValue constant = getConstant();

		if (isConstantValue(constant)) {
			return this.finalValue = constant;
		}

		return this.finalValue = determineFinal();
	}

	public void allocate(ObjectTypeIR typeIR) {
		this.func = func(typeIR.getObjectData());
		if (this.funcPtr == null) {
			create();
		}
		this.func.setConstant(true).setValue(this.funcPtr);
	}

	public final FuncPtr<F> get(ObjOp host) {

		final ObjectIR objectIR = host.getAscendant().ir(getGenerator());
		final ObjectTypeIR typeIR =
				objectIR.getBodyType().getObjectIR().getTypeIR();
		final ObjectIRData data = typeIR.getObjectData();

		return func(data).getValue().get();
	}

	public final void call(DefDirs dirs, ObjOp host, ObjectOp body) {

		final DefDirs subDirs = dirs.begin(
				null,
				"Calculate " + suffix() + " of " + getObjectIR().getId());
		final Block code = subDirs.code();

		if (body != null) {
			code.dumpName("For: ", body);
		}

		final DefValue finalValue = getFinal();

		if (!writeIfConstant(subDirs, finalValue)) {

			final F func = get(host).op(code.id(suffix()), code);
			final DataOp objectArg = objectArg(code, host, body);

			call(subDirs, func, objectArg);
		}

		subDirs.done();
	}

	@Override
	public final DataOp objectArg(Code code, ObjOp host, ObjectOp body) {
		if (isReused()) {
			return body != null
					? body.toData(null, code)
					: host.toData(null, code);
		}
		return super.objectArg(code, host, body);
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected abstract String suffix();

	protected abstract DefValue determineConstant();

	protected abstract DefValue determineFinal();

	protected abstract FuncRec<F> func(ObjectIRData data);

	protected final void set(Function<F> function) {
		this.funcPtr = function.getPointer();
		this.reused = -1;
	}

	protected final void reuse(FuncPtr<F> ptr) {
		this.funcPtr = ptr;
		this.reused = 1;
	}

	protected final void stub(FuncPtr<F> ptr) {
		this.funcPtr = ptr;
		this.reused = 2;
	}

	protected abstract void create();

	protected boolean writeIfConstant(DefDirs dirs, DefValue value) {
		if (!isConstantValue(value)) {
			return false;
		}

		final Block code = dirs.code();

		code.debug(suffix() + " = " + value.valueString());

		if (value.getCondition().isFalse()) {
			code.go(dirs.falseDir());
			return true;
		}
		if (!value.hasValue()) {
			return true;
		}

		final ValOp result =
				value.getValue().op(dirs.getBuilder(), code);

		result.go(code, dirs);
		dirs.returnValue(result);

		return true;
	}

	protected abstract void call(DefDirs subDirs, F func, DataOp objectArg);

	static boolean isConstantValue(DefValue value) {
		if (!value.getCondition().isConstant()) {
			return false;
		}
		if (!value.hasValue()) {
			return true;
		}
		return value.getValue().getKnowledge().isKnown();
	}

}
