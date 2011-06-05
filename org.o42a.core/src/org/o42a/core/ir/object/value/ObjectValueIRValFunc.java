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


public abstract class ObjectValueIRValFunc
		extends ObjectValueIRFunc<ObjectValFunc> {

	public ObjectValueIRValFunc(ObjectIR objectIR) {
		super(objectIR);
	}

	public abstract boolean isClaim();

	public ValOp call(ValDirs dirs, ObjOp host, ObjectOp body) {
		if (dirs.isDebug()) {
			dirs = dirs.begin("Calculate value " + getObjectIR().getId());
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
					return realValue.op(code);
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
		final CodeBlk unknown = function.addBlock("unknown");
		final ValOp result = function.arg(function, OBJECT_VAL.value());
		final ObjBuilder builder = new ObjBuilder(
				function,
				failure.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				DERIVED);
		final ValDirs dirs = splitWhenUnknown(
				function,
				failure.head(),
				unknown.head())
				.value(
						function.id(
								isClaim() ? "claim_val" : "proposition_val"),
						result);
		final Code code = dirs.code();
		final ObjOp host = builder.host();

		code.dumpName("Host: ", host.ptr());

		final ValOp res = build(dirs, host, definitions);

		if (res != result) {
			result.store(code, res);
		}

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

	public ValOp buildFunc(ValDirs dirs, ObjOp host, Definitions definitions) {

		final ValueDef[] defs;

		if (isClaim()) {
			defs = definitions.getClaims();
		} else {
			defs = definitions.getPropositions();
		}
		if (defs.length == 0) {
			return writeAncestorDef(dirs, host);
		}

		final ValueCollector collector = new ValueCollector(
				dirs.dirs(),
				getObjectIR().getObject(),
				defs.length);

		collector.addDefs(defs);

		if (collector.size() == 0) {
			return writeAncestorDef(dirs, host);
		}

		return writeExplicitDefs(dirs, host, collector);
	}

	protected abstract ValOp build(
			ValDirs dirs,
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

					final ValDirs defDirs = splitWhenUnknown(
							block,
							code.tail(),
							collector.next(i))
							.value(block.id("val"), result);
					final ValOp res = writeAncestorDef(defDirs, host);

					if (res != result) {
						result.store(defDirs.code(), res);
					}
					defDirs.done();

					block.go(collector.next(i));
				}
				continue;
			}

			final ValDirs defDirs = splitWhenUnknown(
					block,
					code.tail(),
					collector.next(i)).value(block.id("val"), result);
			final ValOp res = def.write(defDirs, host);

			if (res != result) {
				result.store(defDirs.code(), res);
			}
			defDirs.done();
			block.go(collector.next(i));
		}

		return result;
	}

	private ValOp writeAncestorDef(ValDirs dirs, ObjOp host) {

		final ValOp result = dirs.value();
		final Code code = dirs.code();
		final CondBlk hasAncestor =
			host.hasAncestor(code).branch(code, "has_ancestor", "no_ancestor");
		final CodeBlk noAncestor = hasAncestor.otherwise();

		final Obj object = getObjectIR().getObject();
		final TypeRef ancestor =
			object.type().useBy(dummyUser()).getAncestor();

		if (ancestor.typeObject(dummyUser()).getScope()
				== ancestor.getContext().getVoid().getScope()) {
			noAncestor.debug("Inherited VOID proposition: " + this);
			result.storeVoid(noAncestor);
			noAncestor.go(code.tail());
		} else {
			noAncestor.debug("No ancestor: " + this);
			dirs.dirs().goWhenUnknown(noAncestor);
		}

		final ObjectOp ancestorBody = host.ancestor(hasAncestor);
		final ObjectTypeOp ancestorType =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(null, hasAncestor)
			.op(host.getBuilder(), DERIVED);

		final ValDirs subDirs = dirs.sub(hasAncestor);
		final ValDirs defDirs;

		if (!dirs.isDebug()) {
			defDirs = subDirs;
		} else {
			defDirs = subDirs.begin(
					isClaim() ? "Ancestor claim" : "Ancestor proposition");
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
		if (res != result) {
			result.store(defDirs.code(), res);
		}

		if (dirs.isDebug()) {
			defDirs.done();
		}
		subDirs.done();
		hasAncestor.go(code.tail());

		return result;
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
