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

import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValHolderFactory.NO_VAL_HOLDER;
import static org.o42a.core.ir.value.ValOp.VALUE_ID;
import static org.o42a.core.object.value.ValuePartUsage.ALL_VALUE_PART_USAGES;
import static org.o42a.core.object.value.ValueUsage.ALL_VALUE_USAGES;
import static org.o42a.core.st.DefValue.RUNTIME_DEF_VALUE;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Defs;
import org.o42a.core.object.value.ObjectValueDefs;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public final class ObjectValueFnIR {

	private final ObjectValueIR valueIR;
	private final ID id;
	private FuncPtr<ObjectValueFn> funcPtr;
	private FuncRec<ObjectValueFn> func;
	private byte reused;
	private final Init<DefValue> constant = init(this::determineConstant);
	private final Init<DefValue> finalValue = init(this::determineFinal);

	public ObjectValueFnIR(ObjectValueIR valueIR) {
		this.valueIR = valueIR;
		this.id = getObjectIR().getId().setLocal(
				ID.id().detail(VALUE_ID));
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return getValueIR().getObjectIR();
	}

	public final Obj getObject() {
		return getObjectIR().getObject();
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

	public final ObjectValueDefs valueDefs() {
		return getObject().value().valueDefs();
	}

	public final Defs defs() {
		return valueDefs().getDefs();
	}

	public final boolean isReused() {
		return this.reused > 0;
	}

	public final boolean isStub() {
		return this.reused == 2;
	}

	public final FuncPtr<ObjectValueFn> get() {

		final FuncPtr<ObjectValueFn> ptr = getNotStub();

		assert ptr != null :
			"Attempt to call a stub function: " + this;

		return this.funcPtr;
	}

	public final FuncPtr<ObjectValueFn> getNotStub() {
		if (this.funcPtr == null) {
			create();
		}

		assert this.funcPtr != null :
			"Can't call " + this;

		return isStub() ? null : this.funcPtr;
	}

	public final DefValue getConstant() {
		return this.constant.get();
	}

	public final DefValue getFinal() {
		return this.finalValue.get();
	}

	public void allocate(ObjectDataIR dataIR) {
		this.func = func(dataIR.getInstance());
		if (this.funcPtr == null) {
			create();
		}
		this.func.setConstant(true).setValue(this.funcPtr);
	}

	public final FuncPtr<ObjectValueFn> get(ObjOp host) {

		final ObjectIR objectIR = host.getAscendant().ir(getGenerator());
		final ObjectIRData data = objectIR.getDataIR().getInstance();

		return func(data).getValue().get();
	}

	public final void call(DefDirs dirs, ObjOp host) {

		final DefDirs subDirs = dirs.begin(
				null,
				"Calculate value of " + getObjectIR().getId());
		final Block code = subDirs.code();

		if (!writeIfConstant(subDirs, getFinal())) {

			final ObjectValueFn func = get(host).op(VALUE_ID, code);

			func.call(subDirs, getObjectIR().isExact() ? null : host);
		}

		subDirs.done();
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected FuncRec<ObjectValueFn> func(ObjectIRData data) {
		return data.valueFunc();
	}

	protected final void set(Function<ObjectValueFn> function) {
		this.funcPtr = function.getPointer();
		this.reused = -1;
	}

	protected final void reuse(String name) {
		reuse(name, 1);
	}

	protected final void reuse(FuncPtr<ObjectValueFn> funcPtr) {
		reuse(funcPtr, 1);
	}

	protected final void stub(String name) {
		reuse(name, 2);
	}

	protected final void reuse(String name, int mode) {
		reuse(getGenerator().externalFunction().link(name, OBJECT_VALUE), mode);
	}

	protected final void reuse(FuncPtr<ObjectValueFn> funcPtr, int mode) {
		this.funcPtr = funcPtr;
		this.reused = (byte) mode;
	}

	protected final void create() {
		if (canStub() && !getObject().value().isUsed(
				getGenerator().getAnalyzer(),
				ALL_VALUE_USAGES)) {
			stub("o42a_obj_value_stub");
			return;
		}

		reuse();
		if (isReused()) {
			return;
		}

		set(getGenerator().newFunction().create(
				getId(),
				OBJECT_VALUE,
				new ObjectValueBuilder(this)));
	}

	protected DefValue determineConstant() {

		final DefValue constant = defs().getConstant();

		if (!isConstantValue(constant)) {
			return constant;
		}
		if (!ancestorDefsUpdated()) {
			return constant;
		}

		return RUNTIME_DEF_VALUE;
	}

	protected DefValue determineFinal() {

		final DefValue constant = getConstant();

		if (isConstantValue(constant)) {
			return constant;
		}
		if (isUsed() && !getObject().getConstructionMode().isPredefined()) {
			return RUNTIME_DEF_VALUE;
		}

		return defs().value(getObject().getScope().resolver());
	}

	protected boolean canStub() {
		if (isUsed()) {
			return false;
		}
		return !getObject().getConstructionMode().isPredefined();
	}

	protected boolean writeIfConstant(DefDirs dirs, DefValue value) {
		if (!isConstantValue(value)) {
			return false;
		}

		final Block code = dirs.code();

		code.debug("Value = " + value.valueString());

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

	protected void reuse() {

		final DefValue finalValue = getFinal();

		if (isConstantValue(finalValue)) {
			if (finalValue.getCondition().isFalse()) {
				// Final value is false.
				reuse("o42a_obj_value_false");
				return;
			}
			// Condition is true.
			if (!finalValue.hasValue()) {
				// Only condition present in value.
				if (finalValue.getCondition().isTrue()) {
					// Condition is unknown.
					// Do not update the value during calculation.
					reuse("o42a_obj_value_unknown");
				}
				return;
			}
			// Final value is known.
			if (getValueType().isVoid()) {
				// Value is void.
				reuse("o42a_obj_value_void");
				return;
			}
		}
		if (getObject().value().getStatefulness().isEager()) {
			reuse("o42a_obj_value_eager");
		}
		if (getObjectIR().isExact()) {
			return;
		}
		if (defs().areEmpty()) {
			return;
		}

		final Def def = defs().get()[0];

		if (def.isExplicit()) {
			return;
		}

		final Obj reuseFrom = def.getSource();
		final ObjectValueIR reuseFromIR =
				reuseFrom.ir(getGenerator()).getObjectValueIR();
		final FuncPtr<ObjectValueFn> reused = reuseFromIR.value().getNotStub();

		if (reused != null) {
			reuse(reused);
		}
	}

	ObjBuilder createBuilder(Function<ObjectValueFn> function) {

		final Block failure = function.addBlock("failure");
		final ObjBuilder builder =
				new ObjBuilder(
				function,
				failure.head(),
				getObjectIR(),
				DERIVED);

		if (failure.exists()) {
			failure.debug("Failure");

			// No need to hold a false value.
			final ValOp result =
					function.arg(failure, OBJECT_VALUE.value())
					.op(function, builder, getValueType(), NO_VAL_HOLDER);

			result.storeFalse(failure);
			failure.returnVoid();
		}

		return builder;
	}

	private boolean isUsed() {
		return valueDefs().isUsed(
				getGenerator().getAnalyzer(),
				ALL_VALUE_PART_USAGES);
	}

	private boolean rtUsed() {
		return getObject().value().isUsed(
				getGenerator().getAnalyzer(),
				ALL_VALUE_USAGES);
	}

	private boolean ancestorDefsUpdated() {
		if (rtUsed()) {
			return true;
		}
		return valueDefs().ancestorDefsUpdates().isUsed(
				getGenerator().getAnalyzer(),
				ALL_SIMPLE_USAGES);
	}

}
