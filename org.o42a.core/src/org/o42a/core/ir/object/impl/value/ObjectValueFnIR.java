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
import org.o42a.core.st.DefValue;


public final class ObjectValueFnIR extends ObjectValFnIR {

	public ObjectValueFnIR(ObjectValueIR valueIR) {
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
	protected DefValue determineConstant() {

		final DefValue claim = getValueIR().claim().getConstant();

		if (claim.hasValue() || !claim.getCondition().isTrue()) {
			return claim;
		}

		return getValueIR().proposition().getConstant();
	}

	@Override
	protected DefValue determineFinal() {

		final DefValue claim = getValueIR().claim().getFinal();

		if (claim.hasValue() || !claim.getCondition().isTrue()) {
			return claim;
		}

		return getValueIR().proposition().getFinal();
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
		final ObjectClaimFnIR claim = valueIR.claim();
		final DefValue finalClaim = claim.getFinal();

		if (isConstantValue(finalClaim)) {
			if (finalClaim.getCondition().isFalse()) {
				reuse(falseValFunc());
				return;
			}
			reused = valueIR.proposition().getNotStub();
			if (reused != null) {
				reuse(reused);
			}
			return;
		}
		if (!isConstantValue(valueIR.proposition().getFinal())) {
			return;
		}
		reused = claim.getNotStub();
		if (reused != null) {
			reuse(reused);
		}
	}

	@Override
	protected void build(DefDirs dirs, ObjOp host) {
		getValueIR().writeClaim(dirs, host, null);
		getValueIR().writeProposition(dirs, host, null);
	}

}
