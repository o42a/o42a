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
import static org.o42a.core.ir.op.PathOp.hostPathOp;
import static org.o42a.core.ref.path.PathResolution.NO_PATH_RESOLUTION;
import static org.o42a.core.ref.path.PathResolution.PATH_RESOLUTION_ERROR;
import static org.o42a.core.ref.path.PathResolution.pathResolution;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ref.impl.path.*;
import org.o42a.core.ref.type.TypeRef;
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
		return getPath().getKind();
	}

	public final boolean isAbsolute() {
		return getPath().isAbsolute();
	}

	public final boolean isStatic() {
		return getPath().isStatic();
	}

	public final boolean isSelf() {
		return getPath().isSelf();
	}

	public final Step[] getSteps() {
		return getPath().getSteps();
	}

	public final Step[] getRawSteps() {
		return getRawPath().getSteps();
	}

	public TypeRef ancestor(LocationInfo location, Distributor distributor) {

		final Step[] steps = getRawSteps();

		if (steps.length == 0) {
			return new AncestorStep().toPath().typeRef(
					location,
					distributor);
		}

		final Step lastStep = steps[steps.length - 1];

		return lastStep.ancestor(this, location, distributor);
	}

	public final BoundPath materialize() {

		final Path rawPath = getRawPath();
		final Path materialized = rawPath.materialize();

		if (rawPath == materialized) {
			return this;
		}

		if (this.origin != null) {
			return materialized.bind(this, this.origin);
		}

		return materialized.bind(this, this.deferredOrigin);
	}

	public final BoundPath cut(int stepsToCut) {

		final Path newPath = getRawPath().cut(stepsToCut);

		if (this.origin != null) {
			return newPath.bind(this, this.origin);
		}

		return newPath.bind(this, this.deferredOrigin);
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
		return walkPath(getPath(), resolver, start, walker, false);
	}

	public Rescoper rescoper() {
		if (!getRawPath().isStatic() && getRawSteps().length == 0) {
			return transparentRescoper(getOrigin());
		}
		return new PathRescoper(this);
	}

	public final PathReproduction reproduce(Reproducer reproducer) {
		return getKind().reproduce(reproducer, this);
	}

	public final BoundPath toStatic() {
		if (getRawPath().isStatic()) {
			return this;
		}

		if (this.origin != null) {
			return getRawPath().bindStatically(this, this.origin);
		}

		return getRawPath().bindStatically(this, this.deferredOrigin);
	}

	public final PathOp op(CodeDirs dirs, HostOp start) {
		if (isStatic()) {
			return staticOp(dirs);
		}

		final Step[] steps = getSteps();
		PathOp found = hostPathOp(start);

		for (int i = 0; i < steps.length; ++i) {
			found = steps[i].op(found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
	}

	public final PathOp staticOp(CodeDirs dirs) {
		assert isStatic() :
			this + " is not a static path";

		final CodeBuilder builder = dirs.getBuilder();
		final CompilerContext context = builder.getContext();
		final ObjectIR start = startObject(context).ir(dirs.getGenerator());
		PathOp found = hostPathOp(start.op(builder, dirs.code()));
		final Step[] steps = getSteps();

		for (int i = startIndex(context); i < steps.length; ++i) {
			found = steps[i].op(found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
	}

	@Override
	public String toString() {
		return toString(getRawSteps().length);
	}

	public String toString(int length) {
		return getRawPath().toString(
				this.origin != null ? this.origin : this.deferredOrigin,
				length);
	}

	private PathResolution walkPath(
			Path path,
			PathResolver resolver,
			Scope start,
			PathWalker walker,
			boolean expand) {
		this.path = path;

		final Scope startFrom;
		final PathTracker tracker;

		if (isAbsolute()) {
			startFrom = root(start);
			if (!walker.root(this, startFrom)) {
				return null;
			}
			if (expand) {
				tracker = new PathRecorder(
						this,
						resolver,
						walker,
						startFrom,
						true);
			} else if (resolver.toUser().isDummy()) {
				tracker = new SimplePathTracker(this, resolver, walker);
			} else {
				tracker = new StaticPathTracker(
						this,
						resolver,
						walker,
						startIndex(start.getContext()));
			}
		} else {
			startFrom = start;
			startFrom.assertDerivedFrom(getOrigin());
			if (!walker.start(this, startFrom)) {
				return null;
			}
			if (expand) {
				tracker = new PathRecorder(
						this,
						resolver,
						walker,
						startFrom,
						false);
			} else if (!isStatic() || resolver.toUser().isDummy()) {
				tracker = new SimplePathTracker(this, resolver, walker);
			} else {
				tracker = new StaticPathTracker(
						this,
						resolver,
						walker,
						startIndex(start.getContext()));
			}
		}

		Step[] steps = this.path.getSteps();
		Container result = startFrom.getContainer();
		Scope prev = startFrom;
		int i = 0;

		while (i < steps.length) {

			final Step step = steps[i];
			final PathFragment fragment = step.getPathFragment();

			if (fragment != null) {
				// Build path fragment and replace current step with it.

				final Path replacement = fragment.expand(tracker, i, prev);

				if (replacement == null) {
					// Error occurred.
					steps = ArrayUtil.replace(
							steps,
							i,
							steps.length,
							new Step[] {ErrorStep.ERROR_STEP});
					this.path = new Path(
							this.path.getKind(),
							this.path.isStatic(),
							steps);
					tracker.abortedAt(prev, step);
					return null;
				}

				final Step[] replacementSteps = replacement.getSteps();

				if (replacement.isAbsolute()) {
					// Replacement is an absolute path.
					// Replace all steps from the very first to the current
					// one.
					steps = ArrayUtil.replace(
							steps,
							0,
							i + 1,
							replacementSteps);
					this.path =
							new Path(PathKind.ABSOLUTE_PATH, true, steps);
					// Continue from the ROOT.
					prev = root(start);
					i = 0;
					tracker.setAbsolute(prev);
				} else {
					// Replacement is a relative path.
					// Replace the current step.
					steps = ArrayUtil.replace(
							steps,
							i,
							i + 1,
							replacementSteps);
					this.path = new Path(
							this.path.getKind(),
							this.path.isStatic() || replacement.isStatic(),
							steps);
				}
				// Do not change the current index.
				continue;
			}
			result = step.resolve(
					tracker.nextResolver(),
					this,
					i,
					prev,
					tracker);
			if (tracker.isAborted()) {
				return NO_PATH_RESOLUTION;
			}
			if (result == null) {
				tracker.abortedAt(prev, step);
				return PATH_RESOLUTION_ERROR;
			}
			++i;
			prev = result.getScope();
		}

		if (!tracker.done(result)) {
			return null;
		}

		return pathResolution(this, result);
	}

	private final Scope root(Scope start) {
		return start.getContext().getRoot().getScope();
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

		final StaticPathStartFinder walker = new StaticPathStartFinder();

		walk(pathResolver(dummyUser()), getOrigin(), walker);

		this.startIndex = walker.getStartIndex();
		this.startObject = walker.getStartObject();
	}

	private Path rebuild() {

		final Path rawPath = getRawPath();
		final Step[] rawSteps = rawPath.getSteps();

		if (rawSteps.length == 0) {
			return rawPath;
		}

		final Step[] steps = removeOddFragments();

		if (steps.length <= 1) {
			return new Path(getKind(), isStatic(), steps);
		}

		final Step[] rebuilt = rebuild(steps);

		if (rebuilt == rawSteps) {
			return rawPath;
		}

		return new Path(getKind(), isStatic(), rebuilt);
	}

	private Step[] removeOddFragments() {

		final OddPathFragmentRemover remover =
				new OddPathFragmentRemover(this);

		walkPath(
				getRawPath(),
				pathResolver(dummyUser()),
				getOrigin(),
				remover,
				true);

		return remover.removeOddFragments();
	}

	private Step[] rebuild(Step[] steps) {
		if (steps.length <= 1) {
			return steps;
		}
		return new PathRebuilder(this, steps).rebuild();
	}

}
