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

	private static final byte RESOLUTION = 0x01;
	private static final byte FULL_RESOLUTION = 0x02 | RESOLUTION;
	private static final byte VALUES_RESOLUTION = 0x04 | FULL_RESOLUTION;

	private static final byte ABSENT = 0x10;
	private static final byte ERROR = 0x20;

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

	public final void resolveAll() {

		final Clause clause = resolve(FULL_RESOLUTION).toClause();

		if (clause != null) {
			clause.resolveAll();
		} else {
			toArtifact().resolveAll();
		}
	}

	public final void resolveValues() {

		final Obj materialized =
				resolve(VALUES_RESOLUTION).toArtifact().materialize();

		if (materialized != null) {
			materialized.value().resolveAll(getResolver());
		}
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
		return resolve(RESOLUTION);
	}

	private final int checkFlags(byte flag) {
		getResolved();
		return this.flags & flag;
	}

	private final Container resolve(byte flags) {
		if (this.flags != 0) {
			if ((this.flags & flags) == flags) {
				return this.resolved;
			}
			if ((this.flags & NO_VALUE) != 0) {
				return this.resolved;
			}
		}

		final BoundPath path = getRef().getPath();
		final Resolver resolver = getResolver();
		final PathResolver pathResolver;

		if ((flags & FULL_RESOLUTION) != FULL_RESOLUTION) {
			pathResolver = resolver.toPathResolver();
		} else if ((flags & VALUES_RESOLUTION) == VALUES_RESOLUTION) {
			pathResolver = resolver.toValuePathResolver();
		} else {
			pathResolver = resolver.toFullPathResolver();
		}

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

		this.flags = flags;

		return this.resolved = pathResolution.getResult();
	}

}
