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
package org.o42a.core.ref.type;

import org.o42a.core.CompilerContext;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.util.log.Loggable;


final class DefaultStaticTypeRef extends StaticTypeRef {

	private final Ref fixedRef;
	private final Ref untouchedRef;

	DefaultStaticTypeRef(Ref ref, Ref untouchedRef, Rescoper rescoper) {
		super(rescoper);
		this.fixedRef = ref.toStatic();
		this.untouchedRef = untouchedRef;
		ref.assertSameScope(untouchedRef);
	}

	@Override
	public final CompilerContext getContext() {
		return this.untouchedRef.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.untouchedRef.getLoggable();
	}

	@Override
	public final Ref getRef() {
		return this.fixedRef;
	}

	@Override
	public final Ref getUntachedRef() {
		return this.untouchedRef;
	}

	@Override
	public String toString() {
		if (this.fixedRef == null) {
			return super.toString();
		}
		return this.fixedRef.toString();
	}

	@Override
	protected DefaultStaticTypeRef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new DefaultStaticTypeRef(getRef(), getUntachedRef(), rescoper);
	}

	@Override
	protected StaticTypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper) {
		return new DefaultStaticTypeRef(ref, untouchedRef, rescoper);
	}

	@Override
	protected void fullyResolve() {
		validate();
		this.untouchedRef.resolveAll();
		getRescoper().resolveAll();
	}

}
