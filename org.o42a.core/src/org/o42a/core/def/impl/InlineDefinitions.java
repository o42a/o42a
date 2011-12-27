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
package org.o42a.core.def.impl;

import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;

import org.o42a.codegen.code.Code;
import org.o42a.core.def.InlineCond;
import org.o42a.core.def.InlineValue;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;


public class InlineDefinitions extends InlineValue {

	private final InlineCond requirement;
	private final InlineCond condition;
	private final InlineValue claim;
	private final InlineValue proposition;

	public InlineDefinitions(
			InlineCond requirement,
			InlineCond condition,
			InlineValue claim,
			InlineValue proposition) {
		super(proposition.getValueStruct());
		this.requirement = requirement;
		this.condition = condition;
		this.claim = claim;
		this.proposition = proposition;
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {

		final Code code = dirs.code();

		writeRequirement(dirs.dirs(), host);
		this.condition.writeCond(dirs.dirs(), host);

		final Code unknownClaim = dirs.addBlock("unknown_claim");
		final ValDirs claimDirs =
				dirs.dirs().splitWhenUnknown(
						dirs.dirs().falseDir(),
						unknownClaim.head())
				.value(dirs);
		final ValOp claim = this.claim.writeValue(claimDirs, host);

		if (!code.exists()) {
			claimDirs.done();
			if (!unknownClaim.exists()) {
				return claim;
			}

			unknownClaim.go(code.tail());
			dirs.setStoreMode(INITIAL_VAL_STORE);

			return this.proposition.writeValue(dirs, host);
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
				getValueStruct());
	}

	private void writeRequirement(CodeDirs dirs, HostOp host) {

		final Code unknownReq = dirs.addBlock("unknown_req");
		final CodeDirs reqDirs = dirs.splitWhenUnknown(
				dirs.falseDir(),
				unknownReq.head());

		this.requirement.writeCond(reqDirs, host);
		if (unknownReq.exists()) {
			unknownReq.go(dirs.code().tail());
		}
	}

	private ValType.Op writeProposition(
			ValDirs dirs,
			Code code,
			HostOp host) {

		final ValDirs propDirs = dirs.sub(code);
		final ValType.Op prop = code.phi(
				null,
				this.proposition.writeValue(propDirs, host).ptr());

		propDirs.done();

		return prop;
	}

}
