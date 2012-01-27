/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.ObjectFunc;


public final class ObjBuilder extends CodeBuilder {

	private final ObjOp host;

	public ObjBuilder(
			Function<? extends ObjectFunc<?>> function,
			CodePos exit,
			ObjectBodyIR hostIR,
			Obj hostType,
			ObjectPrecision hostPrecision) {
		super(hostIR.getAscendant().getContext(), function);
		this.host = host(function, exit, hostIR, hostType, hostPrecision);
	}

	@Override
	public final ObjOp host() {
		return this.host;
	}

	@Override
	public final ObjOp owner() {
		return host();
	}

	private ObjOp host(
			Code code,
			CodePos exit,
			ObjectBodyIR hostIR,
			Obj hostType,
			ObjectPrecision hostPrecision) {
		switch (hostPrecision) {
		case EXACT:
			return hostIR.getObjectIR().op(this, code).cast(
					null,
					falseWhenUnknown(code, exit),
					hostType);
		case COMPATIBLE:
			return getFunction().arg(code, getObjectSignature().object())
					.to(null, code, hostIR)
					.op(this, hostType, hostPrecision);
		case DERIVED:

			final ObjectOp host = anonymousObject(
					this,
					getFunction().arg(code, getObjectSignature().object()),
					hostType);

			return host.cast(
					code.id("host"),
					falseWhenUnknown(code, exit),
					hostType);
		}

		throw new IllegalArgumentException(
				"Unknown host precision: " + hostPrecision);
	}

}
