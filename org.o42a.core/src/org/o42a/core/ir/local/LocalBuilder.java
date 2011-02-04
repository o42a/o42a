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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.member.local.Dep;


public class LocalBuilder extends CodeBuilder {

	private final int ownerArg;
	private ObjectOp owner;

	public LocalBuilder(Function<?> function, LocalIR scopeIR, int ownerArg) {
		super(function, scopeIR);
		this.ownerArg = ownerArg;
	}

	@Override
	public final LocalOp host() {
		return (LocalOp) super.host();
	}

	public ObjectOp owner() {
		if (this.owner != null) {
			return this.owner;
		}

		final Obj owner = host().getScope().getOwner();
		final AnyOp ownerPtr =
			getFunction().ptrArg(getFunction(), this.ownerArg);

		return this.owner = anonymousObject(this, ownerPtr, owner);
	}

	@Override
	public ObjectOp newObject(
			Code code,
			CodePos exit,
			ObjectOp ancestor,
			Obj sample,
			int flags) {

		final ObjectOp newObject =
			super.newObject(code, exit, ancestor, sample, flags);

		for (Dep dep : sample.getDeps()) {
			newObject.dep(code, exit, dep).fill(this, code, exit);
		}

		return newObject;
	}

	public final Control createControl(Code code, CodePos exit) {
		return Control.createControl(this, code, exit);
	}
}
