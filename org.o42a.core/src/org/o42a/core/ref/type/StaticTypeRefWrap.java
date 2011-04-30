/*
    Compiler Core
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
package org.o42a.core.ref.type;

import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;


public abstract class StaticTypeRefWrap extends StaticTypeRef {

	private StaticTypeRef wrapped;

	public StaticTypeRefWrap(Scope finalScope) {
		super(new WrapRescoper(finalScope));

		final WrapRescoper rescoper = (WrapRescoper) getRescoper();

		rescoper.wrap = this;
	}

	protected StaticTypeRefWrap(Rescoper rescoper) {
		super(rescoper);
	}

	public final TypeRef getWrapped() {
		return this.wrapped;
	}

	@Override
	public final Ref getRef() {
		return wrapped().getRef();
	}

	@Override
	public final Ref getUntachedRef() {
		return wrapped().getUntachedRef();
	}

	@Override
	public final StaticTypeRef rescope(Rescoper rescoper) {
		if (this.wrapped != null) {
			return this.wrapped.rescope(rescoper);
		}
		return super.rescope(rescoper);
	}

	@Override
	public final StaticTypeRef reproduce(Reproducer reproducer) {
		return wrapped().reproduce(reproducer);
	}

	@Override
	public String toString() {
		if (this.wrapped == null) {
			return super.toString();
		}
		return this.wrapped.toString();
	}

	protected final StaticTypeRef wrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}
		return this.wrapped = resolveWrapped();
	}

	protected abstract StaticTypeRef resolveWrapped();

	@Override
	protected final StaticTypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolve() {
		this.wrapped.resolveAll();
	}

	private static final class WrapRescoper extends RescoperWrap {

		private StaticTypeRefWrap wrap;

		WrapRescoper(Scope finalScope) {
			super(finalScope);
		}

		@Override
		protected TypeRef wrappedTypeRef() {
			return this.wrap.wrapped();
		}

	}

}
