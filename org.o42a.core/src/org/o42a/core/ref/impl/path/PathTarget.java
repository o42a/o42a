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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathResolver.valuePathResolver;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.Deferred;
import org.o42a.util.Holder;


public final class PathTarget extends Ref {

	private final Path path;
	private final Ref start;
	private BoundPath boundPath;
	private Holder<Path> fullPath;

	public PathTarget(
			LocationInfo location,
			Distributor distributor,
			Path path,
			Ref start) {
		super(location, distributor);
		this.path = path;
		this.start = start;
		if (start == null) {
			this.boundPath = path.bind(this, getScope());
			this.fullPath = new Holder<Path>(path);
		}
	}

	private PathTarget(
			LocationInfo location,
			Distributor distributor,
			BoundPath path,
			Ref start) {
		super(location, distributor);
		this.path = path.getRawPath();
		this.start = start;
		this.boundPath = path;
		if (start == null) {
			this.fullPath = new Holder<Path>(this.path);
		}
	}

	public final Ref getStart() {
		return this.start;
	}

	@Override
	public boolean isKnownStatic() {
		if (this.path.isStatic()) {
			return true;
		}
		return this.start != null && this.start.isKnownStatic();
	}

	@Override
	public boolean isStatic() {
		if (this.path.isStatic()) {
			return true;
		}
		return this.start != null && this.start.isKnownStatic();
	}

	@Override
	public boolean isConstant() {
		return isStatic() && getResolution().isConstant();
	}

	@Override
	public Path getPath() {
		if (this.fullPath != null) {
			return this.fullPath.get();
		}

		final Path startPath = this.start.getPath();

		if (startPath == null) {
			this.fullPath = new Holder<Path>(null);
			return null;
		}

		final Path fullPath = startPath.append(this.path);

		this.fullPath = new Holder<Path>(fullPath);

		return fullPath;
	}

	@Override
	public Resolution resolve(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return resolve(resolver, pathResolver(resolver));
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {

		final Resolution resolution = getResolution();

		if (resolution.isError()) {
			return errorRef(this, distribute()).toTypeRef();
		}

		final Artifact<?> artifact = resolution.toArtifact();

		if (artifact == null) {
			getLogger().notArtifact(resolution);
			return errorRef(this, distribute()).toTypeRef();
		}

		final Path fullPath = getPath();

		if (fullPath != null) {
			return getBoundPath().ancestor(location, distribute());
		}

		final Path path = getBoundPath().getPath();
		final Path upPath = path.cutArtifact();

		if (upPath != path) {

			final TypeRef ancestor;
			final TypeRef typeRef = artifact.getTypeRef();

			if (typeRef != null) {
				ancestor = typeRef;
			} else {
				ancestor = artifact.materialize().type().getAncestor();
			}

			return ancestor.rescope(pathRescoper(this.start, upPath));
		}

		final Path dematerializedPath = path.dematerialize();

		if (path == dematerializedPath) {
			return super.ancestor(location);
		}

		final Ref dematerialized;

		if (this.start == null) {
			dematerialized = dematerializedPath.target(this, distribute());
		} else {
			dematerialized =
					dematerializedPath.target(this, distribute(), this.start);
		}

		return dematerialized.ancestor(location);
	}

	@Override
	public Ref materialize() {

		final Path fullPath = getPath();
		final Path path;
		final Ref start;

		if (fullPath != null) {
			path = fullPath;
			start = null;
		} else {
			path = this.path;
			start = this.start;
		}

		final Path materialized = path.materialize();

		if (materialized == path) {
			return this;
		}

		return new PathTarget(this, distribute(), materialized, start);
	}

	@Override
	public Ref rescope(Rescoper rescoper) {
		if (this.start != null) {
			return this.path.target(
					this,
					distributeIn(rescoper.getFinalScope().getContainer()),
					this.start.rescope(rescoper));
		}

		if (!(rescoper instanceof PathRescoper)) {
			return super.rescope(rescoper);
		}

		final PathRescoper pathRescoper = (PathRescoper) rescoper;
		final Path rescopePath = pathRescoper.getPath().getRawPath();

		return rescopePath.append(this.path).target(
				this,
				distributeIn(rescoper.getFinalScope().getContainer()));
	}

	@Override
	public Ref toStatic() {
		if (isKnownStatic()) {
			return this;
		}
		if (this.boundPath != null) {
			return new PathTarget(
					this,
					distribute(),
					this.boundPath.toStatic(),
					getStart());
		}

		final BoundPath staticPath =
				this.path.bindStatically(this, new PathStart());

		return new PathTarget(
				this,
				distribute(),
				staticPath.getRawPath(),
				getStart());
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Path fullPath = getPath();
		final Ref start;
		final BoundPath path = getBoundPath();

		if (fullPath == null) {
			start = this.start;
		} else if (fullPath.isAbsolute()) {
			return fullPath.target(this, reproducer.distribute());
		} else {
			start = null;
		}

		final PathReproduction pathReproduction = path.reproduce(reproducer);

		if (pathReproduction == null) {
			return null;
		}
		if (pathReproduction.isUnchanged()) {
			if (!reproducer.isTopLevel()) {
				return reproducePart(
						reproducer,
						start,
						pathReproduction.getExternalPath());
			}
			// Top-level reproducer`s scope is not compatible with path
			// and requires rescoping.
			return startrWithPrefix(
					reproducer,
					pathReproduction,
					reproducer.getPhrasePrefix().materialize());
		}

		final PathTarget reproducedPart = reproducePart(
				reproducer,
				start,
				pathReproduction.getReproducedPath());

		if (!pathReproduction.isOutOfClause()) {
			return reproducedPart;
		}

		return startrWithPrefix(
				reproducer,
				pathReproduction,
				reproducer.getPhrasePrefix().materialize().rescope(
						reproducedPart.toRescoper()));
	}

	@Override
	public Rescoper toRescoper() {
		return pathRescoper(this.start, this.path);
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		if (this.fullPath != null) {

			final Path fullPath = this.fullPath.get();

			if (fullPath != null) {
				return fullPath.toString();
			}
		}
		if (this.start == null) {
			return this.path.toString();
		}
		return this.start + ":" + this.path;
	}

	protected final Path path() {
		return this.path;
	}

	@Override
	protected FieldDefinition createFieldDefinition() {
		if (this.start == null) {
			return this.path.fieldDefinition(this, distribute());
		}
		return new PathTargetDefinition(this);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		if (this.start != null) {
			this.start.resolveAll(resolver);
		}
		resolve(resolver, fullPathResolver(resolver)).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		resolve(resolver, valuePathResolver(resolver)).resolveValues(
				resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private BoundPath getBoundPath() {
		if (this.boundPath != null) {
			return this.boundPath;
		}

		final Path fullPath = getPath();

		if (fullPath != null) {
			return this.boundPath = fullPath.bind(this, getScope());
		}

		return this.boundPath = this.path.bind(
				this,
				this.start.resolve(getScope().dummyResolver()).getScope());
	}

	private Resolution resolve(Resolver resolver, PathResolver pathResolver) {

		final Path fullPath = getPath();

		if (fullPath != null) {
			return resolver.path(
					pathResolver,
					getBoundPath(),
					resolver.getScope());
		}

		final Resolution start = this.start.resolve(resolver);

		if (start == null) {
			return null;
		}
		if (start.isError()) {
			return start;
		}

		return resolver.path(pathResolver, getBoundPath(), start.getScope());
	}

	private PathTarget reproducePart(
			Reproducer reproducer,
			Ref start,
			Path path) {

		final Ref newStart;

		if (start == null) {
			newStart = null;
		} else {
			newStart = start.reproduce(reproducer);
			if (newStart == null) {
				return null;
			}
		}

		return new PathTarget(this, reproducer.distribute(), path, newStart);
	}

	private Ref startrWithPrefix(
			Reproducer reproducer,
			PathReproduction pathReproduction,
			Ref phrasePrefix) {

		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phrasePrefix;
		}

		return new PathTarget(
				this,
				reproducer.distribute(),
				externalPath,
				phrasePrefix);
	}

	private Rescoper pathRescoper(Ref start, Path path) {
		if (start == null) {
			return path.bind(this, getScope()).rescoper();
		}

		final Scope startScope =
				start.resolve(getScope().dummyResolver()).getScope();

		return path.bind(this, startScope).rescoper().and(start.toRescoper());
	}

	private final class PathStart implements Deferred<Scope> {

		@Override
		public Scope get() {
			return getStart().resolve(
					getScope().dummyResolver()).getScope();
		}

		@Override
		public String toString() {
			return getStart().toString();
		}

	}

	private static final class Op extends RefOp {

		Op(HostOp host, PathTarget ref) {
			super(host, ref);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final PathTarget ref = (PathTarget) getRef();
			final BoundPath boundPath = ref.getBoundPath();
			final HostOp start;

			if (ref.getPath() != null) {
				start = host();
			} else if (ref.start != null && !boundPath.isStatic()) {
				start = ref.start.op(host()).target(dirs);
			} else {
				start = host();
			}

			return boundPath.write(dirs, start);
		}

	}

}
