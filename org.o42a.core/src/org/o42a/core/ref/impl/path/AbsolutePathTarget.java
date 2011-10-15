/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
import org.o42a.core.artifact.Artifact;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class AbsolutePathTarget extends Ref {

	private final BoundPath path;

	public AbsolutePathTarget(
			LocationInfo location,
			Distributor distributor,
			Path path) {
		super(location, distributor);
		this.path = path.bind(getScope());
	}

	@Override
	public boolean isConstant() {
		return getResolution().isConstant();
	}

	@Override
	public boolean isKnownStatic() {
		return true;
	}

	@Override
	public final Path getPath() {
		return this.path.getPath();
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public Resolution resolve(Resolver resolver) {
		return resolve(resolver, pathResolver(this, resolver));
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

		final Path path = this.path.getRawPath();
		final Path upPath = path.cutArtifact();

		if (upPath != path) {

			final TypeRef ancestor;
			final TypeRef typeRef = artifact.getTypeRef();

			if (typeRef != null) {
				ancestor = typeRef;
			} else {
				ancestor = artifact.materialize().type().getAncestor();
			}

			return ancestor.rescope(upPath.rescoper(getScope()));
		}

		final Path dematerializedPath = path.dematerialize();

		if (path == dematerializedPath) {
			return super.ancestor(location);
		}

		final Ref dematerialized =
				dematerializedPath.target(this, distribute());

		return dematerialized.ancestor(location);
	}

	@Override
	public Ref materialize() {

		final Path path = this.path.getRawPath();
		final Path materialized = path.materialize();

		if (materialized == path) {
			return this;
		}

		return new AbsolutePathTarget(this, distribute(), materialized);
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		return new AbsolutePathTarget(
				this,
				reproducer.distribute(),
				this.path.getRawPath());
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return this.path.toString();
	}

	@Override
	protected FieldDefinition createFieldDefinition() {
		return new PathTargetDefinition(this);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolve(resolver, fullPathResolver(this, resolver)).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		resolve(resolver, valuePathResolver(this, resolver)).resolveValues(
				resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private Resolution resolve(Resolver resolver, PathResolver pathResolver) {
		return resolver.path(
				pathResolver,
				this.path,
				getContext().getRoot().getScope());
	}

	private static final class Op extends RefOp {

		Op(HostOp host, AbsolutePathTarget ref) {
			super(host, ref);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final AbsolutePathTarget ref = (AbsolutePathTarget) getRef();

			return ref.path.writeAbolute(dirs);
		}

	}

}
