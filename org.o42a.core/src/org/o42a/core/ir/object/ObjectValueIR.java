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
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.CondValue;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.impl.ObjectIRLocals;
import org.o42a.core.ir.object.impl.value.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;


public class ObjectValueIR {

	private final ObjectIR objectIR;

	private final ObjectIRLocals locals;
	private final ObjectValueFunc value;
	private final ObjectRequirementFunc requirement;
	private final ObjectClaimFunc claim;
	private final ObjectConditionFunc condition;
	private final ObjectPropositionFunc proposition;

	protected ObjectValueIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.locals = new ObjectIRLocals(this);
		this.value = new ObjectValueFunc(this);
		this.requirement = new ObjectRequirementFunc(this);
		this.claim = new ObjectClaimFunc(this);
		this.condition = new ObjectConditionFunc(this);
		this.proposition = new ObjectPropositionFunc(this);
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

	public final CondValue getConstantRequirement() {
		return this.requirement.getConstant();
	}

	public final CondValue getConstantCondition() {
		return this.condition.getConstant();
	}

	public ObjValOp op(CodeBuilder builder, Code code) {
		return getObjectIR().op(builder, code);
	}

	public final ValOp writeValue(ValDirs dirs, ObjOp host, ObjectOp body) {
		return this.value.call(dirs, host, body);
	}

	public final void writeRequirement(
			CodeDirs dirs,
			ObjOp host,
			ObjectOp body) {
		this.requirement.call(dirs, host, body);
	}

	public final ValOp writeClaim(ValDirs dirs, ObjOp host, ObjectOp body) {
		return this.claim.call(dirs, host, body);
	}

	public final void writeCondition(CodeDirs dirs, ObjOp host, ObjectOp body) {
		this.condition.call(dirs, host, body);
	}

	public final ValOp writeProposition(
			ValDirs dirs,
			ObjOp host,
			ObjectOp body) {
		return this.proposition.call(dirs, host, body);
	}

	@Override
	public String toString() {
		return this.objectIR + " Value IR";
	}

	protected void allocate(ObjectTypeIR typeIR) {

		final Definitions definitions = definitions();

		if (definitions.requirements().isFalse()
				|| definitions.conditions().isFalse()) {
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

	protected void createValue(ObjectTypeIR typeIR) {
		this.value.create(typeIR);
	}


	protected void createRequirement(
			ObjectTypeIR typeIR) {
		this.requirement.create(typeIR);
	}

	protected void createClaim(ObjectTypeIR typeIR) {
		this.claim.create(typeIR);
	}

	protected void createCondition(
			ObjectTypeIR typeIR) {
		this.condition.create(typeIR);
	}

	protected void createProposition(
			ObjectTypeIR typeIR) {
		this.proposition.create(typeIR);
	}

	final ObjectIRLocals getLocals() {
		return this.locals;
	}

	private final Definitions definitions() {
		return getObject().value().getDefinitions();
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
		if (definitions.requirements().isFalse()) {
			this.requirement.setFalse(typeIR);
			this.claim.setFalse(typeIR);
		} else {
			createClaimFunctions(typeIR);
		}
		this.condition.setFalse(typeIR);
		this.proposition.setFalse(typeIR);
	}

	private void createFunctions(ObjectTypeIR typeIR, Definitions definitions) {
		createValue(typeIR);
		createClaimFunctions(typeIR);
		createCondition(typeIR);
		createProposition(typeIR);
	}

	private void buildFunctions(ObjectTypeIR typeIR, Definitions definitions) {
		this.value.build();
		this.requirement.build(definitions);
		this.claim.build();
		this.condition.build(definitions);
		this.proposition.build();
	}

	private void createClaimFunctions(
			ObjectTypeIR typeIR) {
		createRequirement(typeIR);
		createClaim(typeIR);
	}

}
