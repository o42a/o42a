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

import static org.o42a.core.artifact.Access.artifactAccess;

import org.o42a.core.*;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.util.log.Loggable;


public abstract class Artifact<A extends Artifact<A>> extends Placed {

	private static Unresolvable<?> unresolvable;
	private static UnresolvableObject unresolvableObject;

	@SuppressWarnings("rawtypes")
	public static Artifact<?> unresolvableArtifact(CompilerContext context) {
		if (unresolvable == null) {
			unresolvable = new Unresolvable(context);
		}
		return unresolvable;
	}

	public static Obj unresolvableObject(CompilerContext context) {
		if (unresolvableObject == null) {
			unresolvableObject = new UnresolvableObject(context);
		}
		return unresolvableObject;
	}

	private Obj enclosingPrototype;
	private ScopePlace localPlace;
	private Ref self;

	public Artifact(Scope scope) {
		super(scope, new ArtifactDistributor(scope, scope));
	}

	protected Artifact(Scope scope, A sample) {
		super(scope, new ArtifactDistributor(scope, sample));
	}

	public abstract ArtifactKind<A> getKind();

	@SuppressWarnings("unchecked")
	public final A toArtifact() {
		return (A) this;
	}

	@Override
	public Container getContainer() {
		return getScope().getContainer();
	}

	public TypeRef getTypeRef() {
		return null;
	}

	public abstract Obj toObject();

	public abstract Array toArray();

	public abstract Directive toDirective();

	public abstract Link toLink();

	public abstract Obj materialize();

	public final Ref selfRef() {
		if (this.self != null) {
			return this.self;
		}
		return this.self = new SelfRef(this);
	}

	public final Ref fixedRef(Distributor distributor) {
		return new FixedRef(distributor, this);
	}

	public boolean isAbstract() {

		final Field<?> field = getScope().toField();

		return field != null && field.isAbstract();
	}

	public boolean isPrototype() {

		final Field<?> field = getScope().toField();

		return field != null && field.isPrototype();
	}

	public boolean isValid() {
		return true;
	}

	public Obj getEnclosingPrototype() {
		if (this.enclosingPrototype != null) {

			if (this.enclosingPrototype != unresolvableObject(getContext())) {
				return this.enclosingPrototype;
			}

			return null;
		}

		final Obj enclosingObject =
			getScope().getEnclosingContainer().toObject();

		if (enclosingObject == null) {
			this.enclosingPrototype = unresolvableObject(getContext());
			return null;
		}
		if (enclosingObject.isPrototype()) {
			return this.enclosingPrototype = enclosingObject;
		}

		final Obj enclosingPrototype = enclosingObject.getEnclosingPrototype();

		if (enclosingPrototype != null) {
			this.enclosingPrototype = enclosingPrototype;
		} else {
			this.enclosingPrototype = unresolvableObject(getContext());
		}

		return enclosingPrototype;
	}

	public LocalPlace getLocalPlace() {
		if (this.localPlace != null) {
			if (this.localPlace != ScopePlace.TOP_PLACE) {
				return (LocalPlace) this.localPlace;
			}
			return null;
		}

		final LocalPlace place = getPlace().toLocal();

		if (place != null) {
			this.localPlace = place;
			return place;
		}

		final Container enclosing = getScope().getEnclosingContainer();

		assert enclosing.toLocal() == null :
			"Enclosing scope of " + this + " is expected to be local";

		final Artifact<?> enclosingArtifact = enclosing.toArtifact();

		if (enclosingArtifact == null) {
			this.localPlace = ScopePlace.TOP_PLACE;
			return null;
		}

		final LocalPlace localPlace = enclosingArtifact.getLocalPlace();

		if (localPlace != null) {
			this.localPlace = localPlace;
		} else {
			this.localPlace = ScopePlace.TOP_PLACE;
		}

		return localPlace;
	}

	public final Access accessBy(ScopeSpec user) {
		return artifactAccess(user, this);
	}

	public abstract void resolveAll();

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName()).append('[');
		out.append(getContext());

		final Loggable loggable = getLoggable();

		if (loggable != null) {
			out.append("]:[");
			loggable.printContent(out);
		}
		out.append(']');

		return out.toString();
	}

	private static final class Unresolvable<A extends Artifact<A>>
			extends Artifact<A> {

		Unresolvable(CompilerContext context) {
			super(context.getRoot().getScope());
		}

		@Override
		public ArtifactKind<A> getKind() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Scope getScope() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj toObject() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Array toArray() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Directive toDirective() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Link toLink() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj materialize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void resolveAll() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class UnresolvableObject extends Obj {

		UnresolvableObject(CompilerContext context) {
			super(context.getRoot().getScope());
		}

		@Override
		protected Ascendants buildAscendants() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void declareMembers(ObjectMembers members) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected Definitions overrideDefinitions(
				Scope scope,
				Definitions ascendantDefinitions) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class ArtifactDistributor extends Distributor {

		private final Scope scope;
		private final PlaceSpec placed;

		ArtifactDistributor(Scope scope, PlaceSpec placed) {
			this.scope = scope;
			this.placed = placed;
		}

		@Override
		public ScopePlace getPlace() {
			return this.placed.getPlace();
		}

		@Override
		public Container getContainer() {
			return null;
		}

		@Override
		public Scope getScope() {
			return this.scope;
		}

	}

}
