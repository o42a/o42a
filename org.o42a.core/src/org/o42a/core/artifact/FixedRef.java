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

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;


final class FixedRef extends Ref {

	private final Resolution self;

	FixedRef(Distributor distributor, Artifact<?> self) {
		super(self, distributor);
		this.self = artifactResolution(self);
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Resolution resolve(Resolver resolver) {
		assertCompatible(resolver.getScope());
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
	protected FieldDefinition createFieldDefinition() {
		return defaultFieldDefinition();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolve(resolver).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		value(resolver);
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
		public HostOp target(CodeDirs dirs) {

			final ScopeIR ir =
				this.ref.getResolution().getScope().ir(getGenerator());

			return ir.op(getBuilder(), dirs.code());
		}

	}

}
