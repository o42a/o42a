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
import org.o42a.core.def.impl.rescoper.RescoperWrap;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.util.log.Loggable;


public abstract class TypeRefWrap extends TypeRef {

	private TypeRef wrapped;

	public TypeRefWrap(Scope finalScope) {
		super(new WrapRescoper(finalScope));

		final WrapRescoper rescoper = (WrapRescoper) getRescoper();

		rescoper.wrap = this;
	}

	protected TypeRefWrap(Rescoper rescoper) {
		super(rescoper);
	}

	@Override
	public final boolean isStatic() {
		return wrapped().isStatic();
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
	public final TypeRef rescope(Rescoper rescoper) {

		final TypeRef wrapped = getWrapped();

		if (wrapped != null) {
			return wrapped.rescope(rescoper);
		}

		return super.rescope(rescoper);
	}

	@Override
	public TypeRef upscope(Scope toScope) {

		final TypeRef wrapped = getWrapped();

		if (wrapped != null) {
			return wrapped.upscope(toScope);
		}

		return upscopeWrap(toScope);
	}

	@Override
	public final TypeRef reproduce(Reproducer reproducer) {
		return wrapped().reproduce(reproducer);
	}

	@Override
	public StaticTypeRef toStatic() {

		final TypeRef wrapped = getWrapped();

		if (wrapped != null) {
			return wrapped.toStatic();
		}

		return new Static(this, getRescoper());
	}

	@Override
	public String toString() {

		final TypeRef wrapped = getWrapped();

		if (wrapped == null) {
			return super.toString();
		}

		return wrapped.toString();
	}

	protected final TypeRef wrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}
		return this.wrapped = resolveWrapped();
	}

	protected abstract TypeRef resolveWrapped();

	@Override
	protected final TypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.wrapped.resolveAll(resolver);
	}

	final TypeRefWrap upscopeWrap(Scope toScope) {
		return (TypeRefWrap) super.upscope(toScope);
	}

	private static final class WrapRescoper extends RescoperWrap {

		private TypeRefWrap wrap;

		WrapRescoper(Scope finalScope) {
			super(finalScope);
		}

		@Override
		protected TypeRef wrappedTypeRef() {
			return this.wrap.wrapped();
		}

	}

	private static final class Static extends StaticTypeRefWrap {

		private final TypeRefWrap wrap;

		Static(TypeRefWrap wrap, Rescoper rescoper) {
			super(wrap.getRescoper());
			this.wrap = wrap;
		}

		@Override
		public CompilerContext getContext() {
			return this.wrap.getContext();
		}

		@Override
		public Loggable getLoggable() {
			return this.wrap.getLoggable();
		}

		@Override
		public String toString() {
			if (this.wrap == null) {
				return super.toString();
			}
			return "&" + this.wrap;
		}

		@Override
		protected StaticTypeRef resolveWrapped() {
			return this.wrap.wrapped().toStatic();
		}

		@Override
		protected StaticTypeRef create(
				Rescoper rescoper,
				Rescoper additionalRescoper) {
			return new Static(this.wrap, rescoper);
		}

		@Override
		protected StaticTypeRefWrap createUpscoped(
				Ref ref,
				Rescoper upscopedRescoper) {

			final TypeRefWrap upscopedWrap =
					this.wrap.upscopeWrap(upscopedRescoper.getFinalScope());

			return new Static(upscopedWrap, upscopedRescoper);
		}

	}

}
