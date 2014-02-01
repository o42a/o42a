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
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.object.type.ValueTypeDescOp;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Defs;
import org.o42a.core.object.type.Sample;
import org.o42a.core.object.value.ObjectValuePart;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public abstract class ObjectValuePartFnIR
		extends AbstractObjectValueFnIR<ObjectValFunc>
		implements FunctionBuilder<ObjectValFunc> {

	private static final ID SAME_VALUE_TYPE = ID.rawId("same_value_type");

	ObjectValuePartFnIR(ObjectValueIR valueIR) {
		super(valueIR);
	}

	public abstract ObjectValuePart part();

	public final Defs defs() {
		return part().getDefs();
	}

	public final boolean isClaim() {
		return part().isClaim();
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

			final ObjectValFunc func = get(host).op(suffix(), code);

			func.call(subDirs, objectArg(code, host, body));
		}

		subDirs.done();
	}

	@Override
	public void build(Function<ObjectValFunc> function) {
		function.debug("Calculating " + suffix());

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
		if (partUsed() && !getObject().getConstructionMode().isPredefined()) {
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
		if (partUsed()) {
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

		final ObjectType type = getObject().type();
		final Sample[] samples = type.getSamples();

		if (samples.length > 1) {
			return;
		}

		final Obj reuseFrom;

		if (samples.length == 1) {

			final ObjectType sampleType =
					samples[0].getTypeRef().getType().type();

			reuseFrom = sampleType.getLastDefinition();
		} else {

			final TypeRef ancestor = type.getAncestor();

			if (ancestor == null) {
				return;
			}
			if (ancestorDefsUpdated()) {
				return;
			}

			reuseFrom = ancestor.getType().type().getLastDefinition();
		}
		if (!getObject().type().getValueType().is(
				reuseFrom.type().getValueType())) {
			// Do not reuse the value of different type.
			return;
		}
		if (defs().updatedSince(reuseFrom)) {
			return;
		}

		final ObjectValueIR reuseFromIR =
				reuseFrom.ir(getGenerator()).allocate().getObjectValueIR();
		final FuncPtr<ObjectValFunc> reused =
				reuseFromIR.value(isClaim()).getNotStub();

		if (reused != null) {
			reuse(reused);
		}
	}

	protected void build(DefDirs dirs, ObjOp host) {

		final DefValue finalValue = getFinal();

		if (writeIfConstant(dirs, finalValue)) {
			return;
		}

		final Defs defs = defs();

		if (defs.isEmpty()) {
			writeAncestorDef(dirs, host);
			return;
		}

		final DefCollector collector =
				new DefCollector(getObject(), defs.length());

		collector.addDefs(defs);

		if (collector.size() == 0) {
			writeAncestorDef(dirs, host);
			return;
		}

		writeExplicitDefs(dirs, host, collector);
	}

	private boolean partUsed() {
		return part().isUsed(
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
		return part().ancestorDefsUpdates().isUsed(
				getGenerator().getAnalyzer(),
				ALL_SIMPLE_USAGES);
	}

	private void writeExplicitDefs(
			DefDirs dirs,
			ObjOp host,
			DefCollector collector) {

		final int size = collector.size();
		final Def[] defs = collector.getExplicitDefs();

		for (int i = 0; i < size; ++i) {

			final Def def = defs[i];

			if (def == null) {
				if (i == collector.getAncestorIndex()) {
					writeAncestorDef(dirs, host);
				}
				continue;
			}

			def.eval().write(dirs, host);
		}
	}

	private void writeAncestorDef(
			DefDirs dirs,
			ObjOp host) {

		final Block code = dirs.code();

		if (!ancestorDefsUpdated()) {

			final TypeRef ancestor = getObject().type().getAncestor();

			if (ancestor == null) {
				code.debug("No ancestor " + suffix());
				return;
			}

			final Obj ancestorObject = ancestor.getType();
			final ObjectOp ancestorBody = host.ancestor(code);
			final ObjectTypeOp ancestorType =
					ancestorObject.ir(getGenerator())
					.getStaticTypeIR()
					.getInstance()
					.pointer(getGenerator())
					.op(null, code)
					.op(dirs.getBuilder(), EXACT);

			writeAncestorDef(dirs, host, ancestorBody, ancestorType);

			return;
		}

		final Block noAncestor = code.addBlock("no_ancestor");

		host.hasAncestor(code).goUnless(code, noAncestor.head());

		final ObjectOp ancestorBody = host.ancestor(code);
		final ObjectTypeOp ancestorType =
				ancestorBody.declaredIn(code).op(host.getBuilder(), DERIVED);

		writeAncestorDef(dirs, host, ancestorBody, ancestorType);

		noAncestor.debug("No ancestor " + suffix());
		writeVoid(noAncestor, dirs, host);

		noAncestor.go(code.tail());
	}

	private void writeVoid(Block code, DefDirs dirs, ObjOp host) {
		if (isClaim()) {
			return;
		}
		if (!dirs.getValueType().isVoid()) {
			return;
		}

		final ValueTypeDescOp hostValueType =
				host.objectType(code)
				.ptr()
				.data(code)
				.valueType(code)
				.load(null, code);
		final Block voidVal = code.addBlock("void_val");

		hostValueType.eq(
				null,
				code,
				ValueType.VOID.ir(getGenerator())
				.getValueTypeDesc()
				.op(null, code))
				.go(code, voidVal.head());
		if (voidVal.exists()) {
			voidVal.debug("Void ancestor");
			dirs.returnValue(voidVal, dirs.getBuilder().voidVal(voidVal));
		}
	}

	private void writeAncestorDef(
			DefDirs dirs,
			ObjOp host,
			ObjectOp ancestorBody,
			ObjectTypeOp ancestorType) {

		final DefDirs defDirs = dirs.begin(null, "Ancestor " + suffix());
		final Block code = defDirs.code();

		code.dumpName("Ancestor: ", ancestorBody);

		final Block diffValueType = code.addBlock("diff_value_type");

		final ValueTypeDescOp hostValueType =
				host.objectType(code)
				.ptr()
				.data(code)
				.valueType(code)
				.load(null, code);
		final ValueTypeDescOp ancestorValueType =
				ancestorType.ptr().data(code).valueType(code).load(null, code);

		hostValueType.eq(SAME_VALUE_TYPE, code, ancestorValueType)
		.goUnless(code, diffValueType.head());

		if (isClaim()) {
			ancestorType.writeClaim(defDirs, ancestorBody);
		} else {
			ancestorType.writeProposition(defDirs, ancestorBody);
		}

		if (diffValueType.exists()) {
			diffValueType.dumpName("Ancestor value type: ", ancestorValueType);
			diffValueType.dumpName("       differs from: ", hostValueType);
			diffValueType.go(code.tail());
		}

		defDirs.done();
	}

}
