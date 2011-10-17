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

import static org.o42a.core.ref.path.PathKind.ABSOLUTE_PATH;
import static org.o42a.core.ref.path.PathKind.RELATIVE_PATH;
import static org.o42a.core.ref.path.Step.MATERIALIZE;
import static org.o42a.util.use.User.dummyUser;

import java.util.Arrays;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.impl.ArrayElementStep;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.ModuleStep;
import org.o42a.core.ref.impl.path.PathTarget;
import org.o42a.core.ref.impl.path.StaticStep;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.ArrayUtil;
import org.o42a.util.Deferred;


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
		return new Path(ABSOLUTE_PATH, true, new ModuleStep(moduleId));
	}

	public static Path memberPath(MemberKey memberKey) {
		return new Path(RELATIVE_PATH, false, new MemberStep(memberKey));
	}

	public static Path materializePath() {
		return new Path(RELATIVE_PATH, false, MATERIALIZE);
	}

	private final PathKind kind;
	private final Step[] steps;
	private final boolean isStatic;

	Path(PathKind kind, boolean isStatic, Step... steps) {
		this.kind = kind;
		this.isStatic = kind.isAbsolute() ? true : isStatic;
		this.steps = steps;
		assert assertStepsNotNull(steps);
	}

	public final PathKind getKind() {
		return this.kind;
	}

	public final boolean isAbsolute() {
		return getKind().isAbsolute();
	}

	public final boolean isStatic() {
		return this.isStatic;
	}

	public final boolean isSelf() {
		return this.steps.length == 0 && !isStatic();
	}

	public final Step[] getSteps() {
		return this.steps;
	}

	public Path append(Step step) {
		assert step != null :
			"Path step not specified";

		final PathKind pathKind = step.getPathKind();

		if (pathKind.isAbsolute()) {
			return new Path(pathKind, true, step);
		}

		final Step[] newSteps = ArrayUtil.append(this.steps, step);

		return new Path(getKind(), isStatic(), newSteps);
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

		if (!lastStep.getStepKind().isArtifact()) {
			return this;
		}
		if (lastIdx == 0) {
			return getKind().emptyPath();
		}

		final Step[] newSteps = Arrays.copyOf(steps, lastIdx);

		return new Path(getKind(), isStatic(), newSteps);
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

		if (!lastStep.getStepKind().isMaterializer()) {
			return this;
		}
		if (lastIdx == 0) {
			return getKind().emptyPath();
		}

		final Step[] newSteps = Arrays.copyOf(this.steps, lastIdx);

		return new Path(getKind(), isStatic(), newSteps);
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

		return new Path(getKind(), isStatic() || path.isStatic(), newSteps);
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
		return new PathTarget(location, distributor, this, null);
	}

	public final BoundPath bind(LocationInfo location, Scope origin) {
		return new BoundPath(location, origin, this);
	}

	public final BoundPath bind(LocationInfo location, Deferred<Scope> origin) {
		return new BoundPath(location, origin, this);
	}

	public final BoundPath bindStatically(LocationInfo location, Scope origin) {
		if (isStatic()) {
			return bind(location, origin);
		}

		final Step[] steps =
				ArrayUtil.prepend(new StaticStep(origin), getSteps());

		return new Path(getKind(), true, steps).bind(location, origin);
	}

	public final BoundPath bindStatically(
			LocationInfo location,
			Deferred<Scope> origin) {
		if (isStatic()) {
			return bind(location, origin);
		}

		final Step[] steps =
				ArrayUtil.prepend(new StaticStep(origin), getSteps());

		return new Path(getKind(), true, steps).bind(location, origin);
	}

	public Path rebuildWithRef(Ref followingRef) {

		final Path path = followingRef.getPath();

		if (path != null) {
			return append(path);
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

		return new Path(getKind(), isStatic(), newSteps);
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
		return toString(null, length);
	}

	String toString(Object origin, int length) {

		final StringBuilder out = new StringBuilder();

		if (isAbsolute()) {
			out.append("</");
		} else {
			out.append('<');
		}
		if (origin != null) {
			out.append('[').append(origin).append("] ");
		}

		for (int i = 0; i < length; ++i) {

			final Step step = this.steps[i];

			if (i != 0) {
				out.append('/');
			}

			out.append(step);
		}
		out.append('>');

		return out.toString();
	}

	private static boolean assertStepsNotNull(Step[] steps) {
		for (Step step : steps) {
			assert step != null :
				"Path step is null";
		}
		return true;
	}

}
