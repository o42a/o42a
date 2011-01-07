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
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.*;
import org.o42a.core.ir.op.ObjectCondFunc;
import org.o42a.core.value.LogicalValue;


abstract class ObjectValueIRCondFunc extends ObjectValueIRFunc<ObjectCondFunc> {

	ObjectValueIRCondFunc(ObjectIR objectIR) {
		super(objectIR);
	}

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
				set(typeIR, getGenerator().trueFunc());
				return;
			}
		} else if (condition.isDefinite()) {
			if (condition.isFalse()) {
				set(typeIR, getGenerator().falseFunc());
			} else {
				set(typeIR, getGenerator().trueFunc());
			}
			return;
		}

		final Function<ObjectCondFunc> function =
			getGenerator().newFunction().create(
					getName(),
					getGenerator().objectCondSignature());

		function.debug("Calculating condition");
		set(typeIR, function.getPointer());
	}

	public void setFalse(ObjectTypeIR typeIR) {
		set(typeIR, getGenerator().falseFunc());
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
				0,
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
			if (condition.isRequirement()
					|| !hasExplicitProposition(definitions)) {
				writeAncestorDef(code, exit, host, condition);
			}
			return;
		}

		final Builder builder =
			new Builder(getObjectIR().getObject(), code, exit, host);

		if (condition.isRequirement()) {
			writeAncestorDef(code, exit, host, condition);
			builder.addDefs(condition.getCondDef().getRequirements());
			return;
		}

		builder.addDefs(condition.getCondDef().getRequirements());
		if (builder.hasExplicitDefs) {
			return;
		}
		if (hasExplicitProposition(definitions)) {
			return;
		}
		writeAncestorDef(code, exit, host, condition);
	}

	protected abstract void build(
			Code code,
			CodePos exit,
			ObjOp host,
			Definitions definitions);

	private void writeAncestorDef(
			Code code,
			CodePos exit,
			ObjOp host,
			DefValue condition) {

		final CondBlk hasAncestor =
			host.hasAncestor(code).branch(code, "has_ancestor", "no_ancestor");
		final CodeBlk noAncestor = hasAncestor.otherwise();

		noAncestor.go(code.tail());

		final ObjectOp ancestorBody = host.ancestor(hasAncestor);
		final ObjectDataOp ancestorData =
			ancestorBody.methods(hasAncestor)
			.objectType(hasAncestor)
			.load(hasAncestor)
			.data(host.getBuilder(), hasAncestor, DERIVED);

		if (condition.isRequirement()) {
			ancestorData.writeRequirement(hasAncestor, exit, ancestorBody);
		} else {
			ancestorData.writePostCondition(hasAncestor, exit, ancestorBody);
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

	private static final class Builder extends DefCollector<CondDef> {

		private final Code code;
		private final CodePos exit;
		private final ObjOp host;
		private boolean hasExplicitDefs;

		Builder(Obj object, Code code, CodePos exit, ObjOp host) {
			super(object);
			this.code = code;
			this.exit = exit;
			this.host = host;
		}

		@Override
		protected void explicitDef(CondDef def) {
			def.writeFullCondition(this.code, this.exit, this.host);
			this.hasExplicitDefs = true;
		}

	}

}
