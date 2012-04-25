/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.object.value.ValueUsage.ALL_VALUE_USAGES;

import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.object.value.ObjectValuePart;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;


public final class ObjectValueFunc extends ObjectValueIRFunc {

	public ObjectValueFunc(ObjectValueIR valueIR) {
		super(valueIR);
	}

	@Override
	public final ObjectValuePart part() {
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

		final Value<?> claim = getValueIR().claim().getConstant();

		if (!claim.getKnowledge().hasUnknownCondition()) {
			return claim;
		}

		return getValueIR().proposition().getConstant();
	}

	@Override
	protected Value<?> determineFinal() {
		if (!getValueIR().claim().getFinal().getKnowledge().isKnown()
				|| !getValueIR().proposition().getFinal()
				.getKnowledge().isKnown()) {
			return getValueStruct().runtimeValue();
		}

		return getObject().value().getDefinitions().value(
				getObject().getScope().dummyResolver());
	}

	@Override
	protected boolean canStub() {
		return getValueIR().claim().canStub()
				&& getValueIR().proposition().canStub()
				&& !getObject().value().isUsed(
						getGenerator().getAnalyzer(),
						ALL_VALUE_USAGES);
	}

	@Override
	protected void reuse() {

		final ObjectValueIR valueIR = getValueIR();
		final FuncPtr<ObjectValFunc> reused;
		final ObjectClaimFunc claim = valueIR.claim();
		final Value<?> finalClaim = claim.getFinal();

		if (finalClaim.getKnowledge().isKnown()) {
			if (finalClaim.getKnowledge().getCondition() == Condition.FALSE) {
				reused = falseValFunc();
			} else {
				reused = valueIR.proposition().getNotStub();
				if (reused == null) {
					return;
				}
			}
		} else if (!valueIR.proposition().getFinal().getKnowledge().isKnown()) {
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
	protected void build(DefDirs dirs, ObjOp host) {
		getValueIR().writeClaim(dirs, host, null);
		getValueIR().writeProposition(dirs, host, null);
	}

}
