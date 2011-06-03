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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.op.CodeDirs.splitWhenUnknown;
import static org.o42a.core.ir.op.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.code.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ObjectValueIRValFunc
		extends ObjectValueIRFunc<ObjectValFunc> {

	public ObjectValueIRValFunc(ObjectIR objectIR) {
		super(objectIR);
	}

	public abstract boolean isClaim();

	public ValOp call(ValDirs dirs, ObjOp host, ObjectOp body) {

		if (dirs.isDebug()) {
			dirs = dirs.begin("obj_val", "Calculate value " + this);
			if (body != null) {
				dirs.code().dumpName("For: ", body.toData(dirs.code()));
			}
		}

		final Code code = dirs.code();

		if (writeFalseValue(dirs.dirs(), body)) {
			if (dirs.isDebug()) {
				dirs.done();
			}
			return falseValue().op(code);
		}

		final Obj object = getObjectIR().getObject();
		final DefValue value = value(object.getDefinitions());

		if (body == null || value.isAlwaysMeaningful()) {

			final Value<?> realValue = value.getRealValue();

			if (realValue != null && realValue.isDefinite()) {
				if (!object.getConstructionMode().isRuntime()
						|| value.getSource() == object) {
					code.debug(getObjectIR().getId() + " = " + realValue);
					if (dirs.isDebug()) {
						dirs.done();
					}
					return realValue.valPtr(getGenerator())
					.op(code.id("const"), code);
				}
			}
		}

		final ValOp result =
			get(host).op(null, code).call(dirs, body(code, host, body));

		if (dirs.isDebug()) {
			dirs.done();
		}

		return result;
	}

	public void create(ObjectTypeIR typeIR, Definitions definitions) {

		final DefValue value = value(definitions);

		if (value.isUnknown()) {
			set(typeIR, unknownValFunc());
		} else {

			final Function<ObjectValFunc> function =
				getGenerator().newFunction().create(getId(), OBJECT_VAL);

			set(typeIR, function.getPointer());
		}
	}

	public void setFalse(ObjectTypeIR typeIR) {
		set(typeIR, falseValFunc());
	}

	public void build(Definitions definitions) {

		final Function<ObjectValFunc> function = get().getFunction();

		if (function == null) {
			return;
		}

		function.debug("Calculating value");

		final CodeBlk failure = function.addBlock("failure");
		final ValOp result = function.arg(function, OBJECT_VAL.value());
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				DERIVED);
		final ObjOp host = builder.host();

		function.dumpName("Host: ", host.ptr());
		build(function, result, host, definitions);
		if (failure.exists()) {
			result.storeFalse(failure);
			failure.returnVoid();
		}

		function.done();
	}

	public void buildFunc(
			Code code,
			ValOp result,
			ObjOp host,
			Definitions definitions) {

		final ValueDef[] defs;

		if (isClaim()) {
			defs = definitions.getClaims();
		} else {
			defs = definitions.getPropositions();
		}
		if (defs.length == 0) {
			writeAncestorDef(code, result, host);
			return;
		}

		final ValueCollector collector =
			new ValueCollector(code, getObjectIR().getObject(), defs.length);

		collector.addDefs(defs);
		if (collector.size() == 0) {
			writeAncestorDef(code, result, host);
		} else {
			writeExplicitDefs(code, result, host, collector);
		}

		code.returnVoid();
	}

	protected abstract void build(
			Code code,
			ValOp result,
			ObjOp host,
			Definitions definitions);

	private FuncPtr<ObjectValFunc> falseValFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_val_false",
				OBJECT_VAL);
	}

	private FuncPtr<ObjectValFunc> unknownValFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_val_unknown",
				OBJECT_VAL);
	}

	private void writeExplicitDefs(
			Code code,
			ValOp result,
			ObjOp host,
			ValueCollector collector) {

		final int size = collector.size();
		final ValueDef[] defs = collector.getExplicitDefs();

		code.go(collector.blocks[0].head());
		for (int i = 0; i < size; ++i) {

			final ValueDef def = defs[i];
			final Code block = collector.blocks[i];

			if (def == null) {
				if (i == collector.ancestorIndex) {
					writeAncestorDef(block, result, host);
					result.go(
							block,
							splitWhenUnknown(
									block,
									code.tail(),
									collector.next(i)));
					block.go(collector.next(i));
				}
				continue;
			}

			final CodeDirs defDirs = splitWhenUnknown(
					block,
					code.tail(),
					collector.next(i));

			def.write(defDirs, host, result);
			block.go(collector.next(i));
		}
	}

	private void writeAncestorDef(Code code, ValOp result, ObjOp host) {

		final CondBlk hasAncestor =
			host.hasAncestor(code).branch(code, "has_ancestor", "no_ancestor");
		final CodeBlk noAncestor = hasAncestor.otherwise();

		final Obj object = getObjectIR().getObject();

		if (object.getValueType() == ValueType.VOID) {

			final TypeRef ancestor =
				object.type().useBy(dummyUser()).getAncestor();

			if (ancestor.typeObject(dummyUser()).getScope()
					== ancestor.getContext().getVoid().getScope()) {
				noAncestor.debug("Inherited VOID proposition: " + this);
				result.storeVoid(noAncestor);
			} else {
				noAncestor.debug(
						"No ancestor, but does not inherit VOID explicitly: "
						+ this);
			}
		} else {
			noAncestor.debug("No ancestor, but not VOID: " + this);
		}

		noAncestor.go(code.tail());

		final ObjectOp ancestorBody = host.ancestor(hasAncestor);
		final ObjectTypeOp ancestorType =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(null, hasAncestor)
			.op(host.getBuilder(), DERIVED);

		final CodeBlk unknownAncValue = hasAncestor.addBlock(
				isClaim() ? "unknown_ancestor_claim"
				: "unknown_ancestor_proposition");
		final CodeBlk falseAncValue = hasAncestor.addBlock(
				isClaim() ? "false_ancestor_claim"
				: "false_ancestor_proposition");
		ValDirs dirs = splitWhenUnknown(
				hasAncestor,
				unknownAncValue.head(),
				falseAncValue.head())
				.value(isClaim() ? "ancestor_claim" : "ancestor_proposition");

		if (dirs.isDebug()) {
			dirs = dirs.begin(
					"debug",
					isClaim() ? "Ancestor claim" : "Ancestor proposition");
			hasAncestor.dumpName(
					"Ancestor: ",
					ancestorBody.toData(hasAncestor));
		}

		final ValOp res;

		if (isClaim()) {
			res = ancestorType.writeClaim(dirs, ancestorBody);
		} else {
			res = ancestorType.writeProposition(dirs, ancestorBody);
		}

		if (res != result) {
			result.store(hasAncestor, res);
		}
		if (dirs.isDebug()) {
			dirs.done();
		}
		hasAncestor.go(code.tail());

		if (falseAncValue.exists()) {
			result.storeFalse(falseAncValue);
			falseAncValue.go(code.tail());
		}
		if (unknownAncValue.exists()) {
			unknownAncValue.go(code.tail());
		}
	}

	private final class ValueCollector extends DefCollector<ValueDef> {

		private final Code code;
		private final ValueDef[] explicitDefs;
		private final Code[] blocks;
		private int size;
		private int ancestorIndex = -1;

		ValueCollector(Code code, Obj object, int capacity) {
			super(object);
			this.code = code;
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
			this.blocks[this.size] = this.code.addBlock(this.size + "_cvar");
			this.explicitDefs[this.size++] = def;
		}

		@Override
		protected void ancestorDef(ValueDef def) {
			if (this.ancestorIndex >= 0) {
				return;
			}
			this.ancestorIndex = this.size;
			this.blocks[this.size] = this.code.addBlock(this.size + "_vvar");
			++this.size;
		}

		CodePos next(int index) {

			final int next = index + 1;

			if (next < this.size) {
				return this.blocks[next].head();
			}

			return this.code.tail();
		}

	}

}
