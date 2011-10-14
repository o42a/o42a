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

import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class AbsolutePath extends Path {

	private CompilerContext context;
	private Obj startObject;
	private int startIndex;

	AbsolutePath(Step... steps) {
		super(true, steps);
	}

	public final CompilerContext getContext() {
		return this.context;
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

	@Override
	public AbsolutePath append(Step step) {
		return (AbsolutePath) super.append(step);
	}

	@Override
	public AbsolutePath append(MemberKey memberKey) {
		return (AbsolutePath) super.append(memberKey);
	}

	@Override
	public AbsolutePath cutArtifact() {
		return (AbsolutePath) super.cutArtifact();
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
	public AbsolutePath arrayItem(Ref indexRef) {
		return (AbsolutePath) super.arrayItem(indexRef);
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

	public HostOp write(CodeDirs dirs, CodeBuilder builder) {

		final CompilerContext context = builder.getContext();
		final ObjectIR start = startObject(context).ir(dirs.getGenerator());
		HostOp found = start.op(builder, dirs.code());
		final Step[] steps = getSteps();

		for (int i = startIndex(context); i < steps.length; ++i) {
			found = steps[i].write(dirs, found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
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

	private int startIndex(CompilerContext context) {
		if (this.startObject == null || !this.context.compatible(context)) {
			findStart(context);
		}
		return this.startIndex;
	}

	private Obj startObject(CompilerContext context) {
		if (this.startObject == null || !this.context.compatible(context)) {
			findStart(context);
		}
		return this.startObject;
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
