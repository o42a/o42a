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
package org.o42a.core.ref.common;

import org.o42a.core.*;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefWrap;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.util.log.Loggable;


public abstract class Wrap extends Ref {

	private Ref wrapped;

	public Wrap(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public Path getPath() {
		return wrapped().getPath();
	}

	public final Ref getWrapped() {
		return this.wrapped;
	}

	@Override
	public final TypeRef ancestor(LocationInfo location) {
		if (this.wrapped != null) {
			return this.wrapped.ancestor(location);
		}
		return new AncestorWrap(location);
	}

	@Override
	public final Resolution resolve(Scope scope) {
		return wrapped().resolve(scope);
	}

	@Override
	public final Value<?> value(Scope scope) {
		return wrapped().value(scope);
	}

	@Override
	public final Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return wrapped().reproduce(reproducer);
	}

	@Override
	public final Ref toStatic() {
		if (this.wrapped != null) {
			return this.wrapped.toStatic();
		}
		return super.toStatic();
	}

	@Override
	public final TypeRef toTypeRef() {
		if (this.wrapped != null) {
			return this.wrapped.toTypeRef();
		}
		return super.toTypeRef();
	}

	@Override
	public final StaticTypeRef toStaticTypeRef() {
		if (this.wrapped != null) {
			return this.wrapped.toStaticTypeRef();
		}
		return super.toStaticTypeRef();
	}

	@Override
	public final TargetRef toTargetRef(TypeRef typeRef) {
		if (this.wrapped != null) {
			return this.wrapped.toTargetRef(typeRef);
		}
		return super.toTargetRef(typeRef);
	}

	@Override
	public final Rescoper toRescoper() {
		return wrapped().toRescoper();
	}

	@Override
	public FieldDefinition toFieldDefinition() {
		if (this.wrapped != null) {
			return this.wrapped.toFieldDefinition();
		}
		return new DefinitionWrap();
	}

	@Override
	public String toString() {
		if (this.wrapped != null) {
			return this.wrapped.toString();
		}
		return super.toString();
	}

	protected abstract Ref resolveWrapped();

	protected final Ref errorRef(LocationInfo location) {
		return errorRef(location, distribute());
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return wrapped().op(host);
	}

	private Ref wrapped() {
		if (this.wrapped == null) {
			this.wrapped = resolveWrapped();
			if (this.wrapped == null) {
				this.wrapped = falseRef(this, distribute());
			} else {
				this.wrapped.assertCompatible(getScope());
			}
		}
		return this.wrapped;
	}

	private final class AncestorWrap extends TypeRefWrap {

		private final LocationInfo location;

		AncestorWrap(LocationInfo location) {
			super(Wrap.this.getScope());
			this.location = location;
		}

		AncestorWrap(LocationInfo location, Rescoper rescoper) {
			super(rescoper);
			this.location = location;
		}

		@Override
		public CompilerContext getContext() {
			return this.location.getContext();
		}

		@Override
		public Loggable getLoggable() {
			return this.location.getLoggable();
		}

		@Override
		public String toString() {
			return Wrap.this + "^^";
		}

		@Override
		protected TypeRef create(
				Rescoper rescoper,
				Rescoper additionalRescoper) {
			return new AncestorWrap(this.location, rescoper);
		}

		@Override
		protected TypeRef resolveWrapped() {
			return Wrap.this.wrapped().ancestor(this);
		}

	}

	private final class DefinitionWrap extends FieldDefinition {

		private FieldDefinition def;

		DefinitionWrap() {
			super(Wrap.this, Wrap.this.distribute());
		}

		@Override
		public boolean isValid() {
			return def().isValid();
		}

		@Override
		public ArtifactKind<?> determineArtifactKind() {
			return def().determineArtifactKind();
		}

		@Override
		public void defineObject(ObjectDefiner definer) {
			def().defineObject(definer);
		}

		@Override
		public void defineLink(LinkDefiner definer) {
			def().defineLink(definer);
		}

		@Override
		public void defineArray(ArrayDefiner definer) {
			def().defineArray(definer);
		}

		@Override
		public AscendantsDefinition getAscendants() {
			return def().getAscendants();
		}

		@Override
		public FieldDefinition reproduce(Reproducer reproducer) {
			return def().reproduce(reproducer);
		}

		@Override
		public String toString() {
			if (this.def != null) {
				return this.def.toString();
			}
			return "FieldDefinition[" + Wrap.this + ']';
		}

		private FieldDefinition def() {
			if (this.def != null) {
				return this.def;
			}
			return this.def = wrapped().toFieldDefinition();
		}

	}

}
