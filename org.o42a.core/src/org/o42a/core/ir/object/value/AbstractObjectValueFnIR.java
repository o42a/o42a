/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.object.value.ValueUsage.ALL_VALUE_USAGES;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.impl.ObjectFnIR;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public abstract class AbstractObjectValueFnIR<F extends ObjectFunc<F>>
		extends ObjectFnIR {

	private final ObjectValueIR valueIR;
	private final ID id;
	private FuncPtr<F> funcPtr;
	private FuncRec<F> func;
	private byte reused;
	private DefValue constant;
	private DefValue finalValue;

	AbstractObjectValueFnIR(ObjectValueIR valueIR) {
		super(valueIR.getObjectIR());
		this.valueIR = valueIR;
		this.id = getObjectIR().getId().setLocal(
				ID.id().detail(suffix()));
	}

	public final ObjectValueIR getValueIR() {
		return this.valueIR;
	}

	public final ID getId() {
		return this.id;
	}

	public final ValueType<?> getValueType() {
		return getObject().type().getValueType();
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

	public void allocate(ObjectDataIR dataIR) {
		this.func = func(dataIR.getInstance());
		if (this.funcPtr == null) {
			create();
		}
		this.func.setConstant(true).setValue(this.funcPtr);
	}

	public final FuncPtr<F> get(ObjOp host) {

		final ObjectIR objectIR = host.getAscendant().ir(getGenerator());
		final ObjectDataIR typeIR =
				objectIR.getBodyType().getObjectIR().getDataIR();
		final ObjectIRData data = typeIR.getInstance();

		return func(data).getValue().get();
	}

	@Override
	public final DataOp objectArg(Code code, ObjOp host) {
		if (isReused()) {
			return host.toData(null, code);
		}
		return super.objectArg(code, host);
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected abstract ID suffix();

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

	protected final void create() {
		if (canStub() && !getObject().value().isUsed(
				getGenerator().getAnalyzer(),
				ALL_VALUE_USAGES)) {
			stub(stubFunc());
			return;
		}

		final DefValue finalValue = getFinal();

		if (isConstantValue(finalValue)) {
			if (finalValue.getCondition().isFalse()) {
				// Final value is false.
				reuse(falseValFunc());
				return;
			}
			// Condition is true.
			if (!finalValue.hasValue()) {
				// Only condition present in value.
				if (finalValue.getCondition().isTrue()) {
					// Condition is unknown.
					// Do not update the value during calculation.
					reuse(unknownValFunc());
				}
				return;
			}
			// Final value is known.
			if (getValueType().isVoid()) {
				// Value is void.
				reuse(voidValFunc());
				return;
			}
		}

		reuse();
		if (isReused()) {
			return;
		}

		set(getGenerator()
				.newFunction()
				.create(getId(), signature(), builder()));
	}

	protected abstract ObjectSignature<F> signature();

	protected abstract FunctionBuilder<F> builder();

	protected abstract boolean canStub();

	protected abstract FuncPtr<F> stubFunc();

	protected abstract FuncPtr<F> unknownValFunc();

	protected abstract FuncPtr<F> falseValFunc();

	protected abstract FuncPtr<F> voidValFunc();

	protected abstract void reuse();

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
