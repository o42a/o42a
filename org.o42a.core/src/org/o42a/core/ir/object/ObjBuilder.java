/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import static org.o42a.core.ir.op.HostOp.HOST_ID;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.op.ObjectFn;


public final class ObjBuilder extends CodeBuilder {

	private final ObjOp host;

	public ObjBuilder(
			Function<? extends ObjectFn<?>> function,
			CodePos exit,
			ObjectIR hostIR,
			ObjectPrecision hostPrecision) {
		super(hostIR.getSampleDeclaration().getContext(), function);
		this.host = host(function, exit, hostIR, hostPrecision);
	}

	@Override
	public final ObjOp host() {
		return this.host;
	}

	private ObjOp host(
			Block code,
			CodePos exit,
			ObjectIR hostIR,
			ObjectPrecision hostPrecision) {
		switch (hostPrecision) {
		case EXACT:
			return hostIR.op(this, code);
		case COMPATIBLE:
			return getObjectSignature().object(code, getFunction())
					.to(null, code, hostIR.getType())
					.op(this, hostIR.getObject(), hostPrecision);
		case DERIVED:

			final ObjectOp host = anonymousObject(
					this,
					code,
					getObjectSignature().object(code, getFunction()),
					hostIR.getObject());

			return host.cast(HOST_ID, dirs(code, exit), hostIR.getObject());
		}

		throw new IllegalArgumentException(
				"Unknown host precision: " + hostPrecision);
	}

}
