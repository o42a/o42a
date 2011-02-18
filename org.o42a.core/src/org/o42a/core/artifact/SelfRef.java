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
package org.o42a.core.artifact;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.SelfRefBase;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


final class SelfRef extends SelfRefBase {

	private final Resolution self;

	SelfRef(Artifact<?> self) {
		super(self, self.distribute());
		this.self = artifactResolution(self);
	}

	@Override
	public Resolution resolve(Scope scope) {
		assertCompatible(scope);
		return this.self;
	}

	@Override
	public Value<?> value(Scope scope) {
		if (scope == this.self.getScope()) {
			return this.self.materialize().getValue();
		}
		return calculateValue(this.self.materialize(), scope);
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		getLogger().notReproducible(this);
		return null;
	}

	@Override
	public String toString() {
		return "&" + this.self.toString();
	}

	@Override
	protected boolean isKnownStatic() {
		return true;
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private static final class Op extends RefOp {

		private final SelfRef ref;

		public Op(HostOp host, SelfRef ref) {
			super(host, ref);
			this.ref = ref;
		}

		@Override
		public void writeLogicalValue(Code code, CodePos exit) {

			final HostOp target = target(code, exit);

			target.materialize(code, exit).writeLogicalValue(
					code,
					exit,
					host().toObject(code, exit));
		}

		@Override
		public void writeValue(Code code, CodePos exit, ValOp result) {

			final HostOp target = target(code, exit);

			target.materialize(code, exit).writeValue(
					code,
					exit,
					result,
					host().toObject(code, exit));
		}

		@Override
		public HostOp target(Code code, CodePos exit) {
			return this.ref.getResolution().getScope().ir(getGenerator())
			.op(getBuilder(), code);
		}

	}

}
