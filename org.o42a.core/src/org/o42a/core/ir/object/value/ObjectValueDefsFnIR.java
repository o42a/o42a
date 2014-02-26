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
import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;
import static org.o42a.core.object.value.ValuePartUsage.ALL_VALUE_PART_USAGES;
import static org.o42a.core.object.value.ValueUsage.ANY_RUNTIME_VALUE_USAGE;
import static org.o42a.core.st.DefValue.RUNTIME_DEF_VALUE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Defs;
import org.o42a.core.object.value.ObjectValueDefs;
import org.o42a.core.st.DefValue;
import org.o42a.util.string.ID;


public class ObjectValueDefsFnIR
		extends AbstractObjectValueFnIR<ObjectValFunc>
		implements FunctionBuilder<ObjectValFunc> {

	private static final ID DEFS_ID = ID.id("defs");

	public ObjectValueDefsFnIR(ObjectValueIR valueIR) {
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

			final ObjectValFunc func = get(host).op(suffix(), code);

			func.call(subDirs, objectArg(code, host));
		}

		subDirs.done();
	}

	@Override
	public void build(Function<ObjectValFunc> function) {
		function.debug("Calculating definition");

		final Block failure = function.addBlock("failure");
		final Block done = function.addBlock("done");
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				getObjectIR().isExact() ? EXACT : DERIVED);
		final ValOp result =
				function.arg(function, OBJECT_VAL.value())
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
		return DEFS_ID;
	}

	@Override
	protected FuncRec<ObjectValFunc> func(ObjectIRData data) {
		return data.defsFunc();
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
	protected final ObjectSignature<ObjectValFunc> signature() {
		return OBJECT_VAL;
	}

	@Override
	protected final FunctionBuilder<ObjectValFunc> builder() {
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
	protected final FuncPtr<ObjectValFunc> stubFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_stub", OBJECT_VAL);
	}

	@Override
	protected final FuncPtr<ObjectValFunc> unknownValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_unknown", OBJECT_VAL);
	}

	@Override
	protected final FuncPtr<ObjectValFunc> falseValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_false", OBJECT_VAL);
	}

	@Override
	protected final FuncPtr<ObjectValFunc> voidValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_void", OBJECT_VAL);
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
		final FuncPtr<ObjectValFunc> reused = reuseFromIR.defs().getNotStub();

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
				ANY_RUNTIME_VALUE_USAGE);
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