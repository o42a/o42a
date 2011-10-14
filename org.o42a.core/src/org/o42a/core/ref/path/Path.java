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

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.path.PathKind.ABSOLUTE_PATH;
import static org.o42a.core.ref.path.PathKind.RELATIVE_PATH;
import static org.o42a.core.ref.path.PathResolution.NO_PATH_RESOLUTION;
import static org.o42a.core.ref.path.PathResolution.PATH_RESOLUTION_ERROR;
import static org.o42a.core.ref.path.PathResolution.pathResolution;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.core.ref.path.Step.MATERIALIZE;
import static org.o42a.util.use.User.dummyUser;

import java.util.Arrays;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.impl.ArrayElementStep;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;


public final class Path {

	public static final Path ROOT_PATH = ABSOLUTE_PATH.emptyPath();
	public static final Path SELF_PATH = RELATIVE_PATH.emptyPath();

	public static Path absolutePath(
			CompilerContext context,
			String... fields) {

		Path path = ROOT_PATH;
		Obj object = context.getRoot();

		for (String field : fields) {

			final Member member = object.field(field);

			assert member != null :
				"Field \"" + field + "\" not found in " + object;

			path = path.append(member.getKey());
			object = member.substance(dummyUser()).toObject();
		}

		return path;
	}

	public static Path modulePath(String moduleId) {
		return new Path(ABSOLUTE_PATH, new ModuleStep(moduleId));
	}

	public static Path memberPath(MemberKey memberKey) {
		return new Path(RELATIVE_PATH, new MemberStep(memberKey));
	}

	public static Path materializePath() {
		return new Path(RELATIVE_PATH, MATERIALIZE);
	}

	private final PathKind kind;
	private final Step[] steps;

	private CompilerContext context;
	private Obj startObject;
	private int startIndex;

	Path(PathKind kind, Step... steps) {
		this.kind = kind;
		this.steps = steps;
		assert assertStepsNotNull(steps);
	}

	public final PathKind getKind() {
		return this.kind;
	}

	public final boolean isAbsolute() {
		return getKind() == PathKind.ABSOLUTE_PATH;
	}

	public final boolean isSelf() {
		return this.steps.length == 0 && !isAbsolute();
	}

	public final Step[] getSteps() {
		return this.steps;
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
		return walkPath(
				resolver,
				isAbsolute() ? start.getContext().getRoot().getScope() : start,
				walker);
	}

	public Path append(Step step) {
		assert step != null :
			"Path step not specified";

		final PathKind pathKind = step.getPathKind();

		if (pathKind.isStatic()) {
			return new Path(pathKind, step);
		}

		final Step[] newSteps = ArrayUtil.append(this.steps, step);

		return new Path(getKind(), newSteps);
	}

	public Path append(MemberKey memberKey) {
		assert memberKey != null :
			"Member key not specified";
		return append(new MemberStep(memberKey));
	}

	public Path cutArtifact() {

		final Step[] steps = dematerialize().getSteps();
		final int length = steps.length;

		if (length == 0) {
			return this;
		}

		final int lastIdx = length - 1;
		final Step lastStep = steps[lastIdx];

		if (!lastStep.isArtifact()) {
			return this;
		}
		if (lastIdx == 0) {
			return getKind().emptyPath();
		}

		final Step[] newSteps = Arrays.copyOf(steps, lastIdx);

		return new Path(getKind(), newSteps);
	}

	public Path materialize() {

		final int length = this.steps.length;

		if (length == 0) {
			return this;
		}

		final Step lastStep = this.steps[length - 1];
		final Step materializer = lastStep.materialize();

		if (materializer == null) {
			return this;
		}

		return append(materializer);
	}

	public Path dematerialize() {

		final int length = this.steps.length;

		if (length == 0) {
			return this;
		}

		final int lastIdx = length - 1;
		final Step lastStep = this.steps[lastIdx];

		if (!lastStep.isMaterializer()) {
			return this;
		}
		if (lastIdx == 0) {
			return getKind().emptyPath();
		}

		final Step[] newSteps = Arrays.copyOf(this.steps, lastIdx);

		return new Path(getKind(), newSteps);
	}

	public Path arrayItem(Ref indexRef) {
		return append(new ArrayElementStep(indexRef));
	}

	public Path append(Path path) {
		assert path != null :
			"Path to append not specified";

		if (path.isAbsolute()) {
			return path;
		}

		final Step[] newSteps = ArrayUtil.append(getSteps(), path.getSteps());

		return new Path(getKind(), newSteps);
	}

	public Rescoper rescoper(Scope finalScope) {
		if (!isAbsolute() && getSteps().length == 0) {
			return transparentRescoper(finalScope);
		}
		return new PathRescoper(this, finalScope);
	}

	public Ref target(
			LocationInfo location,
			Distributor distributor,
			Ref start) {
		if (start == null) {
			return target(location, distributor);
		}

		start.assertCompatibleScope(distributor);

		return getKind().target(location, distributor, this, start);
	}

	public Ref target(LocationInfo location, Distributor distributor) {
		return new PathTarget(location, distributor, this);
	}

	public Path rebuild() {

		final Step[] rebuilt = rebuild(this.steps);

		if (rebuilt == this.steps) {
			return this;
		}

		return new Path(getKind(), rebuilt);
	}

	public Path rebuildWithRef(Ref followingRef) {

		final Path path = followingRef.getPath();

		if (path != null) {
			return append(path).rebuild();
		}

		final int length = this.steps.length;

		if (length == 0) {
			return null;
		}

		final int lastIdx = length - 1;
		final Step lastStep = this.steps[lastIdx];
		final Step rebuilt = lastStep.combineWithRef(followingRef);

		if (rebuilt == null) {
			return null;
		}

		final Step[] newSteps = this.steps.clone();

		newSteps[lastIdx] = rebuilt;

		return new Path(getKind(), newSteps);
	}

	public final PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer) {
		return getKind().reproduce(location, reproducer, this);
	}

	public final HostOp write(CodeDirs dirs, HostOp start) {
		if (isAbsolute()) {
			return writeAbolute(dirs);
		}

		HostOp found = start;

		for (int i = 0; i < this.steps.length; ++i) {
			found = this.steps[i].write(dirs, found);
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
	public int hashCode() {
		return Arrays.hashCode(this.steps);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Path other = (Path) obj;

		return Arrays.equals(this.steps, other.steps);
	}

	@Override
	public String toString() {
		return toString(this.steps.length);
	}

	public String toString(int length) {
		if (length == 0) {
			return isAbsolute() ? "</>" : "<>";
		}

		final StringBuilder out = new StringBuilder();

		for (int i = 0; i < length; ++i) {

			final Step step = this.steps[i];

			if (i == 0) {
				if (!isAbsolute() || step.getPathKind().isStatic()) {
					out.append('<');
				} else {
					out.append("</");
				}
			} else {
				out.append('/');
			}

			out.append(step);
		}
		out.append('>');

		return out.toString();
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

	private static boolean assertStepsNotNull(Step[] steps) {
		for (Step step : steps) {
			assert step != null :
				"Path step is null";
		}
		return true;
	}

	private PathResolution walkPath(
			PathResolver resolver,
			Scope start,
			PathWalker walker) {

		final PathTracker tracker = startWalk(resolver, start, walker);

		if (tracker == null) {
			return null;
		}

		Container result = start.getContainer();
		Scope prev = start;

		for (int i = 0; i < this.steps.length; ++i) {
			result = this.steps[i].resolve(
					tracker.nextResolver(),
					this,
					i,
					prev,
					tracker);
			if (tracker.isAborted()) {
				return NO_PATH_RESOLUTION;
			}
			if (result == null) {
				tracker.abortedAt(prev, this.steps[i]);
				return PATH_RESOLUTION_ERROR;
			}
			prev = result.getScope();
		}

		if (!tracker.done(result)) {
			return null;
		}

		return pathResolution(this, result);
	}

	private Step[] rebuild(Step[] steps) {
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
