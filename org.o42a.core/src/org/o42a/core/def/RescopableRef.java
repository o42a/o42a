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
package org.o42a.core.def;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.value.Value;


public abstract class RescopableRef<R extends RescopableRef<R>>
		extends RescopableStatement<R> {

	private Resolution resolution;
	private Ref rescopedRef;

	public RescopableRef(Rescoper rescoper) {
		super(rescoper);
	}

	@Override
	public final Statement getStatement() {
		return getRef();
	}

	public abstract Ref getRef();

	public final Ref getRescopedRef() {
		if (this.rescopedRef != null) {
			return this.rescopedRef;
		}
		return this.rescopedRef = getRef().rescope(getRescoper());
	}

	public final Artifact<?> getArtifact() {

		final Resolution resolution = getResolution();

		return resolution.isError() ? null : resolution.toArtifact();
	}

	public final Resolution getResolution() {
		if (this.resolution != null) {
			return this.resolution;
		}
		return this.resolution = resolve(getScope());
	}

	public final Resolution resolve(Scope scope) {
		return getRef().resolve(getRescoper().rescope(scope));
	}

	public final Value<?> getValue() {
		return value(getScope());
	}

	public final Value<?> value(Scope scope) {
		return getRef().value(getRescoper().rescope(scope));
	}

	public RefOp op(Code code, CodePos exit, HostOp host) {

		final HostOp rescoped = getRescoper().rescope(code, exit, host);

		return getRef().op(rescoped);
	}

	@Override
	protected final R createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Statement statement,
			Rescoper rescoper) {
		return createReproduction(
				reproducer,
				rescopedReproducer,
				(Ref) statement,
				rescoper);
	}

	protected abstract R createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Rescoper rescoper);

}
