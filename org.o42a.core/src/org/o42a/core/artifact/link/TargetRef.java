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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.CompilerContext;
import org.o42a.core.Scope;
import org.o42a.core.def.RescopableRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.log.Loggable;


public final class TargetRef extends RescopableRef {

	private final Ref ref;
	private final TypeRef typeRef;
	private Logical fullLogical;

	TargetRef(Ref ref, TypeRef typeRef, Rescoper rescoper) {
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
				this.ref.fixScope(),
				this.typeRef.toStatic(),
				getRescoper());
	}

	@Override
	public TargetRef rescope(Rescoper rescoper) {
		return (TargetRef) super.rescope(rescoper);
	}

	@Override
	public final TargetRef upgradeScope(Scope scope) {
		return (TargetRef) super.upgradeScope(scope);
	}

	@Override
	public TargetRef rescope(Scope scope) {
		return (TargetRef) super.rescope(scope);
	}

	public RefOp ref(Code code, CodePos exit, ObjOp host) {

		final HostOp rescopedHost = getRescoper().rescope(code, exit, host);

		return getRef().op(rescopedHost);
	}

	@Override
	public TargetRef reproduce(Reproducer reproducer) {
		return (TargetRef) super.reproduce(reproducer);
	}

	public HostOp target(Code code, CodePos exit, ObjOp host) {
		return ref(code, exit, host).target(code, exit);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		return "(" + this.typeRef + ") " + this.ref;
	}

	@Override
	protected final Ref getScoped() {
		return this.ref;
	}

	@Override
	protected TargetRef create(Rescoper rescoper, Rescoper additionalRescoper) {
		return new TargetRef(
				getRef(),
				getTypeRef().rescope(additionalRescoper),
				rescoper);
	}

	@Override
	protected RescopableRef createReproduction(
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
		public LogicalValue logicalValue(Scope scope) {
			return this.targetRef.getRef().getLogical().logicalValue(
					this.targetRef.getRescoper().rescope(scope));
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
		public void write(Code code, CodePos exit, HostOp host) {
			this.targetRef.getRef().getLogical().write(
					code,
					exit,
					this.targetRef.getRescoper().rescope(code, exit, host));
		}

		@Override
		public String toString() {
			return "(" + this.targetRef + ")?";
		}

	}

}
