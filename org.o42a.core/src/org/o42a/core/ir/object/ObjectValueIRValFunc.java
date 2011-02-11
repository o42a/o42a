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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;

import org.o42a.codegen.code.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Def;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.op.ObjectValFunc;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class ObjectValueIRValFunc extends ObjectValueIRFunc<ObjectValFunc> {

	ObjectValueIRValFunc(ObjectIR objectIR) {
		super(objectIR);
	}

	public void call(Code code, ValOp result, ObjOp host, ObjectOp body) {
		if (body != null) {
			code.dumpName("Calculate value " + this + " for ", body.ptr());
		} else {
			code.debug("Calculate value " + this);
		}

		if (writeFalseValue(code, result, body)) {
			return;
		}

		final Obj object = getObjectIR().getObject();
		final DefValue value = value(object.getDefinitions());

		if (body == null || value.isAlwaysMeaningful()) {

			final Value<?> realValue = value.getRealValue();

			if (realValue != null && realValue.isDefinite()) {
				if (!object.isRuntime() || value.getSource() == object) {
					code.debug(getObjectIR().getId() + " = " + realValue);
					result.store(code, realValue.val(getGenerator()));
					return;
				}
			}
		}

		get(host).op(code).call(code, result, body(code, host, body));
	}

	public void create(ObjectTypeIR typeIR, Definitions definitions) {

		final DefValue value = value(definitions);

		if (value.isUnknown()) {
			set(typeIR, getGenerator().unknownValFunc());
		} else {

			final Function<ObjectValFunc> function =
				getGenerator().newFunction().create(
						getId(),
						getGenerator().objectValSignature());

			function.debug("Calculating value");
			set(typeIR, function.getPointer());
		}
	}

	public void setFalse(ObjectTypeIR typeIR) {
		set(typeIR, getGenerator().falseValFunc());
	}

	public void build(Definitions definitions) {

		final Function<ObjectValFunc> function = get().getFunction();

		if (function == null) {
			return;
		}

		final CodeBlk failure = function.addBlock("failure");
		final ValOp result =
			function.ptrArg(function, 0).to(function, getGenerator().valType());
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				1,
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
			Definitions definitions,
			boolean claim) {

		final Def[] defs;

		if (claim) {
			defs = definitions.getClaims();
		} else {
			defs = definitions.getPropositions();
		}
		if (defs.length == 0) {
			writeAncestorDef(code, result, host, claim);
			return;
		}

		final Collector collector =
			new Collector(getObjectIR().getObject(), defs.length);

		collector.addDefs(defs);

		if (claim || collector.size() == 0) {
			writeAncestorDef(code, result, host, claim);
		}
		writeExplicitDefs(code, result, host, collector);

		code.returnVoid();
	}

	protected abstract void build(
			Code code,
			ValOp result,
			ObjOp host,
			Definitions definitions);

	private void writeExplicitDefs(
			Code code,
			ValOp result,
			ObjOp host,
			Collector collector) {

		final int size = collector.size();

		if (size == 0) {
			return;
		}

		final Def[] defs = collector.getExplicitDefs();

		int displayIdx = 0;
		Code block = code.addBlock("0_var");

		code.go(block.head());
		for (int i = 0; i < size; ++i) {

			final Def def = defs[i];
			final Code next;
			final CodePos nextPos;

			if (i + 1 < size) {
				next = code.addBlock((++displayIdx) + "_var");
				nextPos = next.head();
			} else {
				next = null;
				nextPos = code.tail();
			}

			// write explicit definition
			def.getPrerequisite().writeFullLogical(block, nextPos, host);
			def.writeValue(block, nextPos, host, result);

			block.go(code.tail());
			if (next == null) {
				break;
			}
			block = next;
		}
	}

	private void writeAncestorDef(
			Code code,
			ValOp result,
			ObjOp host,
			boolean claim) {

		final CondBlk hasAncestor =
			host.hasAncestor(code).branch(code, "has_ancestor", "no_ancestor");
		final CodeBlk noAncestor = hasAncestor.otherwise();

		final Obj object = getObjectIR().getObject();

		if (object.getValueType() == ValueType.VOID) {

			final TypeRef ancestor = object.getAncestor();

			if (ancestor.getType() == ancestor.getContext().getVoid()) {
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


		final ObjectDataOp ancestorData =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(hasAncestor)
			.data(host.getBuilder(), hasAncestor, DERIVED);

		if (claim) {
			hasAncestor.dumpName("Ancestor claim: ", ancestorBody.ptr());
			ancestorData.writeClaim(hasAncestor, result, ancestorBody);
		} else {
			hasAncestor.dumpName("Ancestor proposition: ", ancestorBody.ptr());
			ancestorData.writeProposition(hasAncestor, result, ancestorBody);
		}

		hasAncestor.go(code.tail());
	}

	private static final class Collector extends DefCollector<Def> {

		private final Def[] explicitDefs;
		private int size;

		Collector(Obj object, int capacity) {
			super(object);
			this.explicitDefs = new Def[capacity];
		}

		public final Def[] getExplicitDefs() {
			return this.explicitDefs;
		}

		public final int size() {
			return this.size;
		}

		@Override
		protected void explicitDef(Def def) {
			this.explicitDefs[this.size++] = def;
		}

	}

}
