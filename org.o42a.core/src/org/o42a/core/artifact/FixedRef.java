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
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;


final class FixedRef extends Ref {

	private final Resolution self;

	FixedRef(Distributor distributor, Artifact<?> self) {
		super(self, distributor);
		this.self = artifactResolution(self);
	}

	@Override
	public Resolution resolve(Scope scope) {
		assertCompatible(scope);
		return this.self;
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new FixedRef(reproducer.distribute(), this.self.toArtifact());
	}

	@Override
	public String toString() {
		return "&(" + this.self.toString() + " / " + getScope() + ')';
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

		private final FixedRef ref;

		public Op(HostOp host, FixedRef ref) {
			super(host, ref);
			this.ref = ref;
		}

		@Override
		public HostOp target(Code code, CodePos exit) {
			return this.ref.getResolution().getScope().ir(getGenerator())
			.op(getBuilder(), code);
		}

	}

}
