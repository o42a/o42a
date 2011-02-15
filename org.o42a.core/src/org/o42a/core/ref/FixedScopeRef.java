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
package org.o42a.core.ref;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


final class FixedScopeRef extends Ref {

	private final Ref ref;

	FixedScopeRef(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	@Override
	public Resolution resolve(Scope scope) {
		assertCompatible(scope);
		return this.ref.getResolution();
	}

	@Override
	public Value<?> value(Scope scope) {
		assertCompatible(scope);
		return this.ref.getValue();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Resolution resolution = this.ref.getResolution();

		if (resolution.isError()) {
			return null;
		}

		final Artifact<?> artifact = resolution.toArtifact();

		if (artifact == null) {
			getLogger().notReproducible(this);
			return null;
		}

		return artifact.self(reproducer.distribute());
	}

	@Override
	public String toString() {
		return "&" + this.ref;
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

		Op(HostOp host, FixedScopeRef ref) {
			super(host, ref);
		}

		@Override
		public HostOp target(Code code, CodePos exit) {
			return ref().ref.op(rescope(code, exit)).target(code, exit);
		}

		private HostOp rescope(Code code, CodePos exit) {

			final ScopeIR scopeIR = ref().ref.getScope().ir(getGenerator());

			return scopeIR.op(getBuilder(), code);
		}

		private final FixedScopeRef ref() {
			return (FixedScopeRef) getRef();
		}

	}

}
