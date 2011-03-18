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
import static org.o42a.core.ir.op.ObjectValFunc.OBJECT_VAL;

import org.o42a.codegen.code.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Def;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.op.ObjectValFunc;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class ObjectValueIRValFunc extends ObjectValueIRFunc<ObjectValFunc> {

	ObjectValueIRValFunc(ObjectIR objectIR) {
		super(objectIR);
	}

	public abstract boolean isClaim();

	public void call(Code code, ValOp result, ObjOp host, ObjectOp body) {
		if (code.isDebug()) {
			if (body != null) {
				code.dumpName(
						"Calculate value " + this + " for ",
						body.toData(code));
			} else {
				code.debug("Calculate value " + this);
			}
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
			set(typeIR, unknownValFunc());
		} else {

			final Function<ObjectValFunc> function =
				getGenerator().newFunction().create(getId(), OBJECT_VAL);

			function.debug("Calculating value");
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

		final CodeBlk failure = function.addBlock("failure");
		final ValOp result = function.arg(OBJECT_VAL.value());
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				OBJECT_VAL.object(),
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

		final Def[] defs;

		if (isClaim()) {
			defs = definitions.getClaims();
		} else {
			defs = definitions.getPropositions();
		}
		if (defs.length == 0) {
			writeAncestorDef(code, result, host);
			return;
		}

		final Collector collector =
			new Collector(getObjectIR().getObject(), defs.length);

		collector.addDefs(defs);
		if (collector.ancestorIndex < 0) {
			if (collector.size() == 0) {
				writeAncestorDef(code, result, host);
			}
		}
		writeExplicitDefs(code, result, host, collector);

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

			final Code next;
			final CodePos nextPos;

			if (i + 1 < size) {
				next = code.addBlock((++displayIdx) + "_var");
				nextPos = next.head();
			} else {
				next = null;
				nextPos = code.tail();
			}

			if (i == collector.ancestorIndex) {
				writeAncestorDef(block, result, host);
				result.unknown(code).go(block, nextPos, code.tail());
			} else {
				// Write explicit definition.

				final Def def = defs[i];

				def.getPrerequisite().writeFullLogical(block, nextPos, host);
				def.writeValue(block, nextPos, host, result);
				block.go(code.tail());
			}

			if (next == null) {
				break;
			}
			block = next;
		}
	}

	private void writeAncestorDef(Code code, ValOp result, ObjOp host) {

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
		final ObjectTypeOp ancestorType =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(hasAncestor)
			.op(host.getBuilder(), DERIVED);

		if (isClaim()) {
			if (hasAncestor.isDebug()) {
				hasAncestor.dumpName(
						"Ancestor claim: ",
						ancestorBody.toData(code));
			}
			ancestorType.writeClaim(hasAncestor, result, ancestorBody);
		} else {
			if (hasAncestor.isDebug()) {
				hasAncestor.dumpName(
						"Ancestor proposition: ",
						ancestorBody.toData(code));
			}
			ancestorType.writeProposition(hasAncestor, result, ancestorBody);
		}

		hasAncestor.go(code.tail());
	}

	private final class Collector extends DefCollector<Def> {

		private final Def[] explicitDefs;
		private int size;
		private int ancestorIndex = -1;

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

		@Override
		protected void ancestorDef(Def def) {
			if (this.ancestorIndex >= 0) {
				return;
			}
			this.ancestorIndex = this.size;
			this.explicitDefs[this.size++] = null;
		}

	}

}
