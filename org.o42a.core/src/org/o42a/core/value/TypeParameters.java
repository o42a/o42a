/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.value;

import org.o42a.core.*;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.log.Loggable;


public final class TypeParameters implements ValueStructFinder, PlaceInfo {

	public static Mutability typeMutability(
			LocationInfo location,
			Distributor distributor,
			LinkValueType linkType) {
		return new Mutability(location, distributor, linkType);
	}

	private final TypeRef typeRef;
	private final Mutability mutability;

	private TypeParameters(TypeRef typeRef, Mutability mutability) {
		typeRef.assertSameScope(mutability);
		this.typeRef = typeRef;
		this.mutability = mutability;
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	public final TypeParameters setTypeRef(TypeRef typeRef) {
		return typeMutability(
				typeRef,
				typeRef.getRef().distribute(),
				getLinkType())
				.setTypeRef(typeRef);
	}

	public final LinkValueType getLinkType() {
		return getMutability().getLinkType();
	}

	public final Mutability getMutability() {
		return this.mutability;
	}

	@Override
	public final CompilerContext getContext() {
		return getTypeRef().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return getTypeRef().getLoggable();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Scope getScope() {
		return getMutability().getScope();
	}

	@Override
	public final ScopePlace getPlace() {
		return getMutability().getPlace();
	}

	@Override
	public final Container getContainer() {
		return getMutability().getContainer();
	}

	@Override
	public final ValueStruct<?, ?> valueStructBy(
			ValueStruct<?, ?> defaultStruct) {
		return defaultStruct.setParameters(this);
	}

	@Override
	public final TypeParameters prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new TypeParameters(
				newTypeRef,
				getMutability().toScope(prefix.getStart()));
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	@Override
	public ValueStructFinder reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return new TypeParameters(
				typeRef,
				getMutability().toScope(reproducer.getScope()));
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		if (this.mutability == null) {
			return super.toString();
		}
		return "(" + this.mutability + this.typeRef + ")";
	}

	public static final class Mutability extends Placed {

		private final LinkValueType linkType;

		private Mutability(
				LocationInfo location,
				Distributor distributor,
				LinkValueType linkType) {
			super(location, distributor);
			this.linkType = linkType;
		}

		private Mutability(Scope scope, Mutability prototype) {
			super(prototype, prototype.distributeIn(scope.getContainer()));
			this.linkType = prototype.linkType;
		}

		public final LinkValueType getLinkType() {
			return this.linkType;
		}

		public final TypeParameters setTypeRef(TypeRef typeRef) {
			assert typeRef != null :
				"Type reference not specified";
			return new TypeParameters(typeRef, this);
		}

		@Override
		public String toString() {
			if (this.linkType == LinkValueType.LINK) {
				return "`";
			}
			if (this.linkType == LinkValueType.VARIABLE) {
				return "``";
			}
			if (this.linkType == LinkValueType.GETTER) {
				return "```";
			}
			return super.toString();
		}

		private final Mutability toScope(Scope scope) {
			return new Mutability(scope, this);
		}

	}

}
