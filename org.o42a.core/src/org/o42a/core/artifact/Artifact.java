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
import static org.o42a.util.use.Usable.simpleUsable;

import org.o42a.core.*;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.Holder;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.Usable;
import org.o42a.util.use.UseInfo;


public abstract class Artifact<A extends Artifact<A>> extends Placed {

	private Usable<A> content;
	private Holder<Obj> enclosingPrototype;
	private ScopePlace localPlace;
	private Ref self;
	private boolean allResolved;

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

	public final boolean isClone() {
		return getCloneOf() != null;
	}

	public A getCloneOf() {

		@SuppressWarnings("unchecked")
		final Field<A> field = (Field<A>) getScope().toField();

		if (field == null || !field.isClone()) {
			return null;
		}

		return field.getLastDefinition().getArtifact();
	}

	public abstract Obj toObject();

	public abstract Array toArray();

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
			return this.enclosingPrototype.get();
		}

		final Obj enclosingObject =
			getScope().getEnclosingContainer().toObject();

		if (enclosingObject == null || enclosingObject.isPrototype()) {
			this.enclosingPrototype = new Holder<Obj>(enclosingObject);
			return enclosingObject;
		}

		final Obj enclosingPrototype = enclosingObject.getEnclosingPrototype();

		this.enclosingPrototype = new Holder<Obj>(enclosingPrototype);

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

	public final Access accessBy(ScopeInfo user) {
		return artifactAccess(user, this);
	}

	public final Usable<A> content() {
		if (this.content != null) {
			return this.content;
		}
		return this.content = simpleUsable("Content", toArtifact());
	}

	public abstract UseInfo fieldUses();

	public final void resolveAll() {
		if (this.allResolved) {
			return;
		}
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			fullyResolve();
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final boolean assertFullyResolved() {
		assert this.allResolved || isClone() :
			this + " is not fully resolved";
		return true;
	}

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

	protected abstract void fullyResolve();

	private static final class ArtifactDistributor extends Distributor {

		private final Scope scope;
		private final PlaceInfo placed;

		ArtifactDistributor(Scope scope, PlaceInfo placed) {
			this.scope = scope;
			this.placed = placed;
		}

		@Override
		public Loggable getLoggable() {
			return this.placed.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.placed.getContext();
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
