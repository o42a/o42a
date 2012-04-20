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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.impl.ObjectIRFunc;
import org.o42a.core.ir.op.ValDirs;
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
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ObjectValueIRFunc extends ObjectIRFunc
		implements FunctionBuilder<ObjectValFunc> {

	private final ObjectValueIR valueIR;
	private final CodeId id;
	private FuncPtr<ObjectValFunc> funcPtr;
	private FuncRec<ObjectValFunc> func;
	private byte reused;
	private Value<?> constant;
	private Value<?> finalValue;

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

	public final Value<?> getConstant() {
		if (this.constant != null) {
			return this.constant;
		}
		return this.constant = determineConstant();
	}

	public final Value<?> getFinal() {
		if (this.finalValue != null) {
			return this.finalValue;
		}

		final Value<?> constant = getConstant();

		if (constant.getKnowledge().isKnown()) {
			return this.finalValue = constant;
		}

		return this.finalValue = determineFinal();
	}

	public ValOp call(ValDirs dirs, ObjOp host, ObjectOp body) {

		final ValDirs subDirs;

		if (dirs.isDebug()) {
			subDirs = dirs.begin(
					"Calculate " + suffix() + " of " + getObjectIR().getId());
			if (body != null) {
				subDirs.code().dumpName("For: ", body.toData(subDirs.code()));
			}
		} else {
			subDirs = dirs;
		}

		final Block code = subDirs.code();
		final Value<?> finalValue = getFinal();

		if (finalValue.getKnowledge().isKnown()) {
			code.debug(suffix() + " = " + finalValue.valueString());

			final ValOp result = finalValue.op(subDirs.getBuilder(), code);

			result.go(code, subDirs);
			if (subDirs.isDebug()) {
				subDirs.done();
			}

			return result;
		}

		final ObjectValFunc func = get(host).op(code.id(suffix()), code);
		final ValOp result = func.call(subDirs, objectArg(code, host, body));

		if (subDirs.isDebug()) {
			subDirs.done();
		}

		return result;
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
		final Block unknown = function.addBlock("unknown");
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
		final ValDirs dirs = builder.splitWhenUnknown(
				function,
				failure.head(),
				unknown.head())
				.value(result);
		final Block code = dirs.code();
		final ObjOp host = builder.host();

		code.dumpName("Host: ", host.ptr());
		result.store(code, build(dirs, host));

		dirs.done();
		if (failure.exists()) {
			result.storeFalse(failure);
			failure.returnVoid();
		}
		if (unknown.exists()) {
			unknown.returnVoid();
		}

		function.returnVoid();
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

	protected Value<?> determineConstant() {

		final Value<?> constant = defs().constant(definitions());

		if (!constant.getKnowledge().isKnown()) {
			return getValueStruct().runtimeValue();
		}
		if (!part().ancestorDefsUpdates().isUsed(
				getGenerator().getAnalyzer(),
				ALL_SIMPLE_USAGES)) {
			return constant;
		}

		return getValueStruct().runtimeValue();
	}

	protected Value<?> determineFinal() {
		if (!canStub()) {
			return getValueStruct().runtimeValue();
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

		final Value<?> finalValue = getFinal();

		if (finalValue.getKnowledge().isKnown()) {
			if (finalValue.getKnowledge().isFalse()) {
				if (finalValue.getKnowledge().hasUnknownCondition()) {
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

	protected ValOp build(ValDirs dirs, ObjOp host) {

		final Value<?> finalValue = getFinal();

		if (finalValue.getKnowledge().isKnown()) {

			final Block code = dirs.code();
			final ValOp result = finalValue.op(dirs.getBuilder(), code);

			code.debug(
					"Constant " + suffix()
					+ " = " + finalValue.valueString());
			result.go(code, dirs.dirs());

			return result;
		}

		final Defs defs = defs();

		if (defs.isEmpty()) {
			return writeAncestorDef(dirs, host);
		}

		final DefCollector collector = new DefCollector(
				dirs.dirs(),
				getObjectIR().getObject(),
				defs.length());

		collector.addDefs(defs);

		if (collector.size() == 0) {
			return writeAncestorDef(dirs, host);
		}

		return writeExplicitDefs(dirs, host, collector);
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

	private ValOp writeExplicitDefs(
			ValDirs dirs,
			ObjOp host,
			DefCollector collector) {

		final ValOp result = dirs.value();
		final Block code = dirs.code();
		final int size = collector.size();
		final Def[] defs = collector.getExplicitDefs();

		code.go(collector.block(0).head());
		for (int i = 0; i < size; ++i) {

			final Def def = defs[i];
			final Block block = collector.block(i);

			if (def == null) {
				if (i == collector.getAncestorIndex()) {

					final ValDirs defDirs = dirs.getBuilder().splitWhenUnknown(
							block,
							dirs.falseDir(),
							collector.next(i))
							.value(result);

					result.store(block, writeAncestorDef(defDirs, host));

					defDirs.done();

					if (block.exists()) {
						block.go(code.tail());
					}
				}
				continue;
			}

			final ValDirs defDirs = dirs.getBuilder().splitWhenUnknown(
					block,
					dirs.falseDir(),
					collector.next(i)).value(result);

			result.store(block, def.write(defDirs, host));

			defDirs.done();

			if (block.exists()) {
				block.go(code.tail());
			}
		}

		return result;
	}

	private ValOp writeAncestorDef(ValDirs dirs, ObjOp host) {

		final ValOp result = dirs.value();
		final Block code = dirs.code();

		if (!part().ancestorDefsUpdates().isUsed(
				getGenerator().getAnalyzer(),
				ALL_SIMPLE_USAGES)) {

			final TypeRef ancestor = getObject().type().getAncestor();

			if (ancestor == null) {
				code.debug("No ancestor " + suffix());
				code.go(dirs.unknownDir());
				return result;
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

			writeAncestorDef(dirs, code, ancestorBody, ancestorType);

			return result;
		}

		host.hasAncestor(code).goUnless(code, dirs.unknownDir());

		final ObjectOp ancestorBody = host.ancestor(code);
		final ObjectTypeOp ancestorType =
				ancestorBody.methods(code)
				.objectType(code)
				.load(null, code)
				.op(host.getBuilder(), DERIVED);

		writeAncestorDef(dirs, code, ancestorBody, ancestorType);

		return result;
	}

	private void writeAncestorDef(
			ValDirs dirs,
			Block code,
			ObjectOp ancestorBody,
			ObjectTypeOp ancestorType) {

		final ValDirs subDirs = dirs.sub(code);
		final ValDirs defDirs;

		if (!dirs.isDebug()) {
			defDirs = subDirs;
		} else {
			defDirs = subDirs.begin("Ancestor " + suffix());
			defDirs.code().dumpName(
					"Ancestor: ",
					ancestorBody.toData(defDirs.code()));
		}

		final ValOp res;

		if (isClaim()) {
			res = ancestorType.writeClaim(defDirs, ancestorBody);
		} else {
			res = ancestorType.writeProposition(defDirs, ancestorBody);
		}

		subDirs.value().store(defDirs.code(), res);

		if (dirs.isDebug()) {
			defDirs.done();
		}
		subDirs.done();
	}

}
