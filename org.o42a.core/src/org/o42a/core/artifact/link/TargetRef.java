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
package org.o42a.core.artifact.link;

import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.core.def.RescopableRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.log.Loggable;


public final class TargetRef extends RescopableRef<TargetRef> {

	public static TargetRef targetRef(Ref ref, TypeRef typeRef) {
		if (typeRef != null) {
			return new TargetRef(
					ref,
					typeRef,
					transparentRescoper(ref.getScope()));
		}
		return new TargetRef(
				ref,
				ref.ancestor(ref),
				transparentRescoper(ref.getScope()));
	}

	public static TargetRef targetRef(
			Ref ref,
			TypeRef typeRef,
			Rescoper rescoper) {
		if (typeRef != null) {
			return new TargetRef(ref, typeRef, rescoper);
		}
		return new TargetRef(
				ref,
				ref.ancestor(ref).rescope(rescoper),
				rescoper);
	}

	private final Ref ref;
	private final TypeRef typeRef;
	private Logical fullLogical;

	private TargetRef(Ref ref, TypeRef typeRef, Rescoper rescoper) {
		super(rescoper);
		this.ref = ref;
		this.typeRef = typeRef;
		typeRef.assertSameScope(this);
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
	public final Ref getRef() {
		return this.ref;
	}

	public TypeRef getTypeRef() {
		return this.typeRef;
	}

	public Logical fullLogical() {
		if (this.fullLogical != null) {
			return this.fullLogical;
		}
		return this.fullLogical = new FullLogical(this);
	}

	public final TypeRef toTypeRef() {
		return getRef().toTypeRef().rescope(getRescoper());
	}

	public final TargetRef toStatic() {
		return new TargetRef(
				this.ref.toStatic(),
				this.typeRef.toStatic(),
				getRescoper());
	}

	public RefOp ref(CodeDirs dirs, ObjOp host) {

		final HostOp rescopedHost = getRescoper().rescope(dirs, host);

		return getRef().op(rescopedHost);
	}

	public HostOp target(CodeDirs dirs, ObjOp host) {
		return ref(dirs, host).target(dirs);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		return "(" + this.typeRef + ") " + this.ref;
	}

	@Override
	protected TargetRef create(Rescoper rescoper, Rescoper additionalRescoper) {
		return new TargetRef(
				getRef(),
				getTypeRef().rescope(additionalRescoper),
				rescoper);
	}

	@Override
	protected TargetRef createUpscoped(Ref ref, Rescoper upscopedRescoper) {

		final TypeRef upscopedTypeRef =
				getTypeRef().upscope(upscopedRescoper.getFinalScope());

		if (upscopedTypeRef == null) {
			return null;
		}

		return new TargetRef(ref, upscopedTypeRef, upscopedRescoper);
	}

	@Override
	protected TargetRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Rescoper rescoper) {

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return new TargetRef(ref, typeRef, rescoper);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.typeRef.resolveAll(resolver);
		this.ref.resolveAll(resolver);
	}

	private static final class FullLogical extends Logical {

		private final TargetRef targetRef;

		FullLogical(TargetRef targetRef) {
			super(targetRef, targetRef.getScope());
			this.targetRef = targetRef;
		}

		@Override
		public LogicalValue getConstantValue() {
			return this.targetRef.getRef().getLogical().getConstantValue();
		}

		@Override
		public LogicalValue logicalValue(Resolver resolver) {
			assertCompatible(resolver.getScope());
			return this.targetRef.getRef().getLogical().logicalValue(
					this.targetRef.getRescoper().rescope(this, resolver));
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {

			final TargetRef targetRef = this.targetRef.reproduce(reproducer);

			if (targetRef == null) {
				return null;
			}

			return targetRef.fullLogical();
		}

		@Override
		public void write(CodeDirs dirs, HostOp host) {
			assert assertFullyResolved();
			this.targetRef.getRef().getLogical().write(
					dirs,
					this.targetRef.getRescoper().rescope(dirs, host));
		}

		@Override
		public String toString() {
			return "(" + this.targetRef + ")?";
		}

		@Override
		protected void fullyResolve(Resolver resolver) {
			this.targetRef.resolveAll(resolver);
			this.targetRef.getRef().resolveValues(
					this.targetRef.getRescoper().rescope(this, resolver));
		}

	}

}
