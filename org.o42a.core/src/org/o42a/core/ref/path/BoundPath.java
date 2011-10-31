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
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.*;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStructFinder;
import org.o42a.util.ArrayUtil;


public class BoundPath extends Location {

	private final Scope origin;
	private final Path rawPath;
	private Path path;

	private Obj startObject;
	private int startIndex;

	BoundPath(LocationInfo location, Scope origin, Path rawPath) {
		super(location);
		this.origin = origin;
		this.rawPath = rawPath;
	}

	private BoundPath(LocationInfo location, BoundPath prototype) {
		super(location);
		this.origin = prototype.origin;
		this.rawPath = prototype.rawPath;
		this.path = prototype.path;
	}

	public final Scope getOrigin() {
		return this.origin;
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

	public final boolean isKnownStatic() {
		return getRawPath().isStatic();
	}

	public final boolean isSelf() {
		return getPath().isSelf();
	}

	public final int length() {
		return getSteps().length;
	}

	public final int rawLength() {
		return getRawSteps().length;
	}

	public final Step firstStep() {

		final Step[] steps = getSteps();

		if (steps.length == 0) {
			return null;
		}

		return steps[0];
	}

	public final Step lastStep() {

		final Step[] steps = getSteps();

		if (steps.length == 0) {
			return null;
		}

		return steps[steps.length - 1];
	}

	public final BoundPath setLocation(LocationInfo location) {
		return new BoundPath(location, this);
	}

	public final BoundPath rebuild() {
		getPath();
		return this;
	}

	public TypeRef ancestor(LocationInfo location, Distributor distributor) {

		final Step[] steps = getRawSteps();

		if (steps.length == 0) {
			return new AncestorStep().toPath().bind(
					location,
					distributor.getScope()).typeRef(distributor);
		}

		final Step lastStep = steps[steps.length - 1];

		return lastStep.ancestor(this, location, distributor);
	}

	public BoundPath append(Step step) {
		return getRawPath().append(step).bind(this, getOrigin());
	}

	public final BoundPath append(MemberKey memberKey) {
		return getRawPath().append(memberKey).bind(this, getOrigin());
	}

	public final BoundPath append(PathFragment fragment) {
		return getRawPath().append(fragment).bind(this, getOrigin());
	}

	public final BoundPath materialize() {

		final Path rawPath = getRawPath();
		final Path materialized = rawPath.materialize();

		if (rawPath == materialized) {
			return this;
		}

		return materialized.bind(this, this.origin);
	}

	public final BoundPath arrayItem(Ref indexRef) {
		return getRawPath().arrayItem(indexRef).bind(this, getOrigin());
	}

	public final BoundPath newObject(ObjectConstructor constructor) {
		return getRawPath().newObject(constructor).bind(this, getOrigin());
	}

	public final BoundPath append(Path path) {
		return getRawPath().append(path).bind(this, getOrigin());
	}

	public final BoundPath append(BoundPath path) {
		return append(path.getRawPath());
	}

	public final BoundPath prefixWith(PrefixPath prefix) {
		if (prefix.isEmpty()) {
			return this;
		}
		return prefix.getBoundPath().append(this);
	}

	public final BoundPath cut(int stepsToCut) {
		return getRawPath().cut(stepsToCut).bind(this, this.origin);
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

	public final Ref target(Distributor distributor) {
		return new Ref(this, distributor);
	}

	public final TypeRef typeRef(Distributor distributor) {
		return target(distributor).toTypeRef();
	}

	public final TypeRef typeRef(
			Distributor distributor,
			ValueStructFinder valueStructFinder) {
		return target(distributor).toTypeRef(valueStructFinder);
	}

	public final StaticTypeRef staticTypeRef(Distributor distributor) {
		return target(distributor).toStaticTypeRef();
	}

	public final StaticTypeRef staticTypeRef(
			Distributor distributor,
			ValueStructFinder valueStructFinder) {
		return target(distributor).toStaticTypeRef(valueStructFinder);
	}

	public final FieldDefinition fieldDefinition(Distributor distributor) {

		// It is essential to use the last unresolved step,
		// as it can be an unexpanded path fragment,
		// which field definition can perform additional tasks.
		// One example is phrase.
		final Step[] steps = this.rawPath.getSteps();

		if (steps.length == 0) {
			if (isAbsolute()) {
				return new ObjectFieldDefinition(this, distributor);
			}
			return new PathFieldDefinition(this, distributor);
		}

		final Step lastStep = steps[steps.length - 1];

		return lastStep.fieldDefinition(this, distributor);
	}

	public final FieldDefinition rebuiltFieldDefinition(
			Distributor distributor) {

		final Step[] steps = getSteps();

		if (steps.length == 0) {
			if (isAbsolute()) {
				return new ObjectFieldDefinition(this, distributor);
			}
			return new PathFieldDefinition(this, distributor);
		}

		final Step lastStep = steps[steps.length - 1];

		return lastStep.fieldDefinition(this, distributor);
	}

	public final PathReproduction reproduce(Reproducer reproducer) {
		return getKind().reproduce(reproducer, this);
	}

	public final PrefixPath toPrefix(Scope start) {
		start.assertCompatible(start);
		return getRawPath().toPrefix(start);
	}

	public final BoundPath toStatic() {
		if (getRawPath().isStatic()) {
			return this;
		}
		return getRawPath().bindStatically(this, this.origin);
	}

	public Path toPath() {
		return getPath();
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

	@Override
	public String toString() {
		return toString(getRawSteps().length);
	}

	public String toString(int length) {
		if (this.rawPath == null) {
			return super.toString();
		}
		return getRawPath().toString(this.origin, length);
	}

	final Path getPath() {
		if (this.path != null) {
			return this.path;
		}
		return this.path = rebuildPath();
	}

	final Step[] getSteps() {
		return getPath().getSteps();
	}

	private final Path getRawPath() {
		if (this.path != null) {
			return this.path;
		}
		return this.rawPath;
	}

	private final Step[] getRawSteps() {
		return getRawPath().getSteps();
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
				return NO_PATH_RESOLUTION;
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
				return NO_PATH_RESOLUTION;
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

				final BoundPath replacement =
						fragment.expand(tracker, i, prev);

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
			return NO_PATH_RESOLUTION;
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

	private Path rebuildPath() {

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

		return remover.removeOddFragments(getSteps());
	}

	private Step[] rebuild(Step[] steps) {
		if (steps.length <= 1) {
			return steps;
		}
		return new PathRebuilder(this, steps).rebuild();
	}

	private final PathOp staticOp(CodeDirs dirs) {

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

}
