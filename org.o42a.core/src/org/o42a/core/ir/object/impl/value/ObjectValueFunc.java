/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.artifact.object.ValuePart;
import org.o42a.core.def.ValueDefs;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;


public final class ObjectValueFunc extends ObjectValueIRValFunc {

	private boolean functionReused;

	public ObjectValueFunc(ObjectValueIR valueIR) {
		super(valueIR);
	}

	@Override
	public final ValuePart valuePart() {
		return null;
	}

	@Override
	public final ValueDefs defs() {
		return null;
	}

	@Override
	public void create(ObjectTypeIR typeIR) {

		final FuncPtr<ObjectValFunc> knownFunc = reuseFunc();

		if (knownFunc != null) {
			set(typeIR, knownFunc);
			this.functionReused = true;
			return;
		}

		super.create(typeIR);
	}

	@Override
	public void build() {
		if (this.functionReused) {
			return;
		}
		super.build();
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
	protected Value<?> determineConstant() {

		final Condition constantCondition =
				getValueIR().getConstantCondition();

		if (!constantCondition.isConstant()) {
			return getValueType().runtimeValue();
		}

		final Value<?> claim = getValueIR().getConstantClaim();

		if (!claim.isUnknown()) {
			return claim;
		}

		return getValueIR().getConstantProposition();
	}

	@Override
	protected ValOp build(ValDirs dirs, ObjOp host) {

		final Code code = dirs.code();

		final Code unknownReq = dirs.addBlock("unknown_req");
		final CodeDirs reqDirs = dirs.dirs().splitWhenUnknown(
				dirs.dirs().falseDir(),
				unknownReq.head());

		getValueIR().writeRequirement(reqDirs, host, null);
		if (unknownReq.exists()) {
			unknownReq.go(code.tail());
		}

		getValueIR().writeCondition(dirs.dirs(), host, null);

		final Code unknownClaim = dirs.addBlock("unknown_claim");
		final ValDirs claimDirs =
				dirs.dirs().splitWhenUnknown(
						dirs.dirs().falseDir(),
						unknownClaim.head())
				.value(dirs);
		final ValType.Op claim = code.phi(
				null,
				getValueIR().writeClaim(claimDirs, host, null).ptr());

		claimDirs.done();

		final ValDirs propDirs =
				dirs.sub(unknownClaim)
				.setStoreMode(INITIAL_VAL_STORE);
		final ValType.Op prop = unknownClaim.phi(
				null,
				getValueIR().writeProposition(propDirs, host, null).ptr());

		propDirs.done();
		unknownClaim.go(code.tail());

		return code.phi(null, claim, prop).op(
				dirs.getBuilder(),
				getObject().value().getValueType());
	}

	private FuncPtr<ObjectValFunc> reuseFunc() {

		final ObjectValueIR valueIR = getValueIR();

		if (!valueIR.getConstantCondition().isTrue()) {
			return null;
		}

		final Value<?> claim = valueIR.getConstantClaim();
		final Value<?> proposition = valueIR.getConstantProposition();

		if (claim.isDefinite()) {
			if (claim.getCondition() == Condition.FALSE) {
				return falseValFunc();
			}
			return valueIR.proposition().get();
		}
		if (proposition.isUnknown()) {
			return valueIR.claim().get();
		}

		return null;
	}

}
