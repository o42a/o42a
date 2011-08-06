/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.code.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ValuePart;
import org.o42a.core.def.ValueDef;
import org.o42a.core.def.ValueDefs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ObjectValueIRValFunc
		extends ObjectValueIRFunc<ObjectValFunc> {

	private Value<?> constant;

	public ObjectValueIRValFunc(ObjectValueIR valueIR) {
		super(valueIR);
	}

	public final ValueType<?> getValueType() {
		return getObject().value().getValueType();
	}

	public abstract ValuePart valuePart();

	public abstract ValueDefs defs();

	public final boolean isClaim() {
		return valuePart().getDefKind().isClaim();
	}

	public final Value<?> getConstant() {
		if (this.constant != null) {
			return this.constant;
		}

		final Condition constantCondition =
				getValueIR().getConstantCondition();

		if (constantCondition.isFalse()) {
			return this.constant = getValueType().falseValue();
		}

		return this.constant = determineConstant();
	}

	public void create(ObjectTypeIR typeIR) {

		final Value<?> constant = getConstant();

		if (constant.isDefinite()) {
			if (constant.isFalse()) {
				if (constant.isUnknown()) {
					set(typeIR, unknownValFunc());
				} else {
					set(typeIR, falseValFunc());
				}
				return;
			}
			if (getValueType().isVoid()) {
				set(typeIR, voidValFunc());
				return;
			}
		}

		final Function<ObjectValFunc> function =
				getGenerator().newFunction().create(getId(), OBJECT_VAL);

		set(typeIR, function.getPointer());
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

		final Code code = subDirs.code();
		final Value<?> constant = getConstant();

		if (constant.isDefinite()) {
			code.debug(
					"Constant " + suffix()
					+ " = " + constant.valueString());

			final ValOp result = constant.op(subDirs.getBuilder(), code);

			result.go(code, subDirs);
			if (subDirs.isDebug()) {
				subDirs.done();
			}

			return result;
		}

		final ObjectValFunc func = get(host).op(code.id(suffix()), code);
		final ValOp result = func.call(subDirs, body(code, host, body));

		if (subDirs.isDebug()) {
			subDirs.done();
		}

		return result;
	}

	public void build() {

		final Function<ObjectValFunc> function = get().getFunction();

		if (function == null) {
			return;
		}

		function.debug("Calculating " + suffix());

		final Code failure = function.addBlock("failure");
		final Code unknown = function.addBlock("unknown");
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				DERIVED);
		final ValOp result =
				function.arg(function, OBJECT_VAL.value())
				.op(builder, getValueType())
				.setStoreMode(INITIAL_VAL_STORE);
		final ValDirs dirs = builder.splitWhenUnknown(
				function,
				failure.head(),
				unknown.head())
				.value(function.id(suffix() + "_val"), result);
		final Code code = dirs.code();
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
		function.done();
	}

	protected Value<?> determineConstant() {

		final Value<?> constant = defs().getConstant();

		if (!constant.isDefinite()) {
			return constant;
		}
		if (!valuePart().isAncestorDefsUpdatedBy(getGenerator())) {
			return constant;
		}

		return getValueType().runtimeValue();
	}

	protected ValOp build(ValDirs dirs, ObjOp host) {

		final Value<?> constant = getConstant();

		if (constant.isDefinite()) {

			final Code code = dirs.code();
			final ValOp result = constant.op(dirs.getBuilder(), code);

			code.debug(
					"Constant " + suffix()
					+ " = " + constant.valueString());
			result.go(code, dirs.dirs());

			return result;
		}

		final ValueDefs defs = defs();

		if (defs.isEmpty()) {
			return writeAncestorDef(dirs, host);
		}

		final ValueCollector collector = new ValueCollector(
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
		return getGenerator().externalFunction(
				"o42a_obj_val_false",
				OBJECT_VAL);
	}

	final FuncPtr<ObjectValFunc> voidValFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_val_void",
				OBJECT_VAL);
	}

	final FuncPtr<ObjectValFunc> unknownValFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_val_unknown",
				OBJECT_VAL);
	}

	private ValOp writeExplicitDefs(
			ValDirs dirs,
			ObjOp host,
			ValueCollector collector) {

		final ValOp result = dirs.value();
		final Code code = dirs.code();
		final int size = collector.size();
		final ValueDef[] defs = collector.getExplicitDefs();

		code.go(collector.blocks[0].head());
		for (int i = 0; i < size; ++i) {

			final ValueDef def = defs[i];
			final Code block = collector.blocks[i];

			if (def == null) {
				if (i == collector.ancestorIndex) {

					final ValDirs defDirs = dirs.getBuilder().splitWhenUnknown(
							block,
							code.tail(),
							collector.next(i))
							.value(block.id("val"), result);

					result.store(
							defDirs.code(),
							writeAncestorDef(defDirs, host));

					defDirs.done();

					block.go(collector.next(i));
				}
				continue;
			}

			final ValDirs defDirs = dirs.getBuilder().splitWhenUnknown(
					block,
					code.tail(),
					collector.next(i)).value(block.id("val"), result);

			result.store(defDirs.code(), def.write(defDirs, host));
			defDirs.done();
			block.go(collector.next(i));
		}

		return result;
	}

	private ValOp writeAncestorDef(ValDirs dirs, ObjOp host) {

		final ValOp result = dirs.value();
		final Code code = dirs.code();

		if (!valuePart().isAncestorDefsUpdatedBy(getGenerator())) {

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

		final CondCode hasAncestor = host.hasAncestor(code).branch(
				code,
				"has_ancestor",
				"no_ancestor");
		final Code noAncestor = hasAncestor.otherwise();

		final Obj object = getObjectIR().getObject();
		final TypeRef ancestor = object.type().getAncestor();

		if (ancestor.typeObject(dummyUser()).getScope()
				== ancestor.getContext().getVoid().getScope()) {
			noAncestor.debug("Inherited VOID " + suffix());
			result.storeVoid(noAncestor);
			noAncestor.go(code.tail());
		} else {
			noAncestor.debug("No ancestor " + suffix());
			noAncestor.go(dirs.unknownDir());
		}

		final ObjectOp ancestorBody = host.ancestor(hasAncestor);
		final ObjectTypeOp ancestorType =
				ancestorBody.methods(hasAncestor)
				.objectType(hasAncestor)
				.load(null, hasAncestor)
				.op(host.getBuilder(), DERIVED);

		writeAncestorDef(dirs, hasAncestor, ancestorBody, ancestorType);

		hasAncestor.go(code.tail());

		return result;
	}

	private void writeAncestorDef(
			ValDirs dirs,
			Code code,
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

	private final class ValueCollector extends DefCollector<ValueDef> {

		private final CodeDirs dirs;
		private final ValueDef[] explicitDefs;
		private final Code[] blocks;
		private int size;
		private int ancestorIndex = -1;

		ValueCollector(CodeDirs dirs, Obj object, int capacity) {
			super(object);
			this.dirs = dirs;
			this.explicitDefs = new ValueDef[capacity];
			this.blocks = new Code[capacity];
		}

		public final ValueDef[] getExplicitDefs() {
			return this.explicitDefs;
		}

		public final int size() {
			return this.size;
		}

		@Override
		protected void explicitDef(ValueDef def) {
			this.blocks[this.size] = this.dirs.addBlock(this.size + "_cvar");
			this.explicitDefs[this.size++] = def;
		}

		@Override
		protected void ancestorDef(ValueDef def) {
			if (this.ancestorIndex >= 0) {
				return;
			}
			this.ancestorIndex = this.size;
			this.blocks[this.size] = this.dirs.addBlock(this.size + "_vvar");
			++this.size;
		}

		CodePos next(int index) {

			final int next = index + 1;

			if (next < this.size) {
				return this.blocks[next].head();
			}

			return this.dirs.unknownDir();
		}

	}

}
