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
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ref.Resolver;


public final class ObjectValueFunc extends ObjectValueIRValFunc {

	public ObjectValueFunc(ObjectValueIR valueIR) {
		super(valueIR);
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

		final Code code = dirs.code();

		getValueIR().writeRequirement(dirs.dirs(), host, null);
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

}
