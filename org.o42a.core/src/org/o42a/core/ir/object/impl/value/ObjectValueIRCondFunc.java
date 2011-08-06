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
import static org.o42a.core.ir.object.impl.value.DefCollector.explicitDef;
import static org.o42a.core.ir.op.ObjectCondFunc.OBJECT_COND;
import static org.o42a.core.ir.value.Val.CONDITION_FLAG;
import static org.o42a.core.ir.value.Val.UNKNOWN_FLAG;

import org.o42a.codegen.code.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ValuePart;
import org.o42a.core.def.*;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectCondFunc;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Condition;


public abstract class ObjectValueIRCondFunc
		extends ObjectValueIRFunc<ObjectCondFunc> {

	private Condition constant;

	public ObjectValueIRCondFunc(ObjectValueIR valueIR) {
		super(valueIR);
	}

	public abstract ValuePart valuePart();

	public abstract CondDefs defs();

	public final boolean isRequirement() {
		return valuePart().getDefKind().isClaim();
	}

	public final Condition getConstant() {
		if (this.constant != null) {
			return this.constant;
		}
		return this.constant = determineConstant();
	}

	public void create(ObjectTypeIR typeIR) {
		set(typeIR, createFunc());
	}

	public void call(CodeDirs dirs, ObjOp host, ObjectOp body) {

		final CodeDirs subDirs;

		if (!dirs.isDebug()) {
			subDirs = dirs;
		} else {
			subDirs = dirs.begin(
					"calc_cond",
					"Calculate " + suffix() + " " + this);
			if (body != null) {
				subDirs.code().dumpName("For: ", body.toData(subDirs.code()));
			}
		}

		final Code code = subDirs.code();

		switch (getConstant()) {
		case TRUE:
			code.debug("True");
			break;
		case RUNTIME:

			final ObjectCondFunc func = get(host).op(code.id(suffix()), code);

			func.call(code, body(code, host, body)).go(code, subDirs);

			break;
		case UNKNOWN:
			code.debug("Unknown");
			code.go(subDirs.unknownDir());
			break;
		case FALSE:
			code.debug("False");
			code.go(subDirs.falseDir());
		}

		subDirs.end();
	}

	public void build() {

		final Function<ObjectCondFunc> function = get().getFunction();

		if (function == null) {
			return;
		}

		function.debug("Calculating " + suffix());

		final Code condFalse = function.addBlock("false");
		final Code condUnknown = function.addBlock("unknown");
		final ObjBuilder builder = new ObjBuilder(
				function,
				condFalse.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				DERIVED);
		final ObjOp host = builder.host();

		function.dumpName("Host: ", host.ptr());

		final CodeDirs dirs = builder.splitWhenUnknown(
				function,
				condFalse.head(),
				condUnknown.head());

		build(dirs, host);

		function.int8((byte) CONDITION_FLAG).returnValue(function);
		if (condFalse.exists()) {
			condFalse.int8((byte) 0).returnValue(condFalse);
		}
		if (condUnknown.exists()) {
			condUnknown.int8((byte) UNKNOWN_FLAG)
			.returnValue(condUnknown);
		}

		function.done();
	}

	protected Condition determineConstant() {

		final Condition constant = defs().getConstant();

		if (!constant.isConstant() || constant.isFalse()) {
			return constant;
		}
		if (!valuePart().isAncestorDefsUpdatedBy(getGenerator())) {
			return constant;
		}

		return Condition.RUNTIME;
	}

	@Override
	protected final DefValue value() {

		final Resolver resolver = definitions().getScope().dummyResolver();

		return defs().resolve(resolver);
	}

	protected void build(CodeDirs dirs, ObjOp host) {

		final CondDefs defs = defs();
		final CondCollector collector = new CondCollector(
				dirs.code(),
				getObjectIR().getObject(),
				defs.length());

		collector.addDefs(defs);
		if (collector.hasExplicit) {
			writeExplicitDefs(dirs, host, collector);
			return;
		}
		if (isRequirement()) {
			writeAncestorDef(dirs, host, true);
			return;
		}
		if (collector.size() == 0 && hasExplicitProposition()) {
			return;
		}
		writeAncestorDef(dirs, host, true);
	}

	private void writeAncestorDef(
			CodeDirs dirs,
			ObjOp host,
			boolean trueWithoutAncestor) {

		final Code code = dirs.code();
		final CondCode hasAncestor =
			host.hasAncestor(code).branch(code, "has_ancestor", "no_ancestor");
		final Code noAncestor = hasAncestor.otherwise();

		noAncestor.debug("No ancestor " + suffix());
		if (trueWithoutAncestor) {
			noAncestor.go(code.tail());
		} else {
			noAncestor.go(dirs.unknownDir());
		}

		final ObjectOp ancestorBody = host.ancestor(hasAncestor);
		final ObjectTypeOp ancestorType =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(null, hasAncestor)
			.op(host.getBuilder(), DERIVED);

		CodeDirs ancestorDirs = dirs.getBuilder().splitWhenUnknown(
				hasAncestor,
				dirs.falseDir(),
				dirs.unknownDir());

		if (isRequirement()) {
			if (hasAncestor.isDebug()) {
				ancestorDirs = ancestorDirs.begin(
						"ancestor",
						"Ancestor requirement");
				hasAncestor.dumpName(
						"Ancestor: ",
						ancestorBody.toData(hasAncestor));
			}
			ancestorType.writeRequirement(ancestorDirs, ancestorBody);
		} else {
			if (hasAncestor.isDebug()) {
				ancestorDirs = ancestorDirs.begin(
						"ancestor",
						"Ancestor condition");
				hasAncestor.dumpName(
						"Ancestor: ",
						ancestorBody.toData(hasAncestor));
			}
			ancestorType.writeCondition(ancestorDirs, ancestorBody);
		}
		ancestorDirs.end();

		hasAncestor.go(code.tail());
	}

	private void writeExplicitDefs(
			CodeDirs dirs,
			ObjOp host,
			CondCollector collector) {

		final Code code = dirs.code();
		final CondDef[] defs = collector.getExplicitDefs();
		final int size = collector.size();

		code.go(collector.blocks[0].head());
		for (int i = 0; i < size; ++i) {

			final CondDef def = defs[i];
			final Code block = collector.blocks[i];

			if (def == null) {
				if (i == collector.ancestorIndex) {

					final CodeDirs ancestorDirs =
						dirs.getBuilder().splitWhenUnknown(
								block,
								dirs.falseDir(),
								collector.next(i, dirs.unknownDir()));

					writeAncestorDef(ancestorDirs, host, false);
					block.go(collector.next(i, code.tail()));
				}
				continue;
			}
			if (!def.hasPrerequisite()) {

				final CodeDirs defDirs =
					dirs.getBuilder().falseWhenUnknown(block, dirs.falseDir());

				def.write(defDirs, host);
				block.go(collector.next(i, code.tail()));
				continue;
			}

			final CodeDirs defDirs = dirs.getBuilder().splitWhenUnknown(
					block,
					dirs.falseDir(),
					collector.next(i, dirs.unknownDir()));

			def.write(defDirs, host);
			block.go(collector.nextRequired(i, code.tail()));
		}
	}

	private boolean hasExplicitProposition() {

		final Obj object = getObjectIR().getObject();

		for (ValueDef proposition : definitions().propositions().get()) {
			if (explicitDef(object, proposition)) {
				return true;
			}
		}

		return false;
	}

	private FuncPtr<ObjectCondFunc> createFunc() {
		switch (getConstant()) {
		case TRUE:
			return trueFunc();
		case FALSE:
			return falseFunc();
		case RUNTIME:
			return getGenerator().newFunction().create(
					getId(),
					OBJECT_COND).getPointer();
		case UNKNOWN:
			return unknownFunc();
		}

		throw new IllegalStateException(
				"Unsupported condition value: " + getConstant());
	}

	private FuncPtr<ObjectCondFunc> trueFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_cond_true",
				OBJECT_COND);
	}

	private FuncPtr<ObjectCondFunc> falseFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_cond_false",
				OBJECT_COND);
	}

	private FuncPtr<ObjectCondFunc> unknownFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_cond_unknown",
				OBJECT_COND);
	}

	private final class CondCollector extends DefCollector<CondDef> {

		private final Code code;
		private final CondDef[] explicitDefs;
		private final Code[] blocks;
		private int size;
		private int ancestorIndex = -1;
		private boolean hasExplicit;

		CondCollector(Code code, Obj object, int capacity) {
			super(object);
			this.code = code;
			this.explicitDefs = new CondDef[capacity];
			this.blocks = new Code[capacity];
		}

		public final CondDef[] getExplicitDefs() {
			return this.explicitDefs;
		}

		public final int size() {
			return this.size;
		}

		@Override
		protected void explicitDef(CondDef def) {
			this.hasExplicit = true;
			this.blocks[this.size] = this.code.addBlock(this.size + "_cvar");
			this.explicitDefs[this.size++] = def;
		}

		@Override
		protected void ancestorDef(CondDef def) {
			if (this.ancestorIndex >= 0) {
				return;
			}
			this.ancestorIndex = this.size;
			this.blocks[this.size] = this.code.addBlock(this.size + "_cvar");
			++this.size;
		}

		CodePos next(int index, CodePos defaultPos) {
			for (int i = index + 1; i < this.blocks.length; ++i) {

				final Code block = this.blocks[i];

				if (block != null) {
					return block.head();
				}
			}
			return defaultPos;
		}

		CodePos nextRequired(int index, CodePos defaultPos) {
			for (int i = index + 1; i < this.size; ++i) {

				final CondDef def = this.explicitDefs[i];

				if (def == null) {
					if (i == this.ancestorIndex) {
						return this.blocks[i].head();
					}
					continue;
				}
				if (def.hasPrerequisite()) {
					continue;
				}
				return this.blocks[i].head();
			}
			return defaultPos;
		}

	}

}
