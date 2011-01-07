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

import static org.o42a.core.ir.op.Val.FALSE_VAL;
import static org.o42a.core.ir.op.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.op.Val.UNKNOWN_VAL;

import java.util.ArrayList;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.data.CodeRec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.op.*;
import org.o42a.core.value.Value;


public class ObjectValueIR {

	private final ObjectIR objectIR;

	private final ObjectValue value;
	private final Requirement requirement;
	private final Claim claim;
	private final PostCondition postCondition;
	private final Proposition proposition;
	private ArrayList<LocalIRFunc> locals;

	private boolean filled;

	public ObjectValueIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.value = new ObjectValue(objectIR);
		this.requirement = new Requirement(objectIR);
		this.claim = new Claim(objectIR);
		this.postCondition = new PostCondition(objectIR);
		this.proposition = new Proposition(objectIR);
	}

	public final IRGenerator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	@Override
	public String toString() {
		return this.objectIR + " Value IR";
	}

	protected void writeValue(
			Code code,
			ValOp result,
			ObjOp host,
			ObjectOp body) {
		this.value.call(code, result, host, body);

		final Code stillIndefinite = code.addBlock("still_indefinite");

		result.indefinite(code).go(code, stillIndefinite.head());
		result.storeUnknown(stillIndefinite);
		stillIndefinite.go(code.tail());
	}

	protected void writeRequirement(
			Code code,
			CodePos exit,
			ObjOp host,
			ObjectOp body) {
		this.requirement.call(code, exit, host, body);
	}

	protected void writeClaim(
			Code code,
			ValOp result,
			ObjOp host,
			ObjectOp body) {
		this.claim.call(code, result, host, body);
	}

	protected void writePostCondition(
			Code code,
			CodePos exit,
			ObjOp host,
			ObjectOp body) {
		this.postCondition.call(code, exit, host, body);
	}

	protected void writeProposition(
			Code code,
			ValOp result,
			ObjOp host,
			ObjectOp body) {
		this.proposition.call(code, result, host, body);
	}

	protected void allocate(ObjectTypeIR typeIR) {

		final Obj object = getObjectIR().getObject();
		final Definitions definitions = object.getDefinitions();

		if (!object.isRuntime() && definitions.getPostCondition().isFalse()
				|| definitions.getRequirement().isFalse()) {
			createFalseFunctions(typeIR, definitions);
		} else {
			createFunctions(typeIR, definitions);
		}
	}

	protected void fill(ObjectTypeIR typeIR) {

		final Definitions definitions = definitions();

		assignValue(typeIR, definitions);
		buildFunctions(typeIR, definitions);
		buildLocals();
	}

	protected void createValue(ObjectTypeIR typeIR, Definitions definitions) {
		this.value.create(typeIR, definitions);
	}

	protected void buildValue(
			Code code,
			ValOp result,
			ObjOp host,
			Definitions definitions) {

		final CodeBlk done = code.addBlock("done");

		final CodeBlk postConditionFailed =
			code.addBlock("post_condition_failed");

		writePostCondition(code, postConditionFailed.head(), host, null);
		if (postConditionFailed.exists()) {
			result.storeFalse(postConditionFailed);
			postConditionFailed.returnVoid();
		}

		this.claim.call(code, result, host, null);
		result.unknown(code).goUnless(code, done.head());

		this.proposition.call(code, result, host, null);
		result.unknown(code).goUnless(code, done.head());

		result.storeUnknown(code);// to override indefinite value
		code.returnVoid();
		if (done.exists()) {
			done.returnVoid();
		}
	}

	protected void createRequirement(
			ObjectTypeIR typeIR,
			Definitions definitions) {
		this.requirement.create(typeIR, definitions);
	}

	protected void buildRequirement(
			Code code,
			CodePos exit,
			ObjOp host,
			Definitions definitions) {
		this.requirement.buildFunc(code, exit, host, definitions);
	}

	protected void createClaim(ObjectTypeIR typeIR, Definitions definitions) {
		this.claim.create(typeIR, definitions);
	}

	protected void buildClaim(
			Code code,
			ValOp result,
			ObjOp host,
			Definitions definitions) {
		this.claim.buildFunc(code, result, host, definitions, true);
	}

	protected void createPostCondition(
			ObjectTypeIR typeIR,
			Definitions definitions) {
		this.postCondition.create(typeIR, definitions);
	}

	protected void buildPostCondition(
			Code code,
			CodePos exit,
			ObjOp host,
			Definitions definitions) {
		this.postCondition.buildFunc(code, exit, host, definitions);
	}

	protected void createProposition(
			ObjectTypeIR typeIR,
			Definitions definitions) {
		this.proposition.create(typeIR, definitions);
	}

	protected void buildProposition(
			Code code,
			ValOp result,
			ObjOp host,
			Definitions definitions) {
		this.proposition.buildFunc(code, result, host, definitions, false);
	}

	final void addLocal(LocalIRFunc local) {
		if (this.locals == null) {
			this.locals = new ArrayList<LocalIRFunc>();
			this.locals.add(local);
			getObjectIR().allocate();
		} else {
			this.locals.add(local);
		}
		if (this.filled) {
			local.build();
		}
	}

	private final Definitions definitions() {
		return getObjectIR().getObject().getDefinitions();
	}

	private void assignValue(ObjectTypeIR typeIR, Definitions definitions) {

		final Val val;

		final DefValue value = definitions.value(definitions.getScope());
		final Value<?> realValue = value.getRealValue();

		if (realValue != null) {
			if (realValue.isDefinite()) {
				val = realValue.val(getGenerator());
			} else {
				val = INDEFINITE_VAL;
			}
		} else if (value.isUnknown()) {
			val = UNKNOWN_VAL;
		} else {
			val = FALSE_VAL;
		}

		typeIR.getObjectData().getValue().set(val);
	}

	private void createFalseFunctions(
			ObjectTypeIR typeIR,
			Definitions definitions) {
		this.value.setFalse(typeIR);
		if (definitions.getRequirement().isFalse()) {
			this.requirement.setFalse(typeIR);
			this.claim.setFalse(typeIR);
		} else {
			createClaimFunctions(typeIR, definitions);
		}
		this.postCondition.setFalse(typeIR);
		this.proposition.setFalse(typeIR);
	}

	private void createFunctions(ObjectTypeIR typeIR, Definitions definitions) {
		createValue(typeIR, definitions);
		createClaimFunctions(typeIR, definitions);
		createPostCondition(typeIR, definitions);
		createProposition(typeIR, definitions);
	}

	private void buildFunctions(ObjectTypeIR typeIR, Definitions definitions) {
		this.value.build(definitions);
		this.requirement.build(definitions);
		this.claim.build(definitions);
		this.postCondition.build(definitions);
		this.proposition.build(definitions);
	}

	private void buildLocals() {
		if (this.locals == null) {
			return;
		}
		for (LocalIRFunc local : this.locals) {
			local.build();
		}
	}

	private void createClaimFunctions(
			ObjectTypeIR typeIR,
			Definitions definitions) {
		createRequirement(typeIR, definitions);
		createClaim(typeIR, definitions);
	}

	private final class ObjectValue extends ObjectValueIRValFunc {

		private ObjectValue(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected String suffix() {
			return "value";
		}

		@Override
		protected CodeRec<ObjectValFunc> func(ObjectDataType data) {
			return data.getValueFunc();
		}

		@Override
		protected DefValue value(Definitions definitions) {
			return definitions.value(definitions.getScope());
		}

		@Override
		protected void build(
				Code code,
				ValOp result,
				ObjOp host,
				Definitions definitions) {
			buildValue(code, result, host, definitions);
		}

	}

	private final class Requirement extends ObjectValueIRCondFunc {

		private Requirement(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected String suffix() {
			return "requirement";
		}

		@Override
		protected DefValue value(Definitions definitions) {
			return definitions.requirement(definitions.getScope());
		}

		@Override
		protected CodeRec<ObjectCondFunc> func(ObjectDataType data) {
			return data.getRequirementFunc();
		}

		@Override
		protected void build(
				Code code,
				CodePos exit,
				ObjOp host,
				Definitions definitions) {
			buildRequirement(code, exit, host, definitions);
		}

	}

	private final class Claim extends ObjectValueIRValFunc {

		private Claim(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected String suffix() {
			return "claim";
		}

		@Override
		protected DefValue value(Definitions definitions) {
			return definitions.claim(definitions.getScope());
		}

		@Override
		protected CodeRec<ObjectValFunc> func(ObjectDataType data) {
			return data.getClaimFunc();
		}

		@Override
		protected void build(
				Code code,
				ValOp result,
				ObjOp host,
				Definitions definitions) {
			buildClaim(code, result, host, definitions);
		}

	}

	private final class PostCondition extends ObjectValueIRCondFunc {

		private PostCondition(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected String suffix() {
			return "post_condition";
		}

		@Override
		protected DefValue value(Definitions definitions) {
			return definitions.postCondition(definitions.getScope());
		}

		@Override
		protected CodeRec<ObjectCondFunc> func(ObjectDataType data) {
			return data.getPostConditionFunc();
		}

		@Override
		protected void build(
				Code code,
				CodePos exit,
				ObjOp host,
				Definitions definitions) {
			buildPostCondition(code, exit, host, definitions);
		}

	}

	private final class Proposition extends ObjectValueIRValFunc {

		private Proposition(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected String suffix() {
			return "proposition";
		}

		@Override
		protected DefValue value(Definitions definitions) {
			return definitions.proposition(definitions.getScope());
		}

		@Override
		protected CodeRec<ObjectValFunc> func(ObjectDataType data) {
			return data.getPropositionFunc();
		}

		@Override
		protected void build(
				Code code,
				ValOp result,
				ObjOp host,
				Definitions definitions) {
			buildProposition(code, result, host, definitions);
		}

	}

}
