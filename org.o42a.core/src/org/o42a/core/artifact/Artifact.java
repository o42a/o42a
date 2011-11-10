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

import org.o42a.core.*;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.Holder;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.UseInfo;


public abstract class Artifact<A extends Artifact<A>> extends Placed {

	private ArtifactContent content;
	private ArtifactContent clonesContent;
	private Ref self;
	private boolean allResolved;
	private final A propagatedFrom;
	private Holder<A> cloneOf;

	public Artifact(Scope scope) {
		super(scope, new ArtifactDistributor(scope, scope));
		this.propagatedFrom = null;
	}

	protected Artifact(ArtifactScope<A> scope) {
		super(scope, new ArtifactDistributor(scope, scope));
		this.propagatedFrom = null;
		scope.setScopeArtifact(toArtifact());
	}

	protected Artifact(Scope scope, A sample) {
		super(scope, new ArtifactDistributor(scope, sample));
		this.propagatedFrom = sample;
	}

	protected Artifact(ArtifactScope<A> scope, A sample) {
		super(scope, new ArtifactDistributor(scope, sample));
		this.propagatedFrom = sample;
		scope.setScopeArtifact(toArtifact());
	}

	public abstract ArtifactKind<A> getKind();

	@SuppressWarnings("unchecked")
	public final A toArtifact() {
		return (A) this;
	}

	@Override
	public MemberContainer getContainer() {
		return getScope().getContainer();
	}

	public TypeRef getTypeRef() {
		return null;
	}

	public A getPropagatedFrom() {
		return this.propagatedFrom;
	}

	public final boolean isClone() {
		return getCloneOf() != null;
	}

	public boolean isPropagated() {
		return getPropagatedFrom() != null;
	}

	public final A getCloneOf() {
		if (this.cloneOf != null) {
			return this.cloneOf.get();
		}

		final A cloneOf = findCloneOf();

		this.cloneOf = new Holder<A>(cloneOf);

		return cloneOf;
	}

	public abstract Obj toObject();

	public abstract Link toLink();

	public abstract Obj materialize();

	public final Ref selfRef() {
		if (this.self != null) {
			return this.self;
		}
		return this.self =
				Path.SELF_PATH
				.bindStatically(this, getScope())
				.target(distribute());
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

	public final ArtifactContent content() {
		if (this.content != null) {
			return this.content;
		}

		final Artifact<?> cloneOf = getCloneOf();

		if (cloneOf != null) {
			return this.content = cloneOf.clonesContent();
		}

		return this.content = new ArtifactContent(this, false);
	}

	public final ArtifactContent clonesContent() {
		if (this.clonesContent != null) {
			return this.clonesContent;
		}

		final Artifact<?> cloneOf = getCloneOf();

		if (cloneOf != null) {
			return this.clonesContent = cloneOf.clonesContent();
		}

		return this.clonesContent = new ArtifactContent(this, true);
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
		assert this.allResolved || (isClone() && getCloneOf().allResolved):
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
			loggable.print(out);
		}
		out.append(']');

		return out.toString();
	}

	protected A findCloneOf() {
		if (!getScope().isClone()) {
			return null;
		}
		return getKind().cast(getScope().getLastDefinition().getArtifact());
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
