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
package org.o42a.core.ir.object.value;

import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;
import static org.o42a.core.object.value.ValuePartUsage.ALL_VALUE_PART_USAGES;
import static org.o42a.core.object.value.ValueUsage.ALL_VALUE_USAGES;
import static org.o42a.core.st.DefValue.RUNTIME_DEF_VALUE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.value.ObjectValueFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Defs;
import org.o42a.core.object.value.ObjectValueDefs;
import org.o42a.core.st.DefValue;
import org.o42a.util.string.ID;


public class ObjectDefFnIR
		extends AbstractObjectValueFnIR<ObjectValueFunc>
		implements FunctionBuilder<ObjectValueFunc> {

	private static final ID DEF_ID = ID.rawId("def");

	public ObjectDefFnIR(ObjectValueIR valueIR) {
		super(valueIR);
	}

	public final ObjectValueDefs valueDefs() {
		return getObject().value().valueDefs();
	}

	public final Defs defs() {
		return valueDefs().getDefs();
	}

	public final void call(DefDirs dirs, ObjOp host) {

		final DefDirs subDirs = dirs.begin(
				null,
				"Calculating definition of " + getObjectIR().getId());
		final Block code = subDirs.code();

		if (!writeIfConstant(subDirs, getFinal())) {

			final ObjectValueFunc func = get(host).op(suffix(), code);

			func.call(subDirs, objectArg(code, host));
		}

		subDirs.done();
	}

	@Override
	public void build(Function<ObjectValueFunc> function) {
		function.debug("Calculating definition");

		final Block failure = function.addBlock("failure");
		final Block done = function.addBlock("done");
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				getObjectIR(),
				getObjectIR().getObject(),
				getObjectIR().isExact() ? EXACT : DERIVED);
		final ValOp result =
				function.arg(function, signature().value())
				.op(function, builder, getValueType(), VAL_TRAP);
		final DefDirs dirs =
				builder.dirs(function, failure.head())
				.value(result)
				.def(done.head());
		final ObjOp host = builder.host();

		if (getObjectIR().isExact()) {
			dirs.code().debug("Exact host: " + getObjectIR().getId());
		} else {
			dirs.code().dumpName("Host: ", host);
		}

		build(dirs, host);

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
	protected ID suffix() {
		return DEF_ID;
	}

	@Override
	protected FuncRec<ObjectValueFunc> func(ObjectIRData data) {
		return data.defFunc();
	}

	@Override
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

	@Override
	protected DefValue determineFinal() {
		if (isUsed() && !getObject().getConstructionMode().isPredefined()) {
			return RUNTIME_DEF_VALUE;
		}
		return defs().value(getObject().getScope().resolver());
	}

	@Override
	protected final ObjectValueFunc.Signature signature() {
		return OBJECT_VALUE;
	}

	@Override
	protected final FunctionBuilder<ObjectValueFunc> builder() {
		return this;
	}

	@Override
	protected boolean canStub() {
		if (isUsed()) {
			return false;
		}
		return !getObject().getConstructionMode().isPredefined();
	}

	@Override
	protected final FuncPtr<ObjectValueFunc> stubFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_value_stub", signature());
	}

	@Override
	protected final FuncPtr<ObjectValueFunc> unknownValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_value_unknown", signature());
	}

	@Override
	protected final FuncPtr<ObjectValueFunc> falseValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_value_false", signature());
	}

	@Override
	protected final FuncPtr<ObjectValueFunc> voidValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_value_void", signature());
	}

	@Override
	protected void reuse() {
		if (defs().isEmpty()) {
			return;
		}

		final Def def = defs().get()[0];

		if (def.isExplicit()) {
			return;
		}

		final Obj reuseFrom = def.getSource();
		final ObjectValueIR reuseFromIR =
				reuseFrom.ir(getGenerator()).allocate().getObjectValueIR();
		final FuncPtr<ObjectValueFunc> reused = reuseFromIR.def().getNotStub();

		if (reused != null) {
			reuse(reused);
		}
	}

	protected void build(DefDirs dirs, ObjOp host) {
		if (writeIfConstant(dirs, getFinal())) {
			return;
		}
		for (Def def : defs().get()) {
			def.eval().write(dirs, host);
			if (!dirs.code().exists()) {
				break;
			}
		}
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
