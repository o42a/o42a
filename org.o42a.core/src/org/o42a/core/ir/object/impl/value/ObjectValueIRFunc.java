/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;
import static org.o42a.core.object.type.DerivationUsage.RUNTIME_DERIVATION_USAGE;
import static org.o42a.core.object.value.ValuePartUsage.VALUE_PART_ACCESS;
import static org.o42a.core.object.value.ValueUsage.ALL_VALUE_USAGES;
import static org.o42a.core.st.DefValue.RUNTIME_DEF_VALUE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.impl.ObjectIRFunc;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Defs;
import org.o42a.core.object.def.SourceInfo;
import org.o42a.core.object.type.Sample;
import org.o42a.core.object.value.ObjectValuePart;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ObjectValueIRFunc extends ObjectIRFunc
		implements FunctionBuilder<ObjectValFunc> {

	private final ObjectValueIR valueIR;
	private final CodeId id;
	private FuncPtr<ObjectValFunc> funcPtr;
	private FuncRec<ObjectValFunc> func;
	private byte reused;
	private DefValue constant;
	private DefValue finalValue;

	ObjectValueIRFunc(ObjectValueIR valueIR) {
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

	public final boolean isReused() {
		return this.reused > 0;
	}

	public final boolean isStub() {
		return this.reused == 2;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return getObject().value().getValueStruct();
	}

	public abstract ObjectValuePart part();

	public final Defs defs() {
		return part().getDefs();
	}

	public final boolean isClaim() {
		return part().isClaim();
	}

	public final FuncPtr<ObjectValFunc> get() {

		final FuncPtr<ObjectValFunc> ptr = getNotStub();

		assert ptr != null :
			"Attempt to call a stub function: " + this;

		return this.funcPtr;
	}

	public final FuncPtr<ObjectValFunc> getNotStub() {
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

	public void call(DefDirs dirs, ObjOp host, ObjectOp body) {

		final DefDirs subDirs = dirs.begin(
				"Calculate " + suffix() + " of " + getObjectIR().getId());
		final Block code = subDirs.code();

		if (body != null) {
			code.dumpName("For: ", body.toData(code));
		}

		final DefValue finalValue = getFinal();

		if (!writeIfConstant(subDirs, finalValue)) {

			final ObjectValFunc func = get(host).op(code.id(suffix()), code);

			func.call(subDirs, objectArg(code, host, body));
		}

		subDirs.done();
	}

	public void allocate(ObjectTypeIR typeIR) {
		this.func = func(typeIR.getObjectData());
		if (this.funcPtr == null) {
			create();
		}
		this.func.setConstant(true).setValue(this.funcPtr);
	}

	public final FuncPtr<ObjectValFunc> get(ObjOp host) {

		final ObjectIR objectIR = host.getAscendant().ir(getGenerator());
		final ObjectTypeIR typeIR =
				objectIR.getBodyType().getObjectIR().getTypeIR();
		final ObjectIRData data = typeIR.getObjectData();

		return func(data).getValue().get();
	}

	public int addSources(SourceInfo[] destination, SourceInfo[] sources) {

		int size = 0;

		for (SourceInfo def : sources) {
			size = addSource(destination, size, def);
		}

		return size;
	}

	@Override
	public void build(Function<ObjectValFunc> function) {
		if (isReused()) {
			return;
		}

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
				.op(builder, getValueStruct())
				.setStoreMode(INITIAL_VAL_STORE);
		final DefDirs dirs =
				builder.dirs(function, failure.head())
				.value(result)
				.def(done.head());
		final Block code = dirs.code();
		final ObjOp host = builder.host();

		code.dumpName("Host: ", host.ptr());
		build(dirs, host);

		dirs.done();

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
	public DataOp objectArg(Code code, ObjOp host, ObjectOp body) {
		if (isReused()) {
			return body != null ? body.toData(code) : host.toData(code);
		}
		return super.objectArg(code, host, body);
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected DefValue determineConstant() {

		final DefValue constant = defs().constant(definitions());

		if (!isConstantValue(constant)) {
			return constant;
		}
		if (!part().ancestorDefsUpdates().isUsed(
				getGenerator().getAnalyzer(),
				ALL_SIMPLE_USAGES)) {
			return constant;
		}

		return RUNTIME_DEF_VALUE;
	}

	protected DefValue determineFinal() {
		if (!canStub()) {
			return RUNTIME_DEF_VALUE;
		}
		return defs().value(
				definitions(),
				getObject().getScope().dummyResolver());
	}

	protected void create() {
		if (canStub() && !getObject().value().isUsed(
				getGenerator().getAnalyzer(),
				ALL_VALUE_USAGES)) {
			stub(stubFunc());
			return;
		}

		final DefValue finalValue = getFinal();

		if (isConstantValue(finalValue)) {
			if (!finalValue.hasValue()) {
				if (finalValue.getLogicalValue().isTrue()) {
					reuse(unknownValFunc());
				} else {
					reuse(falseValFunc());
				}
				return;
			}
			if (getValueStruct().isVoid()) {
				reuse(voidValFunc());
				return;
			}
		}

		reuse();
		if (isReused()) {
			return;
		}

		set(getGenerator().newFunction().create(getId(), OBJECT_VAL, this));
	}

	protected boolean canStub() {
		if (getObject().type().derivation().isUsed(
				getGenerator().getAnalyzer(),
				RUNTIME_DERIVATION_USAGE)) {
			return false;
		}
		return !part().isUsed(getGenerator().getAnalyzer(), VALUE_PART_ACCESS);
	}

	protected abstract String suffix();

	protected final void set(Function<ObjectValFunc> function) {
		this.funcPtr = function.getPointer();
		this.reused = -1;
	}

	protected final void reuse(FuncPtr<ObjectValFunc> ptr) {
		this.funcPtr = ptr;
		this.reused = 1;
	}

	protected final void stub(FuncPtr<ObjectValFunc> ptr) {
		this.funcPtr = ptr;
		this.reused = 2;
	}

	protected abstract FuncRec<ObjectValFunc> func(ObjectIRData data);

	protected void reuse() {

		final ObjectType type = getObject().type();
		final Sample[] samples = type.getSamples();

		if (samples.length > 1) {
			return;
		}

		final Obj reuseFrom;

		if (samples.length == 1) {

			final ObjectType sampleType =
					samples[0].getTypeRef().type(dummyUser());

			reuseFrom = sampleType.getLastDefinition();
		} else {

			final TypeRef ancestor = type.getAncestor();

			if (ancestor == null) {
				return;
			}
			if (part().ancestorDefsUpdates().isUsed(
					getGenerator().getAnalyzer(),
					ALL_SIMPLE_USAGES)) {
				return;
			}

			reuseFrom = ancestor.type(dummyUser()).getLastDefinition();
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

	final FuncPtr<ObjectValFunc> falseValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_false", OBJECT_VAL);
	}

	final FuncPtr<ObjectValFunc> voidValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_void", OBJECT_VAL);
	}

	final FuncPtr<ObjectValFunc> unknownValFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_unknown", OBJECT_VAL);
	}

	final FuncPtr<ObjectValFunc> stubFunc() {
		return getGenerator()
				.externalFunction()
				.link("o42a_obj_val_stub", OBJECT_VAL);
	}

	static boolean isConstantValue(DefValue value) {
		if (!value.getLogicalValue().isConstant()) {
			return false;
		}
		if (!value.hasValue()) {
			return true;
		}
		return value.getValue().getKnowledge().isKnown();
	}

	private int addSource(SourceInfo[] destination, int at, SourceInfo source) {

		final Obj src = source.getSource();

		if (src == getObjectIR().getObject()) {
			// explicit definition - add unconditionally
			destination[at] = source;
			return at + 1;
		}

		final ObjectType srcType = src.type();

		for (int i = 0; i < at; ++i) {

			final SourceInfo dest = destination[i];

			if (dest == null) {
				continue;
			}

			final ObjectType destType = dest.getSource().type();

			if (destType.derivedFrom(srcType)) {
				// definition will be generated by derived definition
				return at;
			}
			if (destType.derivedFrom(srcType)) {
				// new definition generates ascending one
				destination[i] = null;
			}
		}

		destination[at] = source;

		return at + 1;
	}

	private boolean writeIfConstant(DefDirs dirs, DefValue value) {
		if (!isConstantValue(value)) {
			return false;
		}

		final Block code = dirs.code();

		code.debug(suffix() + " = " + value.valueString());

		if (value.getLogicalValue().isFalse()) {
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

	private void writeAncestorDef(DefDirs dirs, ObjOp host) {

		final Block code = dirs.code();

		if (!part().ancestorDefsUpdates().isUsed(
				getGenerator().getAnalyzer(),
				ALL_SIMPLE_USAGES)) {

			final TypeRef ancestor = getObject().type().getAncestor();

			if (ancestor == null) {
				code.debug("No ancestor " + suffix());
				return;
			}

			final Obj ancestorObject = ancestor.typeObject(dummyUser());
			final ObjectOp ancestorBody = host.ancestor(code);
			final ObjectTypeOp ancestorType =
					ancestorObject.ir(getGenerator())
					.getStaticTypeIR()
					.getInstance()
					.pointer(getGenerator())
					.op(null, code)
					.op(dirs.getBuilder(), EXACT);

			writeAncestorDef(dirs, ancestorBody, ancestorType);

			return;
		}

		final Block noAncestor = code.addBlock("no_ancestor");

		host.hasAncestor(code).goUnless(code, noAncestor.head());

		final ObjectOp ancestorBody = host.ancestor(code);
		final ObjectTypeOp ancestorType =
				ancestorBody.methods(code)
				.objectType(code)
				.load(null, code)
				.op(host.getBuilder(), DERIVED);

		writeAncestorDef(dirs, ancestorBody, ancestorType);

		noAncestor.debug("No ancestor " + suffix());
		noAncestor.go(code.tail());
	}

	private void writeAncestorDef(
			DefDirs dirs,
			ObjectOp ancestorBody,
			ObjectTypeOp ancestorType) {

		final DefDirs defDirs = dirs.begin("Ancestor " + suffix());

		defDirs.code().dumpName(
				"Ancestor: ",
				ancestorBody.toData(defDirs.code()));

		if (isClaim()) {
			ancestorType.writeClaim(defDirs, ancestorBody);
		} else {
			ancestorType.writeProposition(defDirs, ancestorBody);
		}

		defDirs.done();
	}

}
