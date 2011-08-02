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
package org.o42a.core.ref.impl.type;

import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.util.log.Loggable;


public final class DefaultTypeRef extends TypeRef {

	private final Ref ref;

	public DefaultTypeRef(Ref ref, Rescoper rescoper) {
		super(rescoper);
		this.ref = ref;
	}

	@Override
	public final CompilerContext getContext() {
		return this.ref.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.ref.getLoggable();
	}

	@Override
	public boolean isStatic() {
		return getRef().isStatic();
	}

	@Override
	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public final Ref getUntachedRef() {
		return this.ref;
	}

	@Override
	public StaticTypeRef toStatic() {
		return new DefaultStaticTypeRef(
				getRef(),
				getUntachedRef(),
				getRescoper());
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	@Override
	protected DefaultTypeRef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new DefaultTypeRef(getRef(), rescoper);
	}

	@Override
	protected DefaultTypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper) {
		assert ref == untouchedRef :
			ref + " should be the same as " + untouchedRef;
		return new DefaultTypeRef(ref, rescoper);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		validate();
		this.ref.resolveAll(resolver);
	}

}
