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
import static org.o42a.core.ir.object.ObjectPrecision.APPROXIMATE_OBJECT;
import static org.o42a.core.ir.object.ObjectValueIRKind.*;
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
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.op.ObjectFn;
import org.o42a.core.ir.op.OpPresets;
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

		assert !fnPtr.getKind().isStub() :
			"Attempt to call a stub function: " + this;

		return fnPtr.ptr();
	}

	public final ObjectValueIRKind getKind() {
		return fnPtr().getKind();
	}

	public final ObjectValueIR getReusedFrom() {
		return fnPtr().getReusedFrom();
	}

	public final ObjectValueIR getOrigin() {

		final ObjectValueIR reusedFrom = getReusedFrom();

		return reusedFrom != null ? reusedFrom : this;
	}

	public final DefValue getConstant() {
		return this.constant.get();
	}

	public final DefValue getFinal() {
		return this.finalValue.get();
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
		final ValueCodeBuilder builder =
				new ValueCodeBuilder(
				function,
				failure.head(),
				getObjectIR(),
				APPROXIMATE_OBJECT);

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
		if (!value.hasKnownValue()) {
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

		return fnPtr.getKind().isStub() ? null : fnPtr.ptr();
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

		if (finalValue.hasKnownValue()) {
			if (finalValue.getCondition().isFalse()) {
				// Final value is false.
				return predef(FALSE_VALUE_IR);
			}
			// Condition is true.
			if (!finalValue.hasValue()) {
				// Only condition present in value.
				if (finalValue.getCondition().isTrue()) {
					// Condition is unknown.
					// Do not update the value during calculation.
					return predef(UNKNOWN_VALUE_IR);
				}
				return null;
			}
			// Final value is known.
			if (getValueType().isVoid()) {
				// Value is void.
				return predef(VOID_VALUE_IR);
			}
		}
		if (getObject().value().getStatefulness().isEager()) {
			return predef(EAGER_VALUE_IR);
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
			return reuse(reuseFromIR);
		}

		return null;
	}

	private final ObjectValueFnPtr create(Function<ObjectValueFn> function) {
		return fnPtr(VALUE_IR, function.getPointer(), null);
	}

	private final ObjectValueFnPtr reuse(ObjectValueIR from) {
		return fnPtr(from.getKind(), from.ptr(), from.getOrigin());
	}

	private final ObjectValueFnPtr stub() {
		return predef(STUB_VALUE_IR);
	}

	private final ObjectValueFnPtr predef(ObjectValueIRKind kind) {
		return fnPtr(kind, kind.getFunctionName(), null);
	}

	private final ObjectValueFnPtr fnPtr(
			ObjectValueIRKind kind,
			String name,
			ObjectValueIR reusedFrom) {
		return fnPtr(
				kind,
				getGenerator().externalFunction().link(name, OBJECT_VALUE),
				reusedFrom);
	}

	private static final ObjectValueFnPtr fnPtr(
			ObjectValueIRKind kind,
			FuncPtr<ObjectValueFn> funcPtr,
			ObjectValueIR reusedFrom) {
		return new ObjectValueFnPtr(kind, funcPtr, reusedFrom);
	}

	private DefValue determineConstant() {

		final DefValue constant = defs().getConstant();

		if (!constant.hasKnownValue()) {
			return constant;
		}
		if (!ancestorDefsUpdated()) {
			return constant;
		}

		return RUNTIME_DEF_VALUE;
	}

	private DefValue determineFinal() {

		final DefValue constant = getConstant();

		if (constant.hasKnownValue()) {
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

		private final ObjectValueIRKind kind;
		private final FuncPtr<ObjectValueFn> ptr;
		private final ObjectValueIR reusedFrom;

		ObjectValueFnPtr(
				ObjectValueIRKind kind,
				FuncPtr<ObjectValueFn> ptr,
				ObjectValueIR reusedFrom) {
			this.ptr = ptr;
			this.kind = kind;
			this.reusedFrom = reusedFrom;
		}

		public final ObjectValueIRKind getKind() {
			return this.kind;
		}

		public final FuncPtr<ObjectValueFn> ptr() {
			return this.ptr;
		}

		public final ObjectValueIR getReusedFrom() {
			return this.reusedFrom;
		}

		@Override
		public String toString() {
			if (this.ptr == null) {
				return super.toString();
			}
			return this.ptr.toString();
		}

	}

	private static final class ValueCodeBuilder extends ObjBuilder {

		ValueCodeBuilder(
				Function<? extends ObjectFn<?>> function,
				CodePos exit,
				ObjectIR hostIR,
				ObjectPrecision hostPrecision) {
			super(function, exit, hostIR, hostPrecision);
		}

		@Override
		protected ObjOp host(
				Block code,
				CodePos exit,
				ObjectIR hostIR,
				ObjectPrecision hostPrecision) {
			setDefaultPresets(
					hostIR.getValueIR().valuePresets(defaultPresets(hostIR)));

			return super.host(code, exit, hostIR, hostPrecision);
		}

		private OpPresets defaultPresets(ObjectIR hostIR) {
			if (hostIR.getObject()
					.analysis()
					.valueEscapeMode(getGenerator().getEscapeAnalyzer())
					.isEscapePossible()) {
				return getDefaultPresets();
			}
			return getDefaultPresets().setStackAllocationAllowed(true);
		}

	}

}
