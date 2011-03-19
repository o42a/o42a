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
import static org.o42a.core.ir.op.ObjectCondFunc.OBJECT_COND;

import org.o42a.codegen.code.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.*;
import org.o42a.core.ir.op.ObjectCondFunc;
import org.o42a.core.value.LogicalValue;


abstract class ObjectValueIRCondFunc extends ObjectValueIRFunc<ObjectCondFunc> {

	ObjectValueIRCondFunc(ObjectIR objectIR) {
		super(objectIR);
	}

	public abstract boolean isRequirement();

	public void call(
			Code code,
			CodePos exit,
			ObjOp host,
			ObjectOp body) {
		if (body != null) {
			code.debug("Calculate condition " + this + " for " + body);
		} else {
			code.debug("Calculate condition " + this);
		}

		final Obj object = getObjectIR().getObject();
		final DefValue condition = value(object.getDefinitions());
		final LogicalValue logicalValue = logicalValue(condition, body);

		if (logicalValue.isConstant()) {
			if (logicalValue.isFalse()) {
				code.debug("False");
				code.go(exit);
			} else {
				code.debug("True");
			}
			return;
		}

		get(host).op(code).call(
				code,
				body(code, host, body)).goUnless(code, exit);
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

		function.debug("Calculating condition");
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

		final CodeBlk exit = function.addBlock("exit");
		final ObjBuilder builder = new ObjBuilder(
				function,
				exit.head(),
				getObjectIR().getMainBodyIR(),
				getObjectIR().getObject(),
				DERIVED);
		final ObjOp host = builder.host();

		function.dumpName("Host: ", host.ptr());
		build(function, exit.head(), host, definitions);

		function.bool(true).returnValue(function);
		if (exit.exists()) {
			exit.bool(false).returnValue(exit);
		}

		function.done();
	}

	public void buildFunc(
			Code code,
			CodePos exit,
			ObjOp host,
			Definitions definitions) {

		final DefValue condition = value(definitions);

		if (condition.isUnknown()) {
			if (isRequirement() || !hasExplicitProposition(definitions)) {
				writeAncestorDef(code, exit, host);
			}
			return;
		}

		final Builder builder =
			new Builder(getObjectIR().getObject(), code, exit, host);

		if (isRequirement()) {
			writeAncestorDef(code, exit, host);
			builder.ancestorWritten = true;
			builder.addDefs(condition.getLogicalDef().getRequirements());
			return;
		}

		builder.addDefs(condition.getLogicalDef().getRequirements());
		if (builder.written) {
			return;
		}
		if (hasExplicitProposition(definitions)) {
			return;
		}
		writeAncestorDef(code, exit, host);
	}

	protected abstract void build(
			Code code,
			CodePos exit,
			ObjOp host,
			Definitions definitions);

	private void writeAncestorDef(
			Code code,
			CodePos exit,
			ObjOp host) {

		final CondBlk hasAncestor =
			host.hasAncestor(code).branch(code, "has_ancestor", "no_ancestor");
		final CodeBlk noAncestor = hasAncestor.otherwise();

		noAncestor.debug(
				isRequirement()
				? "No ancestor requirement" : "No ancestor condition");
		noAncestor.go(code.tail());

		final ObjectOp ancestorBody = host.ancestor(hasAncestor);
		final ObjectTypeOp ancestorType =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(hasAncestor)
			.op(host.getBuilder(), DERIVED);

		if (isRequirement()) {
			if (hasAncestor.isDebug()) {
				hasAncestor.dumpName(
						"Ancestor requirement: ",
						ancestorBody.toData(code));
			}
			ancestorType.writeRequirement(hasAncestor, exit, ancestorBody);
		} else {
			if (hasAncestor.isDebug()) {
				hasAncestor.dumpName(
						"Ancestor condition: ",
						ancestorBody.toData(code));
			}
			ancestorType.writeCondition(hasAncestor, exit, ancestorBody);
		}

		hasAncestor.go(code.tail());
	}

	private boolean hasExplicitProposition(Definitions definitions) {

		final Obj object = getObjectIR().getObject();

		for (Def proposition : definitions.getPropositions()) {
			if (proposition.getSource() == object) {
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

	private final class Builder extends DefCollector<LogicalDef> {

		private final Code code;
		private final CodePos exit;
		private final ObjOp host;
		private boolean written;
		private boolean ancestorWritten;

		Builder(Obj object, Code code, CodePos exit, ObjOp host) {
			super(object);
			this.code = code;
			this.exit = exit;
			this.host = host;
		}

		@Override
		protected void explicitDef(LogicalDef def) {
			def.writeFullLogical(this.code, this.exit, this.host);
			this.written = true;
		}

		@Override
		protected void ancestorDef(LogicalDef def) {
			this.written = true;
			if (this.ancestorWritten) {
				return;
			}
			writeAncestorDef(this.code, this.exit, this.host);
			this.ancestorWritten = true;
		}

	}

}
