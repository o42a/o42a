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

import static org.o42a.core.ref.RefUsage.*;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.*;
import org.o42a.util.log.Loggable;


public final class Resolution implements ScopeInfo {

	private static final byte RESOLVED = 0x01;
	private static final byte ABSENT = 0x20;
	private static final byte ERROR = 0x40;

	private static final byte NO_VALUE = ERROR | ABSENT;

	private final Ref ref;
	private final Resolver resolver;
	private Container resolved;
	private byte flags;

	Resolution(Ref ref, Resolver resolver) {
		this.ref = ref;
		this.resolver = resolver;
	}

	public final Ref getRef() {
		return this.ref;
	}

	public final Resolver getResolver() {
		return this.resolver;
	}

	@Override
	public final CompilerContext getContext() {
		return getResolver().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return getResolver().getLoggable();
	}

	@Override
	public final Scope getScope() {
		return getResolved().getScope();
	}

	public final boolean isError() {
		return checkFlags(ERROR) != 0;
	}

	public final boolean isResolved() {
		return checkFlags(ABSENT) == 0;
	}

	public final boolean isFalse() {
		return toObject() == getContext().getFalse();
	}

	public final Clause toClause() {
		return getResolved().toClause();
	}

	public final Artifact<?> toArtifact() {
		return getResolved().toArtifact();
	}

	public final Obj toObject() {
		return getResolved().toObject();
	}

	public final Link toLink() {
		return toArtifact().toLink();
	}

	public Directive toDirective() {

		final Obj materialized = materialize();

		if (materialized == null) {
			return null;
		}
		if (materialized.value().getValueType() != ValueType.DIRECTIVE) {
			return null;
		}

		final Value<Directive> value = ValueStruct.DIRECTIVE.cast(
				materialized.value().explicitUseBy(getResolver()).getValue());

		if (!value.getKnowledge().isKnown()) {
			getResolver().getLogger().error(
					"runtime_directive",
					this,
					"Unable to execute directive at compile time");
			return null;
		}

		return value.getCompilerValue();
	}

	public final Obj materialize() {

		final Artifact<?> artifact = toArtifact();

		if (artifact == null) {
			return null;
		}

		return artifact.materialize();
	}

	public final Resolution resolveAll() {
		return resolveAll(RESOLUTION_REF_USAGE);
	}

	public final Resolution resolveTarget() {
		return resolveAll(TARGET_REF_USAGE);
	}

	public final Resolution resolveAssignee() {
		return resolveAll(ASSIGNEE_REF_USAGE);
	}

	public final Resolution resolveType() {
		return resolveAll(TYPE_REF_USAGE);
	}

	public final Resolution resolveLogical() {
		return resolveAll(LOGICAL_REF_USAGE);
	}

	public final Resolution resolveValue() {
		return resolveAll(VALUE_REF_USAGE);
	}

	public final Resolution resolveAll(RefUsage usage) {
		getRef().refFullyResolved();

		if ((this.flags & NO_VALUE) == 0) {

			final Container resolved =
					resolve(getResolver().toFullPathResolver(usage));

			if (resolved != null) {
				usage.fullyResolve(this, resolved);
			}
		}

		return this;
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
		if (this.resolved != null) {
			return this.resolved.toString();
		}
		return "Resolution[" + this.ref + ']';
	}

	private final Container getResolved() {
		if (this.flags != 0) {
			return this.resolved;
		}
		return resolve(getResolver().toPathResolver());
	}

	private final int checkFlags(byte flag) {
		getResolved();
		return this.flags & flag;
	}

	private final Container resolve(PathResolver pathResolver) {

		final Resolver resolver = getResolver();
		final BoundPath path = getRef().getPath();
		final PathResolution pathResolution = path.walk(
				pathResolver.resolveBy(resolver),
				resolver.getWalker());

		if (!pathResolution.isResolved()) {
			if (pathResolution.isError()) {
				this.flags = ERROR;
				return this.resolved = getContext().getFalse();
			}
			this.flags = ABSENT;
			return null;
		}

		this.flags = RESOLVED;

		return this.resolved = pathResolution.getResult();
	}

}
