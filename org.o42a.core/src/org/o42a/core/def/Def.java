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
package org.o42a.core.def;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.def.Rescoper.upgradeRescoper;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public abstract class Def<D extends Def<D>>
		implements SourceInfo, Rescopable<D> {

	public static final Obj sourceOf(ScopeInfo scope) {
		return sourceOf(scope.getScope().getContainer());
	}

	public static Obj sourceOf(Container container) {

		final Obj object = container.toObject();

		if (object != null) {
			return object;
		}

		final LocalScope local = container.toLocal();

		assert local != null :
			"Definition can be created only inside object or local scope";

		return local.getOwner();
	}

	private final Rescoper rescoper;
	private final Obj source;
	private final LocationInfo location;
	private DefKind kind;
	private boolean hasPrerequisite;
	private boolean allResolved;
	private Logical precondition;
	private Logical prerequisite;
	private Logical logical;

	Def(
			Obj source,
			LocationInfo location,
			DefKind kind,
			Rescoper rescoper) {
		this.rescoper = rescoper;
		this.location = location;
		this.source = source;
		this.kind = kind;
		this.hasPrerequisite = false;
	}

	Def(D prototype, Rescoper rescoper) {
		this.rescoper = rescoper;
		this.source = prototype.source;
		this.location = prototype.location;
		this.kind = prototype.kind;
		this.hasPrerequisite = prototype.hasPrerequisite;
		this.prerequisite = prototype.prerequisite;
		this.precondition = prototype.precondition;
		this.logical = prototype.logical;
	}

	@Override
	public final Scope getScope() {
		return this.rescoper.getFinalScope();
	}

	public final Rescoper getRescoper() {
		return this.rescoper;
	}

	@Override
	public final Loggable getLoggable() {
		return this.location.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.location.getContext();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Obj getSource() {
		return this.source;
	}

	public final DefKind getKind() {
		return this.kind;
	}

	public final boolean isValue() {
		return getKind().isValue();
	}

	public final boolean hasPrerequisite() {
		return this.hasPrerequisite;
	}

	@Override
	public D rescope(Rescoper rescoper) {

		final Rescoper oldRescoper = getRescoper();
		final Rescoper newRescoper = oldRescoper.and(rescoper);

		if (newRescoper.equals(oldRescoper)) {
			return self();
		}

		return create(newRescoper, rescoper);
	}

	@Override
	public D rescope(BoundPath path) {
		return rescope(path.toRescoper());
	}

	@Override
	public D upgradeScope(Scope scope) {
		if (scope == getScope()) {
			return self();
		}
		return rescope(upgradeRescoper(getScope(), scope));
	}

	public D rescope(Scope scope) {
		if (getScope() == scope) {
			return self();
		}
		return rescope(getScope().rescoperTo(this, scope));
	}

	public void resolveAll(Resolver resolver) {
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			getRescoper().resolveAll(resolver);
			fullyResolve(getRescoper().rescope(resolver));
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final Logical fullLogical() {
		return getLogical().rescope(getRescoper());
	}

	public final D addPrerequisite(Logical prerequisite) {

		final Logical oldPrerequisite = getPrerequisite();
		final Logical newPrerequisite = oldPrerequisite.and(prerequisite);

		if (oldPrerequisite.sameAs(newPrerequisite)) {
			return self();
		}

		final D copy = copy();

		copy.hasPrerequisite = true;
		copy.prerequisite = newPrerequisite;

		return copy;
	}

	public final D addPrecondition(Logical precondition) {

		final Logical oldPrecondition = getPrecondition();
		final Logical newPrecondition =
				Logical.and(oldPrecondition, precondition);

		if (newPrecondition == oldPrecondition) {
			return self();
		}

		final D copy = copy();

		copy.precondition = newPrecondition;
		copy.logical = null;

		return copy;
	}

	public final D claim() {
		if (getKind().isClaim()) {
			return self();
		}

		final D copy = copy();

		copy.kind = this.kind.claim();

		return copy;
	}

	public final D unclaim() {
		if (!getKind().isClaim()) {
			return self();
		}

		final D copy = copy();

		copy.kind = this.kind.unclaim();

		return copy;
	}

	public abstract boolean impliesWhenAfter(D def);

	public abstract boolean impliesWhenBefore(D def);

	public abstract ValueDef toValue();

	public abstract CondDef toCondition();

	public abstract Definitions toDefinitions();

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

	public final boolean assertFullyResolved() {
		assert this.allResolved :
			this + " is not fully resolved";
		return true;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(name()).append('[');
		if (hasPrerequisite()) {
			out.append(getPrerequisite()).append("? ");
		}

		final Logical precondition = getPrecondition();

		if (!precondition.isTrue()) {
			out.append(precondition).append(", ");
		}
		if (isValue()) {
			out.append('=');
		}
		out.append(getLocation());
		if (getKind().isClaim()) {
			out.append("!]");
		} else {
			out.append(".]");
		}

		return out.toString();
	}

	@SuppressWarnings("unchecked")
	protected final D self() {
		return (D) this;
	}

	protected abstract D create(
			Rescoper rescoper,
			Rescoper additionalRescoper);

	protected abstract void fullyResolve(Resolver resolver);

	protected final Logical getPrerequisite() {
		if (this.prerequisite != null) {
			return this.prerequisite;
		}
		this.prerequisite = buildPrerequisite();
		assert this.prerequisite != null :
			"Definition without prerequisite";
		if (!hasPrerequisite()) {
			assert this.prerequisite.isTrue() :
				"No prerequisite, so it should be TRUE: " + this.prerequisite;
		}
		return this.prerequisite;
	}

	protected abstract Logical buildPrerequisite();

	protected final Logical getPrecondition() {
		if (this.precondition != null) {
			return this.precondition;
		}
		return this.precondition = buildPrecondition();
	}

	protected abstract Logical buildPrecondition();

	protected final Logical getLogical() {
		if (this.logical != null) {
			return this.logical;
		}
		return this.logical = getPrecondition().and(buildLogical());
	}

	protected abstract Logical buildLogical();

	protected abstract String name();

	protected final LocationInfo getLocation() {
		return this.location;
	}

	final void update(DefKind kind, boolean hasPrerequisite) {
		this.kind = kind;
		this.hasPrerequisite = hasPrerequisite;
	}

	private final D copy() {
		return create(getRescoper(), transparentRescoper(getScope()));
	}

}
