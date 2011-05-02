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

import static org.o42a.core.ir.op.CodeDirs.splitWhenUnknown;
import static org.o42a.core.ir.op.Val.FALSE_VAL;
import static org.o42a.core.ir.op.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.op.Val.UNKNOWN_VAL;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.value.ObjectIRLocals;
import org.o42a.core.ir.object.value.ObjectValueIRCondFunc;
import org.o42a.core.ir.object.value.ObjectValueIRValFunc;
import org.o42a.core.ir.op.*;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;


public class ObjectValueIR {

	private final ObjectIR objectIR;

	private final ObjectIRLocals locals;
	private final ObjectValue value;
	private final Requirement requirement;
	private final Claim claim;
	private final Condition condition;
	private final Proposition proposition;

	public ObjectValueIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.locals = new ObjectIRLocals(this);
		this.value = new ObjectValue(objectIR);
		this.requirement = new Requirement(objectIR);
		this.claim = new Claim(objectIR);
		this.condition = new Condition(objectIR);
		this.proposition = new Proposition(objectIR);
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public ObjValOp op(CodeBuilder builder, Code code) {
		return getObjectIR().op(builder, code);
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

		result.loadIndefinite(null, code).go(code, stillIndefinite.head());
		result.storeUnknown(stillIndefinite);
		stillIndefinite.go(code.tail());
	}

	protected void writeRequirement(
			CodeDirs dirs,
			ObjOp host,
			ObjectOp body) {
		this.requirement.call(dirs, host, body);
	}

	protected void writeClaim(
			Code code,
			ValOp result,
			ObjOp host,
			ObjectOp body) {
		this.claim.call(code, result, host, body);
	}

	protected void writeCondition(
			CodeDirs dirs,
			ObjOp host,
			ObjectOp body) {
		this.condition.call(dirs, host, body);
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

		if (definitions.getConstantRequirement().isFalse()
				|| definitions.getConstantCondition().isFalse()) {
			createFalseFunctions(typeIR, definitions);
		} else {
			createFunctions(typeIR, definitions);
		}
	}

	protected void fill(ObjectTypeIR typeIR) {

		final Definitions definitions = definitions();

		assignValue(typeIR, definitions);
		buildFunctions(typeIR, definitions);
		this.locals.build();
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

		final CodeBlk conditionFalse = code.addBlock("condition_false");
		final CodeBlk conditionUnknwon = code.addBlock("condition_unknown");
		final CodeDirs conditionDirs = splitWhenUnknown(
				code,
				conditionFalse.head(),
				conditionUnknwon.head());

		writeRequirement(conditionDirs, host, null);
		writeCondition(conditionDirs, host, null);
		if (conditionFalse.exists()) {
			conditionFalse.debug("Object condition is FALSE");
			result.storeFalse(conditionFalse);
			conditionFalse.returnVoid();
		}
		if (conditionUnknwon.exists()) {
			// Override indefinite value.
			conditionUnknwon.debug("Object condition is UNKNOWN");
			result.storeUnknown(conditionUnknwon);
			conditionUnknwon.returnVoid();
		}

		writeClaim(code, result, host, null);
		result.loadIndefinite(null, code).goUnless(code, done.head());

		writeProposition(code, result, host, null);
		result.loadIndefinite(null, code).goUnless(code, done.head());

		result.storeUnknown(code);// Override indefinite value.
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
			CodeDirs dirs,
			ObjOp host,
			Definitions definitions) {
		this.requirement.buildFunc(dirs, host, definitions);
	}

	protected void createClaim(ObjectTypeIR typeIR, Definitions definitions) {
		this.claim.create(typeIR, definitions);
	}

	protected void buildClaim(
			Code code,
			ValOp result,
			ObjOp host,
			Definitions definitions) {
		this.claim.buildFunc(code, result, host, definitions);
	}

	protected void createCondition(
			ObjectTypeIR typeIR,
			Definitions definitions) {
		this.condition.create(typeIR, definitions);
	}

	protected void buildCondition(
			CodeDirs dirs,
			ObjOp host,
			Definitions definitions) {
		this.condition.buildFunc(dirs, host, definitions);
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
		this.proposition.buildFunc(code, result, host, definitions);
	}

	final ObjectIRLocals getLocals() {
		return this.locals;
	}

	private final Definitions definitions() {
		return getObjectIR().getObject().getDefinitions();
	}

	private void assignValue(ObjectTypeIR typeIR, Definitions definitions) {

		final Val val;
		final Resolver resolver =
			definitions.getScope().newResolver(dummyUser());
		final DefValue value = definitions.value(resolver);
		final Value<?> realValue = value.getRealValue();

		if (realValue != null) {
			val = realValue.val(getGenerator());
		} else if (!value.isDefinite()) {
			val = INDEFINITE_VAL;
		} else if (value.isUnknown()) {
			val = UNKNOWN_VAL;
		} else {
			val = FALSE_VAL;
		}

		typeIR.getObjectData().value().set(val);
	}

	private void createFalseFunctions(
			ObjectTypeIR typeIR,
			Definitions definitions) {
		this.value.setFalse(typeIR);
		if (definitions.getConstantRequirement().isFalse()) {
			this.requirement.setFalse(typeIR);
			this.claim.setFalse(typeIR);
		} else {
			createClaimFunctions(typeIR, definitions);
		}
		this.condition.setFalse(typeIR);
		this.proposition.setFalse(typeIR);
	}

	private void createFunctions(ObjectTypeIR typeIR, Definitions definitions) {
		createValue(typeIR, definitions);
		createClaimFunctions(typeIR, definitions);
		createCondition(typeIR, definitions);
		createProposition(typeIR, definitions);
	}

	private void buildFunctions(ObjectTypeIR typeIR, Definitions definitions) {
		this.value.build(definitions);
		this.requirement.build(definitions);
		this.claim.build(definitions);
		this.condition.build(definitions);
		this.proposition.build(definitions);
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
		public boolean isClaim() {
			return false;
		}

		@Override
		protected String suffix() {
			return "value";
		}

		@Override
		protected FuncRec<ObjectValFunc> func(ObjectDataType data) {
			return data.valueFunc();
		}

		@Override
		protected DefValue value(Definitions definitions) {

			final Resolver resolver =
				definitions.getScope().newResolver(dummyUser());

			return definitions.value(resolver);
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

		Requirement(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		public boolean isRequirement() {
			return true;
		}

		@Override
		protected String suffix() {
			return "requirement";
		}

		@Override
		protected DefValue value(Definitions definitions) {

			final Resolver resolver =
				definitions.getScope().newResolver(dummyUser());

			return definitions.requirement(resolver);
		}

		@Override
		protected FuncRec<ObjectCondFunc> func(ObjectDataType data) {
			return data.requirementFunc();
		}

		@Override
		protected void build(
				CodeDirs dirs,
				ObjOp host,
				Definitions definitions) {
			buildRequirement(dirs, host, definitions);
		}

	}

	private final class Claim extends ObjectValueIRValFunc {

		Claim(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		public boolean isClaim() {
			return true;
		}

		@Override
		protected String suffix() {
			return "claim";
		}

		@Override
		protected DefValue value(Definitions definitions) {

			final Resolver resolver =
				definitions.getScope().newResolver(dummyUser());

			return definitions.claim(resolver);
		}

		@Override
		protected FuncRec<ObjectValFunc> func(ObjectDataType data) {
			return data.claimFunc();
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

	private final class Condition extends ObjectValueIRCondFunc {

		Condition(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		public boolean isRequirement() {
			return false;
		}

		@Override
		protected String suffix() {
			return "condition";
		}

		@Override
		protected DefValue value(Definitions definitions) {

			final Resolver resolver =
				definitions.getScope().newResolver(dummyUser());

			return definitions.condition(resolver);
		}

		@Override
		protected FuncRec<ObjectCondFunc> func(ObjectDataType data) {
			return data.conditionFunc();
		}

		@Override
		protected void build(
				CodeDirs dirs,
				ObjOp host,
				Definitions definitions) {
			buildCondition(dirs, host, definitions);
		}

	}

	private final class Proposition extends ObjectValueIRValFunc {

		Proposition(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		public boolean isClaim() {
			return false;
		}

		@Override
		protected String suffix() {
			return "proposition";
		}

		@Override
		protected DefValue value(Definitions definitions) {

			final Resolver resolver =
				definitions.getScope().newResolver(dummyUser());

			return definitions.proposition(resolver);
		}

		@Override
		protected FuncRec<ObjectValFunc> func(ObjectDataType data) {
			return data.propositionFunc();
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
