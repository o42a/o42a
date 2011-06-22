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

import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.value.Val.UNKNOWN_VAL;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.value.ObjectIRLocals;
import org.o42a.core.ir.object.value.ObjectValueIRCondFunc;
import org.o42a.core.ir.object.value.ObjectValueIRValFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectCondFunc;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.*;
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

	public final Obj getObject() {
		return getObjectIR().getObject();
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

	protected ValOp writeValue(ValDirs dirs, ObjOp host, ObjectOp body) {
		return this.value.call(dirs, host, body);
	}

	protected void writeRequirement(CodeDirs dirs, ObjOp host, ObjectOp body) {
		this.requirement.call(dirs, host, body);
	}

	protected ValOp writeClaim(ValDirs dirs, ObjOp host, ObjectOp body) {
		return this.claim.call(dirs, host, body);
	}

	protected void writeCondition(CodeDirs dirs, ObjOp host, ObjectOp body) {
		this.condition.call(dirs, host, body);
	}

	protected ValOp writeProposition(ValDirs dirs, ObjOp host, ObjectOp body) {
		return this.proposition.call(dirs, host, body);
	}

	protected void allocate(ObjectTypeIR typeIR) {

		final Obj object = getObject();
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

	protected ValOp buildValue(
			ValDirs dirs,
			ObjOp host,
			Definitions definitions) {

		final Code code = dirs.code();

		writeRequirement(dirs.dirs(), host, null);
		writeCondition(dirs.dirs(), host, null);

		final Code unknownClaim = dirs.addBlock("unknown_claim");
		final ValDirs claimDirs =
			dirs.dirs().splitWhenUnknown(
					dirs.dirs().falseDir(),
					unknownClaim.head())
			.value(dirs);
		final ValType.Op claim =
			code.phi(null, writeClaim(claimDirs, host, null).ptr());

		claimDirs.done();

		final ValDirs propDirs = dirs.sub(unknownClaim);
		final ValType.Op prop = unknownClaim.phi(
				null,
				writeProposition(propDirs, host, null).ptr());

		propDirs.done();
		unknownClaim.go(code.tail());

		return code.phi(null, claim, prop).op(
				dirs.getBuilder(),
				getObject().getValueType());
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

	protected ValOp buildClaim(
			ValDirs dirs,
			ObjOp host,
			Definitions definitions) {
		return this.claim.buildFunc(dirs, host, definitions);
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

	protected ValOp buildProposition(
			ValDirs dirs,
			ObjOp host,
			Definitions definitions) {
		return this.proposition.buildFunc(dirs, host, definitions);
	}

	final ObjectIRLocals getLocals() {
		return this.locals;
	}

	private final Definitions definitions() {
		return getObject().getDefinitions();
	}

	private void assignValue(ObjectTypeIR typeIR, Definitions definitions) {

		final Val val;
		final Resolver resolver = definitions.getScope().dummyResolver();
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
		protected FuncRec<ObjectValFunc> func(ObjectIRData data) {
			return data.valueFunc();
		}

		@Override
		protected DefValue value(Definitions definitions) {

			final Resolver resolver = definitions.getScope().dummyResolver();

			return definitions.value(resolver);
		}

		@Override
		protected ValOp build(
				ValDirs dirs,
				ObjOp host,
				Definitions definitions) {
			return buildValue(dirs, host, definitions);
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

			final Resolver resolver = definitions.getScope().dummyResolver();

			return definitions.requirement(resolver);
		}

		@Override
		protected FuncRec<ObjectCondFunc> func(ObjectIRData data) {
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

			final Resolver resolver = definitions.getScope().dummyResolver();

			return definitions.claim(resolver);
		}

		@Override
		protected FuncRec<ObjectValFunc> func(ObjectIRData data) {
			return data.claimFunc();
		}

		@Override
		protected ValOp build(
				ValDirs dirs,
				ObjOp host,
				Definitions definitions) {
			return buildClaim(dirs, host, definitions);
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

			final Resolver resolver = definitions.getScope().dummyResolver();

			return definitions.condition(resolver);
		}

		@Override
		protected FuncRec<ObjectCondFunc> func(ObjectIRData data) {
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

			final Resolver resolver = definitions.getScope().dummyResolver();

			return definitions.proposition(resolver);
		}

		@Override
		protected FuncRec<ObjectValFunc> func(ObjectIRData data) {
			return data.propositionFunc();
		}

		@Override
		protected ValOp build(
				ValDirs dirs,
				ObjOp host,
				Definitions definitions) {
			return buildProposition(dirs, host, definitions);
		}

	}

}
