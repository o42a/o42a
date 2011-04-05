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
import static org.o42a.core.ir.object.value.DefCollector.explicitDef;
import static org.o42a.core.ir.op.CodeDirs.splitWhenUnknown;
import static org.o42a.core.ir.op.ObjectCondFunc.OBJECT_COND;
import static org.o42a.core.ir.op.Val.CONDITION_FLAG;
import static org.o42a.core.ir.op.Val.UNKNOWN_FLAG;

import org.o42a.codegen.code.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.*;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectCondFunc;
import org.o42a.core.value.LogicalValue;


public abstract class ObjectValueIRCondFunc
		extends ObjectValueIRFunc<ObjectCondFunc> {

	public ObjectValueIRCondFunc(ObjectIR objectIR) {
		super(objectIR);
	}

	public abstract boolean isRequirement();

	public void call(CodeDirs dirs, ObjOp host, ObjectOp body) {

		final Code code = dirs.code();

		if (dirs.isDebug()) {
			dirs = dirs.begin("calc_cond", "Calculate condition " + this);
			if (body != null) {
				code.dumpName("For: ", body.toData(code));
			}
		}

		final Obj object = getObjectIR().getObject();
		final DefValue condition = value(object.getDefinitions());
		final LogicalValue logicalValue = logicalValue(condition, body);

		if (logicalValue.isConstant()) {
			if (logicalValue.isFalse()) {
				code.debug("False");
				dirs.goWhenFalse(code);
			} else {
				code.debug("True");
			}
			dirs.end();
			return;
		}

		get(host).op(code).call(
				code,
				body(code, host, body)).go(code, dirs);

		dirs.end();
	}

	public void create(ObjectTypeIR typeIR, Definitions definitions) {

		final DefValue condition = value(definitions);

		if (condition.isUnknown()) {
			if (!getObjectIR().getObject().isRuntime()) {
				set(typeIR, trueFunc());
				return;
			}
		} else if (condition.isDefinite()) {
			if (condition.isFalse()) {
				set(typeIR, falseFunc());
			} else {
				set(typeIR, trueFunc());
			}
			return;
		}

		final Function<ObjectCondFunc> function =
			getGenerator().newFunction().create(getId(), OBJECT_COND);

		set(typeIR, function.getPointer());
	}

	public void setFalse(ObjectTypeIR typeIR) {
		set(typeIR, falseFunc());
	}

	public void build(Definitions definitions) {

		final Function<ObjectCondFunc> function = get().getFunction();

		if (function == null) {
			return;
		}

		function.debug("Calculating condition");

		final CodeBlk condFalse = function.addBlock("false");
		final CodeBlk condUnknown = function.addBlock("unknown");
		final ObjBuilder builder = new ObjBuilder(
				function,
				condFalse.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				DERIVED);
		final ObjOp host = builder.host();

		function.dumpName("Host: ", host.ptr());
		final CodeDirs dirs =
			splitWhenUnknown(function, condFalse.head(), condUnknown.head());

		build(dirs, host, definitions);

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

	public void buildFunc(CodeDirs dirs, ObjOp host, Definitions definitions) {

		final DefValue condition = value(definitions);

		if (condition.isUnknown()) {
			if (isRequirement() || !hasExplicitProposition(definitions)) {
				writeAncestorDef(dirs, host);
			}
			return;
		}

		final Builder builder =
			new Builder(getObjectIR().getObject(), dirs, host);

		if (isRequirement()) {
			writeAncestorDef(dirs, host);
			builder.ancestorWritten = true;
			builder.addDefs(definitions.getRequirements());
			return;
		}

		builder.addDefs(definitions.getConditions());
		if (builder.written) {
			return;
		}
		if (hasExplicitProposition(definitions)) {
			return;
		}
		writeAncestorDef(dirs, host);
	}

	protected abstract void build(
			CodeDirs dirs,
			ObjOp host,
			Definitions definitions);

	private void writeAncestorDef(CodeDirs dirs, ObjOp host) {

		final Code code = dirs.code();
		final CondBlk hasAncestor =
			host.hasAncestor(code).branch(code, "has_ancestor", "no_ancestor");
		final CodeBlk noAncestor = hasAncestor.otherwise();

		noAncestor.debug(
				isRequirement()
				? "No ancestor requirement" : "No ancestor condition");
		dirs.goWhenUnknown(noAncestor);

		final ObjectOp ancestorBody = host.ancestor(hasAncestor);
		final ObjectTypeOp ancestorType =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(hasAncestor)
			.op(host.getBuilder(), DERIVED);

		final CodeBlk ancestorFalse = hasAncestor.addBlock("ancestor_false");
		final CodeBlk ancestorUnknown =
			hasAncestor.addBlock("ancestor_unknown");
		CodeDirs ancestorDirs = splitWhenUnknown(
				hasAncestor,
				ancestorFalse.head(),
				ancestorUnknown.head());

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

		dirs.goWhenTrue(hasAncestor);
		if (ancestorFalse.exists()) {
			dirs.goWhenFalse(ancestorFalse);
		}
		if (ancestorUnknown.exists()) {
			dirs.goWhenUnknown(ancestorUnknown);
		}
	}

	private boolean hasExplicitProposition(Definitions definitions) {

		final Obj object = getObjectIR().getObject();

		for (ValueDef proposition : definitions.getPropositions()) {
			if (explicitDef(object, proposition)) {
				return true;
			}
		}

		return false;
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

	private final class Builder extends DefCollector<CondDef> {

		private final CodeDirs dirs;
		private final ObjOp host;
		private boolean written;
		private boolean ancestorWritten;

		Builder(Obj object, CodeDirs dirs, ObjOp host) {
			super(object);
			this.dirs = dirs;
			this.host = host;
		}

		@Override
		protected void explicitDef(CondDef def) {
			def.writeLogicalValue(this.dirs, this.host);
			this.written = true;
		}

		@Override
		protected void ancestorDef(CondDef def) {
			this.written = true;
			if (this.ancestorWritten) {
				return;
			}
			writeAncestorDef(this.dirs, this.host);
			this.ancestorWritten = true;
		}

	}

}
