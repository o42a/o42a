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
package org.o42a.core.ir.local;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.member.local.Dep;


public class LocalBuilder extends CodeBuilder {

	private final ObjectOp owner;

	public LocalBuilder(
			Function<? extends ObjectFunc<?>> function,
			LocalIR scopeIR) {
		super(function, scopeIR);

		final Obj owner = scopeIR.getScope().getOwner();
		final DataOp ownerPtr =
				getFunction().arg(function, getObjectSignature().object());

		this.owner = anonymousObject(this, ownerPtr, owner);
	}

	@Override
	public final LocalOp host() {
		return (LocalOp) super.host();
	}

	public final ObjectOp owner() {
		return this.owner;
	}

	@Override
	public ObjectOp newObject(
			CodeDirs dirs,
			ObjectOp scope,
			ObjectOp ancestor,
			Obj sample) {

		final ObjectOp newObject =
				super.newObject(dirs, scope, ancestor, sample);

		for (Dep dep : sample.getDeps()) {
			newObject.dep(dirs, dep).fill(this, dirs);
		}

		return newObject;
	}

	public final Control createControl(Code code, CodePos exit) {
		return new MainControl(this, code, exit);
	}

}
