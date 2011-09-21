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
package org.o42a.core.ref.path;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class AbsolutePath extends Path {

	private CompilerContext context;
	private Obj startObject;
	private int startIndex;

	AbsolutePath(PathFragment... fragments) {
		super(true, fragments);
	}

	public AbsolutePath append(CompilerContext context, String... fields) {

		AbsolutePath path = this;
		Obj object = resolve(context).getArtifact().toObject();

		for (String field : fields) {

			final Member member = object.field(field);

			assert member != null :
				"Field \"" + field + "\" not found in " + object;

			path = path.append(member.getKey());
			object = member.substance(dummyUser()).toObject();
		}

		return path;
	}

	public final Ref target(CompilerContext context) {

		final Artifact<?> target = resolve(context).getArtifact();

		return target(target, declarativeDistributor(target.getContainer()));
	}

	public final Ref target(Scope scope) {

		final Artifact<?> target = resolve(scope.getContext()).getArtifact();

		return target(target, declarativeDistributor(scope.getContainer()));
	}

	public final Ref target(Container container) {

		final Artifact<?> target =
				resolve(container.getContext()).getArtifact();

		return target(target, declarativeDistributor(container));
	}

	@Override
	public Ref target(
			LocationInfo location,
			Distributor distributor,
			Ref start) {
		return new AbsolutePathTarget(location, distributor, this);
	}

	@Override
	public Ref target(LocationInfo location, Distributor distributor) {
		return new AbsolutePathTarget(location, distributor, this);
	}

	public PathResolution resolve(CompilerContext context) {
		return resolve(
				pathResolver(context, dummyUser()),
				context.getRoot().getScope());
	}

	@Override
	public AbsolutePath materialize() {
		return (AbsolutePath) super.materialize();
	}

	@Override
	public AbsolutePath dematerialize() {
		return (AbsolutePath) super.dematerialize();
	}

	@Override
	public AbsolutePath append(PathFragment fragment) {
		return (AbsolutePath) super.append(fragment);
	}

	@Override
	public AbsolutePath append(MemberKey memberKey) {
		return (AbsolutePath) super.append(memberKey);
	}

	@Override
	public AbsolutePath append(Path path) {
		return (AbsolutePath) super.append(path);
	}

	@Override
	public AbsolutePath rebuild() {
		return (AbsolutePath) super.rebuild();
	}

	@Override
	public AbsolutePath rebuildWithRef(Ref followingRef) {
		return (AbsolutePath) super.rebuildWithRef(followingRef);
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer) {
		return unchangedPath(this);
	}

	@Override
	PathTracker startWalk(
			PathResolver resolver,
			Scope start,
			PathWalker walker) {
		if (!walker.root(this, start)) {
			return null;
		}
		if (resolver.toUser().isDummy()) {
			return new PathTracker(resolver, walker);
		}
		return new AbsolutePathTracker(
				resolver,
				walker,
				startIndex(start.getContext()));
	}

	HostOp write(CodeDirs dirs, CodeBuilder builder) {

		final CompilerContext context = builder.getContext();
		final ObjectIR start = startObject(context).ir(dirs.getGenerator());
		HostOp found = start.op(builder, dirs.code());
		final PathFragment[] fragments = getFragments();

		for (int i = startIndex(context); i < fragments.length; ++i) {
			found = fragments[i].write(dirs, found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
	}

	Obj startObject(CompilerContext context) {
		if (this.startObject == null || !this.context.compatible(context)) {
			findStart(context);
		}
		return this.startObject;
	}

	int startIndex(CompilerContext context) {
		if (this.startObject == null || !this.context.compatible(context)) {
			findStart(context);
		}
		return this.startIndex;
	}

	private void findStart(CompilerContext context) {

		final AbsolutePathStartFinder walker = new AbsolutePathStartFinder();

		walk(
				pathResolver(context, dummyUser()),
				context.getRoot().getScope(),
				walker);

		this.context = context;
		this.startIndex = walker.getStartIndex();
		this.startObject = walker.getStartObject();
	}

}
