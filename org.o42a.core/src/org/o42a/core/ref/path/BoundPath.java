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
package org.o42a.core.ref.path;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.path.PathResolution.NO_PATH_RESOLUTION;
import static org.o42a.core.ref.path.PathResolution.PATH_RESOLUTION_ERROR;
import static org.o42a.core.ref.path.PathResolution.pathResolution;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.impl.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;
import org.o42a.util.Deferred;


public class BoundPath extends Location {

	private final Deferred<Scope> deferredOrigin;
	private Scope origin;
	private final Path rawPath;
	private Path path;

	private Obj startObject;
	private int startIndex;

	BoundPath(LocationInfo location, Scope origin, Path rawPath) {
		super(location);
		this.deferredOrigin = null;
		this.origin = origin;
		this.rawPath = rawPath;
	}

	BoundPath(
			LocationInfo location,
			Deferred<Scope> deferredOrigin,
			Path rawPath) {
		super(location);
		this.deferredOrigin = deferredOrigin;
		this.rawPath = rawPath;
	}

	public final Scope getOrigin() {
		if (this.origin != null) {
			return this.origin;
		}
		return this.origin = this.deferredOrigin.get();
	}

	public final Path getPath() {
		if (this.path != null) {
			return this.path;
		}
		return this.path = rebuild();
	}

	public final Path getRawPath() {
		if (this.path != null) {
			return this.path;
		}
		return this.rawPath;
	}

	public final PathKind getKind() {
		return getRawPath().getKind();
	}

	public final boolean isAbsolute() {
		return getRawPath().isAbsolute();
	}

	public final boolean isSelf() {
		return getPath().isSelf();
	}

	public final Step[] getSteps() {
		return getPath().getSteps();
	}

	public final PathResolution resolve(
			PathResolver resolver,
			Scope start) {
		return walk(resolver, start, DUMMY_PATH_WALKER);
	}

	public PathResolution walk(
			PathResolver resolver,
			Scope start,
			PathWalker walker) {
		if (isAbsolute()) {
			return walkPath(
					resolver,
					start.getContext().getRoot().getScope(),
					walker);
		}

		start.assertDerivedFrom(getOrigin());

		return walkPath(resolver, start, walker);
	}

	public Rescoper rescoper() {
		if (!isAbsolute() && getSteps().length == 0) {
			return transparentRescoper(getOrigin());
		}
		return new PathRescoper(this);
	}

	public final PathReproduction reproduce(Reproducer reproducer) {
		return getKind().reproduce(reproducer, this);
	}

	public final HostOp write(CodeDirs dirs, HostOp start) {
		if (isAbsolute()) {
			return writeAbolute(dirs);
		}

		final Step[] steps = getSteps();
		HostOp found = start;

		for (int i = 0; i < steps.length; ++i) {
			found = steps[i].write(dirs, found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
	}

	public final HostOp writeAbolute(CodeDirs dirs) {
		assert isAbsolute() :
			this + " is not absolute path";

		final CodeBuilder builder = dirs.getBuilder();
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
	public String toString() {
		return toString(getSteps().length);
	}

	public String toString(int length) {
		return getRawPath().toString(
				this.origin != null ? this.origin : this.deferredOrigin,
				length);
	}

	private PathTracker startWalk(
			PathResolver resolver,
			Scope start,
			PathWalker walker) {
		if (!isAbsolute()) {
			if (!walker.start(this, start)) {
				return null;
			}
			return new PathTracker(resolver, walker);
		}
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
		if (this.startObject == null) {
			findStart(context);
		}
		return this.startIndex;
	}

	private Obj startObject(CompilerContext context) {
		if (this.startObject == null) {
			findStart(context);
		}
		return this.startObject;
	}

	private void findStart(CompilerContext context) {

		final AbsolutePathStartFinder walker = new AbsolutePathStartFinder();

		walk(
				pathResolver(dummyUser()),
				context.getRoot().getScope(),
				walker);

		this.startIndex = walker.getStartIndex();
		this.startObject = walker.getStartObject();
	}

	private PathResolution walkPath(
			PathResolver resolver,
			Scope start,
			PathWalker walker) {

		final PathTracker tracker = startWalk(resolver, start, walker);

		if (tracker == null) {
			return null;
		}

		final Step[] steps = getSteps();
		Container result = start.getContainer();
		Scope prev = start;

		for (int i = 0; i < steps.length; ++i) {
			result = steps[i].resolve(
					tracker.nextResolver(),
					this,
					i,
					prev,
					tracker);
			if (tracker.isAborted()) {
				return NO_PATH_RESOLUTION;
			}
			if (result == null) {
				tracker.abortedAt(prev, steps[i]);
				return PATH_RESOLUTION_ERROR;
			}
			prev = result.getScope();
		}

		if (!tracker.done(result)) {
			return null;
		}

		return pathResolution(this, result);
	}

	Path rebuild() {

		final Step[] steps = this.rawPath.getSteps();
		final Step[] rebuilt = rebuild(steps);

		if (rebuilt == steps) {
			return this.rawPath;
		}

		return new Path(getKind(), rebuilt);
	}

	private static Step[] rebuild(Step[] steps) {
		if (steps.length <= 1) {
			return steps;
		}

		final Step[] rebuiltSteps = new Step[steps.length];
		Step prev = rebuiltSteps[0] = steps[0];
		int nextIdx = 1;
		int rebuiltIdx = 0;

		for (;;) {

			final Step next = steps[nextIdx];
			final Step rebuilt = next.rebuild(prev);

			if (rebuilt != null) {
				rebuiltSteps[rebuiltIdx] = prev = rebuilt;
				if (++nextIdx >= steps.length) {
					break;
				}
				continue;
			}

			rebuiltSteps[++rebuiltIdx] = prev = next;
			if (++nextIdx >= steps.length) {
				break;
			}
		}

		final int rebuiltLen = rebuiltIdx + 1;

		if (rebuiltLen == steps.length) {
			return steps;
		}

		return rebuild(ArrayUtil.clip(rebuiltSteps, rebuiltLen));
	}

}
