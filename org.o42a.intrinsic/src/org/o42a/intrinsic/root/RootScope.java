/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectScope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;


final class RootScope extends ObjectScope {

	RootScope(LocationInfo location, Distributor enclosing) {
		super(location, enclosing);
	}

	@Override
	public boolean contains(Scope other) {
		return true;
	}

	@Override
	protected ScopeIR createIR(Generator generator) {
		return new IR(generator, this);
	}

	private final class IR extends ScopeIR {

		private final CodeId id;

		IR(Generator generator, Scope scope) {
			super(generator, scope);
			this.id = generator.id("ROOT");
		}

		@Override
		public CodeId getId() {
			return this.id;
		}

		@Override
		public void allocate() {

			final Obj object = getScope().getContainer().toObject();

			object.ir(getGenerator()).allocate();
		}

		@Override
		protected void targetAllocated() {
		}

		@Override
		protected HostOp createOp(CodeBuilder builder, Code code) {

			final Obj object = getScope().getContainer().toObject();

			return object.ir(getGenerator()).op(builder, code);
		}

	}

}
