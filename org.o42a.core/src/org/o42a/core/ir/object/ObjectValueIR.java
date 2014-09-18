/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.object;

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
import org.o42a.codegen.code.*;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Defs;
import org.o42a.core.object.value.ObjectValueDefs;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public final class ObjectValueIR {

	private final ObjectIR objectIR;
	private final ID id;
	private final Init<ObjectValueFnPtr> fnPtr = init(this::create);
	private FuncRec<ObjectValueFn> func;
	private final Init<DefValue> constant = init(this::determineConstant);
	private final Init<DefValue> finalValue = init(this::determineFinal);

	ObjectValueIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.id = getObjectIR().getId().setLocal(
				ID.id().detail(VALUE_ID));
	}

	public final ID getId() {
		return this.id;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final Obj getObject() {
		return getObjectIR().getObject();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final ValueType<?> getValueType() {
		return getObject().type().getValueType();
	}

	public final FuncPtr<ObjectValueFn> ptr() {

		final ObjectValueFnPtr fnPtr = fnPtr();

		assert !fnPtr.isStub() :
			"Attempt to call a stub function: " + this;

		return fnPtr.ptr();
	}

	public final DefValue getConstant() {
		return this.constant.get();
	}

	public final DefValue getFinal() {
		return this.finalValue.get();
	}

	public final ValueOp op(CodeBuilder builder, Code code) {
		return getObjectIR().op(builder, code).value();
	}

	public final void writeValue(DefDirs dirs, ObjOp host) {

		final DefDirs subDirs = dirs.begin(
				null,
				"Calculate value of " + getObjectIR().getId());

		if (!writeIfConstant(subDirs, getFinal())) {

			final ObjectValueFn fn = ptr().op(VALUE_ID, subDirs.code());

			fn.call(subDirs, getObjectIR().isExact() ? null : host);
		}

		subDirs.done();
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	final void allocate(ObjectDataIR dataIR) {
		this.func = dataIR.getInstance().valueFunc();
		this.func.setConstant(true).setValue(() -> fnPtr().ptr());
	}

	final ObjBuilder createBuilder(Function<ObjectValueFn> function) {

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

	final boolean writeIfConstant(DefDirs dirs, DefValue value) {
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

	final Defs defs() {
		return valueDefs().getDefs();
	}

	private final ObjectValueDefs valueDefs() {
		return getObject().value().valueDefs();
	}

	private final ObjectValueFnPtr fnPtr() {
		return this.fnPtr.get();
	}

	private final FuncPtr<ObjectValueFn> nonStubPtr() {

		final ObjectValueFnPtr fnPtr = fnPtr();

		return fnPtr.isStub() ? null : fnPtr.ptr();
	}

	private final ObjectValueFnPtr create() {
		if (canStub() && !getObject().value().isUsed(
				getGenerator().getAnalyzer(),
				ALL_VALUE_USAGES)) {
			return stub();
		}

		final ObjectValueFnPtr reused = reuse();

		if (reused != null) {
			return reused;
		}

		return create(getGenerator().newFunction().create(
				getId(),
				OBJECT_VALUE,
				new ObjectValueBuilder(this)));
	}

	private ObjectValueFnPtr reuse() {

		final DefValue finalValue = getFinal();

		if (isConstantValue(finalValue)) {
			if (finalValue.getCondition().isFalse()) {
				// Final value is false.
				return reuse("o42a_obj_value_false");
			}
			// Condition is true.
			if (!finalValue.hasValue()) {
				// Only condition present in value.
				if (finalValue.getCondition().isTrue()) {
					// Condition is unknown.
					// Do not update the value during calculation.
					return reuse("o42a_obj_value_unknown");
				}
				return null;
			}
			// Final value is known.
			if (getValueType().isVoid()) {
				// Value is void.
				return reuse("o42a_obj_value_void");
			}
		}
		if (getObject().value().getStatefulness().isEager()) {
			reuse("o42a_obj_value_eager");
		}
		if (getObjectIR().isExact()) {
			return null;
		}
		if (defs().areEmpty()) {
			return null;
		}

		final Def def = defs().get()[0];

		if (def.isExplicit()) {
			return null;
		}

		final Obj reuseFrom = def.getSource();
		final ObjectValueIR reuseFromIR =
				reuseFrom.ir(getGenerator()).getObjectValueIR();
		final FuncPtr<ObjectValueFn> reused = reuseFromIR.nonStubPtr();

		if (reused != null) {
			return reuse(reused);
		}

		return null;
	}

	private final ObjectValueFnPtr create(Function<ObjectValueFn> function) {
		return fnPtr(function.getPointer(), false);
	}

	private final ObjectValueFnPtr reuse(String name) {
		return fnPtr(name, false);
	}

	private final ObjectValueFnPtr reuse(FuncPtr<ObjectValueFn> funcPtr) {
		return fnPtr(funcPtr, false);
	}

	private final ObjectValueFnPtr stub() {
		return fnPtr("o42a_obj_value_stub", true);
	}

	private final ObjectValueFnPtr fnPtr(String name, boolean stub) {
		return fnPtr(
				getGenerator().externalFunction().link(name, OBJECT_VALUE),
				stub);
	}

	private static final ObjectValueFnPtr fnPtr(
			FuncPtr<ObjectValueFn> funcPtr,
			boolean stub) {
		return new ObjectValueFnPtr(funcPtr, stub);
	}

	private DefValue determineConstant() {

		final DefValue constant = defs().getConstant();

		if (!isConstantValue(constant)) {
			return constant;
		}
		if (!ancestorDefsUpdated()) {
			return constant;
		}

		return RUNTIME_DEF_VALUE;
	}

	private DefValue determineFinal() {

		final DefValue constant = getConstant();

		if (isConstantValue(constant)) {
			return constant;
		}
		if (isUsed() && !getObject().getConstructionMode().isPredefined()) {
			return RUNTIME_DEF_VALUE;
		}

		return defs().value(getObject().getScope().resolver());
	}

	private boolean canStub() {
		if (isUsed()) {
			return false;
		}
		return !getObject().getConstructionMode().isPredefined();
	}

	private static boolean isConstantValue(DefValue value) {
		if (!value.getCondition().isConstant()) {
			return false;
		}
		if (!value.hasValue()) {
			return true;
		}
		return value.getValue().getKnowledge().isKnown();
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

	private static final class ObjectValueFnPtr {

		private final FuncPtr<ObjectValueFn> ptr;
		private final boolean stub;

		ObjectValueFnPtr(FuncPtr<ObjectValueFn> ptr, boolean stub) {
			this.ptr = ptr;
			this.stub = stub;
		}

		public final FuncPtr<ObjectValueFn> ptr() {
			return this.ptr;
		}

		public final boolean isStub() {
			return this.stub;
		}

		@Override
		public String toString() {
			if (this.ptr == null) {
				return super.toString();
			}
			return this.ptr.toString();
		}

	}

}
