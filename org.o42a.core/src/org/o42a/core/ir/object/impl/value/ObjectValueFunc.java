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
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;


public final class ObjectValueFunc extends ObjectValueIRValFunc {

	public ObjectValueFunc(ObjectValueIR valueIR) {
		super(valueIR);
	}

	@Override
	public final ValuePart part() {
		return null;
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
				getValueIR().condition().getConstant();

		if (!constantCondition.isConstant()) {
			return getValueStruct().runtimeValue();
		}

		final Value<?> claim = getValueIR().claim().getConstant();

		if (!claim.isUnknown()) {
			return claim;
		}

		return getValueIR().proposition().getConstant();
	}

	@Override
	protected Value<?> determineFinal() {
		if (!getValueIR().requirement().getFinal().isConstant()
				|| !getValueIR().condition().getFinal().isConstant()
				|| !getValueIR().claim().getFinal().isDefinite()
				|| !getValueIR().proposition().getFinal().isDefinite()) {
			return getValueStruct().runtimeValue();
		}

		return getObject().value().getDefinitions().value(
				getObject().getScope().dummyResolver());
	}

	@Override
	protected boolean canStub() {
		return false;
	}

	@Override
	protected void reuse() {

		final ObjectValueIR valueIR = getValueIR();

		if (!valueIR.condition().getFinal().isTrue()) {
			return;
		}

		final FuncPtr<ObjectValFunc> reused;
		final ObjectClaimFunc claim = valueIR.claim();
		final Value<?> finalClaim = claim.getFinal();

		if (finalClaim.isDefinite()) {
			if (finalClaim.getCondition() == Condition.FALSE) {
				reused = falseValFunc();
			} else {
				reused = valueIR.proposition().getNotStub();
				if (reused == null) {
					return;
				}
			}
		} else if (!valueIR.proposition().getFinal().isUnknown()) {
			return;
		} else {
			reused = claim.getNotStub();
			if (reused == null) {
				return;
			}
		}

		reuse(reused);
	}

	@Override
	protected ValOp build(ValDirs dirs, ObjOp host) {

		final Code code = dirs.code();

		writeRequirement(dirs.dirs(), host);
		getValueIR().writeCondition(dirs.dirs(), host, null);

		final Code unknownClaim = dirs.addBlock("unknown_claim");
		final ValDirs claimDirs =
				dirs.dirs().splitWhenUnknown(
						dirs.dirs().falseDir(),
						unknownClaim.head())
				.value(dirs);
		final ValOp claim = getValueIR().writeClaim(claimDirs, host, null);

		if (!code.exists()) {
			claimDirs.done();
			if (!unknownClaim.exists()) {
				return claim;
			}

			unknownClaim.go(code.tail());
			dirs.setStoreMode(INITIAL_VAL_STORE);

			return getValueIR().writeProposition(dirs, host, null);
		}
		if (!unknownClaim.exists()) {
			claimDirs.done();
			return claim;
		}

		final ValType.Op result1 = code.phi(null, claim.ptr());

		claimDirs.done();

		final ValType.Op result2 = writeProposition(dirs, unknownClaim, host);

		unknownClaim.go(code.tail());

		return code.phi(null, result1, result2).op(
				dirs.getBuilder(),
				getObject().value().getValueStruct());
	}

	private void writeRequirement(CodeDirs dirs, ObjOp host) {

		final Code unknownReq = dirs.addBlock("unknown_req");
		final CodeDirs reqDirs = dirs.splitWhenUnknown(
				dirs.falseDir(),
				unknownReq.head());

		getValueIR().writeRequirement(reqDirs, host, null);
		if (unknownReq.exists()) {
			unknownReq.go(dirs.code().tail());
		}
	}

	private ValType.Op writeProposition(
			ValDirs dirs,
			Code code,
			ObjOp host) {

		final ValDirs propDirs = dirs.sub(code).setStoreMode(INITIAL_VAL_STORE);
		final ValType.Op prop = code.phi(
				null,
				getValueIR().writeProposition(propDirs, host, null).ptr());

		propDirs.done();

		return prop;
	}

}
