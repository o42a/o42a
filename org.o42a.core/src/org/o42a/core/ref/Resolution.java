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
package org.o42a.core.ref;

import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.materializePath;
import static org.o42a.core.value.Directive.SKIP_DIRECTIVE;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.GroupClause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Directive;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.Loggable;


public abstract class Resolution implements ScopeInfo {

	private final ScopeInfo resolved;

	Resolution(ScopeInfo resolved) {
		this.resolved = resolved;
	}

	@Override
	public final CompilerContext getContext() {
		return this.resolved.getContext();
	}

	@Override
	public Loggable getLoggable() {
		return this.resolved.getLoggable();
	}

	@Override
	public final Scope getScope() {
		return this.resolved.getScope();
	}

	public boolean isError() {
		return false;
	}

	public final boolean isFalse() {
		return toObject() == getContext().getFalse();
	}

	public abstract Container toContainer();

	public abstract Artifact<?> toArtifact();

	public final Obj toObject() {
		return toArtifact().toObject();
	}

	public final Link toLink() {
		return toArtifact().toLink();
	}

	public final Array toArray() {
		return toArtifact().toArray();
	}

	public Directive toDirective(Resolver resolver) {

		final Obj materialized = materialize();

		if (materialized == null) {
			return null;
		}
		if (materialized.getValueType() != ValueType.DIRECTIVE) {
			return null;
		}

		final Value<Directive> value = ValueType.DIRECTIVE.cast(
				materialized.value(resolver).getValue());

		if (!value.isDefinite()) {
			resolver.getLogger().error(
					"runtime_directive",
					this,
					"Unable to execute directive at compile time");
			return null;
		}

		return value.getDefiniteValue();
	}

	public abstract Obj materialize();

	public abstract Path materializationPath();

	public abstract Path member(
			PlaceInfo user,
			MemberId memberId,
			Obj declaredIn);

	public abstract void resolveAll();

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
		return this.resolved.toString();
	}

	protected final ScopeInfo getResolved() {
		return this.resolved;
	}

	static final class Error extends Resolution {

		Error(Ref errorRef) {
			super(errorRef);
		}

		@Override
		public boolean isError() {
			return true;
		}

		@Override
		public final Container toContainer() {
			return toArtifact();
		}

		@Override
		public final Obj toArtifact() {
			return getContext().getFalse();
		}

		@Override
		public final Directive toDirective(Resolver resolver) {
			return SKIP_DIRECTIVE;
		}

		@Override
		public final Obj materialize() {
			return toArtifact();
		}

		@Override
		public final Path materializationPath() {
			return SELF_PATH;
		}

		@Override
		public Path member(PlaceInfo user, MemberId memberId, Obj declaredIn) {
			return null;
		}

		@Override
		public void resolveAll() {
		}

		@Override
		public String toString() {
			return "ERROR";
		}

	}

	static final class ArtifactResolution extends Resolution {

		ArtifactResolution(Artifact<?> resolved) {
			super(resolved);
		}

		@Override
		public final Container toContainer() {
			return toArtifact().getContainer();
		}

		@Override
		public final Artifact<?> toArtifact() {
			return (Artifact<?>) getResolved();
		}

		@Override
		public final Obj materialize() {
			return toArtifact().materialize();
		}

		@Override
		public final Path materializationPath() {
			return materializePath();
		}

		@Override
		public Path member(PlaceInfo user, MemberId memberId, Obj declaredIn) {

			final Obj owner = materialize();
			final Path found = owner.member(user, memberId, declaredIn);

			if (found == null) {
				user.getContext().getLogger().unresolved(user, memberId);
				return null;
			}

			return materializationPath().append(found);
		}

		@Override
		public void resolveAll() {
			toArtifact().resolveAll();
		}

	}

	static final class ObjectResolution extends Resolution {

		ObjectResolution(Obj resolved) {
			super(resolved);
		}

		@Override
		public final Obj toContainer() {
			return toArtifact();
		}

		@Override
		public final Obj toArtifact() {
			return (Obj) getResolved();
		}

		@Override
		public final Obj materialize() {
			return toArtifact();
		}

		@Override
		public final Path materializationPath() {
			return SELF_PATH;
		}

		@Override
		public Path member(PlaceInfo user, MemberId memberId, Obj declaredIn) {

			final Obj owner = toArtifact();
			final Path found = owner.member(user, memberId, declaredIn);

			if (found == null) {
				user.getContext().getLogger().unresolved(user, memberId);
				return null;
			}

			return found;
		}

		@Override
		public void resolveAll() {
			toArtifact().resolveAll();
		}

	}

	static final class GroupResolution extends Resolution {

		GroupResolution(GroupClause resolved) {
			super(resolved);
		}

		@Override
		public final GroupClause toContainer() {
			return (GroupClause) getResolved();
		}

		@Override
		public final Artifact<?> toArtifact() {
			return toContainer().toArtifact();
		}

		@Override
		public final Obj materialize() {
			return toArtifact().toObject();
		}

		@Override
		public Path materializationPath() {
			return SELF_PATH;
		}

		@Override
		public Path member(PlaceInfo user, MemberId memberId, Obj declaredIn) {

			final Path found =
				toContainer().member(user, memberId, declaredIn);

			if (found == null) {
				user.getContext().getLogger().unresolved(user, memberId);
				return null;
			}

			return found;
		}

		@Override
		public void resolveAll() {
			toContainer().resolveAll();
		}

	}

	static final class LocalResolution extends Resolution {

		LocalResolution(LocalScope local) {
			super(local);
		}

		@Override
		public final LocalScope toContainer() {
			return (LocalScope) getResolved();
		}

		@Override
		public final Artifact<?> toArtifact() {
			return null;
		}

		@Override
		public final Obj materialize() {
			return null;
		}

		@Override
		public Path materializationPath() {
			return SELF_PATH;
		}

		@Override
		public Path member(PlaceInfo user, MemberId memberId, Obj declaredIn) {

			final Path found =
				toContainer().member(user, memberId, declaredIn);

			if (found == null) {
				user.getContext().getLogger().unresolved(user, memberId);
				return null;
			}

			return found;
		}

		@Override
		public void resolveAll() {
			toContainer().resolveAll();
		}

	}

}
