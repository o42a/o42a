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

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;
import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.path.PathReproduction.outOfClausePath;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
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
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;


public class Path {

	public static final AbsolutePath ROOT_PATH = new AbsolutePath();

	public static final Path SELF_PATH = new Path(new Step[0]);

	public static AbsolutePath absolutePath(
			CompilerContext context,
			String... fields) {

		AbsolutePath path = ROOT_PATH;
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

	public static AbsolutePath modulePath(String moduleId) {
		return new AbsolutePath(new ModuleStep(moduleId));
	}

	public static Path memberPath(MemberKey memberKey) {
		return new Path(new MemberStep(memberKey));
	}

	public static Path materializePath() {
		return new Path(MATERIALIZE);
	}

	private final boolean absolute;
	private final Step[] steps;

	Path(boolean absolute, Step... steps) {
		this.absolute = absolute;
		this.steps = steps;
		assert assertStepsNotNull(steps);
	}

	Path(Step... steps) {
		this.absolute = false;
		this.steps = steps;
		assert assertStepsNotNull(steps);
	}

	public final boolean isAbsolute() {
		return this.absolute;
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
		if (step.isAbsolute()) {
			return new AbsolutePath(step);
		}

		final Step[] newSteps =
				ArrayUtil.append(this.steps, step);

		if (!isAbsolute()) {
			return new Path(newSteps);
		}

		return new AbsolutePath(newSteps);
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
			if (!isAbsolute()) {
				return SELF_PATH;
			}
			return ROOT_PATH;
		}

		final Step[] newSteps = Arrays.copyOf(steps, lastIdx);

		if (!isAbsolute()) {
			return new Path(newSteps);
		}

		return new AbsolutePath(newSteps);
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
			if (!isAbsolute()) {
				return SELF_PATH;
			}
			return ROOT_PATH;
		}

		final Step[] newSteps = Arrays.copyOf(this.steps, lastIdx);

		if (!isAbsolute()) {
			return new Path(newSteps);
		}

		return new AbsolutePath(newSteps);
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

		if (isAbsolute()) {
			return new AbsolutePath(newSteps);
		}

		return new Path(newSteps);
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

		if (isSelf()) {
			return start;
		}

		return new PathTarget(location, distributor, this, start);
	}

	public Ref target(LocationInfo location, Distributor distributor) {
		return new PathTarget(location, distributor, this);
	}

	public Path rebuild() {

		final Step[] rebuilt = rebuild(this.steps);

		if (rebuilt == this.steps) {
			return this;
		}
		if (isAbsolute()) {
			return new AbsolutePath(rebuilt);
		}

		return new Path(rebuilt);
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

		if (isAbsolute()) {
			return new AbsolutePath(newSteps);
		}

		return new Path(newSteps);
	}

	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer) {

		Scope toScope = reproducer.getScope();
		final int len = this.steps.length;

		if (len == 0) {

			final Clause clause =
					reproducer.getReproducingScope().getContainer().toClause();

			if (clause == null) {
				return outOfClausePath(SELF_PATH, SELF_PATH);
			}

			return unchangedPath(SELF_PATH);
		}

		Path reproduced = SELF_PATH;

		for (int i = 0; i < len; ++i) {

			final Step step = this.steps[i];
			final PathReproduction reproduction =
					step.reproduce(location, reproducer, toScope);

			if (reproduction == null) {
				return null;
			}
			if (reproduction.isUnchanged()) {
				// Left the rest of the path unchanged too.
				return partiallyReproducedPath(reproduced, i);
			}

			final Path reproducedPath = reproduction.getReproducedPath();
			final PathResolution resolution = reproducedPath.resolve(
					pathResolver(location, dummyUser()),
					toScope);

			if (!resolution.isResolved()) {
				return null;
			}

			reproduced = reproduced.append(reproducedPath);

			if (reproduction.isOutOfClause()) {
				return outOfClausePath(
						reproduced,
						reproduction.getExternalPath().append(
								new Path(copyOfRange(
										this.steps,
										i + 1,
										this.steps.length))));
			}

			toScope = resolution.getResult().getScope();
		}

		return reproducedPath(reproduced);
	}

	public final HostOp write(CodeDirs dirs, HostOp start) {

		HostOp found = start;

		for (int i = 0; i < this.steps.length; ++i) {
			found = this.steps[i].write(dirs, found);
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
				if (!isAbsolute() || step.isAbsolute()) {
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

	PathTracker startWalk(
			PathResolver resolver,
			Scope start,
			PathWalker walker) {
		if (!walker.start(this, start)) {
			return null;
		}
		return new PathTracker(resolver, walker);
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

	private PathReproduction partiallyReproducedPath(
			Path reproduced,
			int firstUnchangedIdx) {
		if (firstUnchangedIdx == 0) {
			return unchangedPath(this);
		}

		final int stepsLeft = this.steps.length - firstUnchangedIdx;
		final Step[] newSteps = Arrays.copyOf(
				reproduced.steps,
				reproduced.steps.length + stepsLeft);

		arraycopy(
				this.steps,
				firstUnchangedIdx,
				newSteps,
				reproduced.steps.length,
				stepsLeft);

		return reproducedPath(new Path(newSteps));
	}

}
