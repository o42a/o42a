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
import static org.o42a.core.ref.path.PathFragment.MATERIALIZE;
import static org.o42a.core.ref.path.PathReproduction.outOfClausePath;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.util.use.User.dummyUser;

import java.util.Arrays;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Reproducer;
import org.o42a.util.use.UserInfo;


public class Path {

	public static final AbsolutePath ROOT_PATH = new AbsolutePath();

	public static final Path SELF_PATH = new Path(new PathFragment[0]);

	private static final DummyPathWalker DUMMY_WALKER =
		new DummyPathWalker();

	public static AbsolutePath absolutePath(
			CompilerContext context,
			String... fields) {
		return ROOT_PATH.append(context, fields);
	}

	public static AbsolutePath modulePath(String moduleId) {
		return new AbsolutePath(new ModuleFragment(moduleId));
	}

	public static Path memberPath(MemberKey memberKey) {
		return new Path(new MemberFragment(memberKey));
	}

	public static Path materializePath() {
		return new Path(MATERIALIZE);
	}

	private final boolean absolute;
	private final PathFragment[] fragments;

	Path(PathFragment fragment) {
		this.absolute = false;
		this.fragments = new PathFragment[] {fragment};
	}

	private Path(PathFragment[] fragments) {
		this(false, fragments);
	}

	Path(boolean absolute, PathFragment[] fragments) {
		this.fragments = fragments;
		this.absolute = absolute;
	}

	public final boolean isAbsolute() {
		return this.absolute;
	}

	public final boolean isSelf() {
		return this.fragments.length == 0 && !this.absolute;
	}

	public PathFragment[] getFragments() {
		return this.fragments;
	}

	public final Artifact<?> resolveArtifact(
			LocationInfo location,
			UserInfo user,
			Scope start) {
		return walkToArtifact(location, user, start, DUMMY_WALKER);
	}

	public final Artifact<?> resolveArtifactFrom(
			LocationInfo location,
			Resolver resolver,
			Ref start) {
		return walkToArtifactFrom(location, resolver, start, DUMMY_WALKER);
	}

	public Artifact<?> walkToArtifact(
			LocationInfo location,
			UserInfo user,
			Scope start,
			PathWalker walker) {
		return walkPathToArtifact(
				location,
				user,
				this.absolute
				? start.getContext().getRoot().getScope() : start,
				walker);
	}

	public Artifact<?> walkToArtifactFrom(
			LocationInfo location,
			Resolver resolver,
			Ref start,
			PathWalker walker) {
		if (isAbsolute()) {
			return walkPathToArtifact(
					location,
					resolver,
					resolver.getScope().getContext().getRoot().getScope(),
					walker);
		}

		final Resolution resolution = start.resolve(resolver);

		if (resolution.isError()) {
			return null;
		}

		return walkPathToArtifact(
				location,
				resolver,
				resolution.getScope(),
				walker);
	}

	public final Container resolve(
			LocationInfo location,
			UserInfo user,
			Scope start) {
		return walk(location, user, start, DUMMY_WALKER);
	}

	public final Container resolveFrom(
			LocationInfo location,
			Resolver resolver,
			Ref start) {
		return walkFrom(location, resolver, start, DUMMY_WALKER);
	}

	public Container walk(
			LocationInfo location,
			UserInfo user,
			Scope start,
			PathWalker walker) {
		return walkPath(
				location,
				user,
				this.absolute ? start.getContext().getRoot().getScope() : start,
				walker);
	}

	public Container walkFrom(
			LocationInfo location,
			Resolver resolver,
			Ref start,
			PathWalker walker) {
		if (isAbsolute()) {
			return walkPath(
					location,
					resolver,
					resolver.getScope().getContext().getRoot().getScope(),
					walker);
		}

		final Resolution resolution = start.resolve(resolver);

		if (resolution.isError()) {
			return null;
		}

		return walkPath(location, resolver, resolution.getScope(), walker);
	}

	public Path append(PathFragment fragment) {
		if (fragment == null) {
			throw new NullPointerException("Path key not specified");
		}
		if (fragment.isAbsolute()) {
			return new AbsolutePath(fragment);
		}

		final PathFragment[] newKeys =
			new PathFragment[this.fragments.length + 1];

		arraycopy(this.fragments, 0, newKeys, 0, this.fragments.length);
		newKeys[this.fragments.length] = fragment;

		return isAbsolute() ? new AbsolutePath(newKeys) : new Path(newKeys);
	}

	public Path append(MemberKey memberKey) {
		if (memberKey == null) {
			throw new NullPointerException("Field key not specified");
		}
		return append(new MemberFragment(memberKey));
	}

	public Path materialize() {
		return append(MATERIALIZE);
	}

	public Path append(Path path) {
		if (path == null) {
			throw new NullPointerException(
					"Field path to append not specified");
		}
		if (path.isAbsolute()) {
			return path;
		}

		final PathFragment[] fragments = path.fragments;
		final PathFragment[] newFragments =
			new PathFragment[this.fragments.length + fragments.length];

		arraycopy(this.fragments, 0, newFragments, 0, this.fragments.length);
		arraycopy(
				fragments,
				0,
				newFragments,
				this.fragments.length,
				fragments.length);

		if (isAbsolute()) {
			return new AbsolutePath(newFragments);
		}

		return new Path(newFragments);
	}

	public Rescoper rescoper(Scope finalScope) {
		if (!isAbsolute() && getFragments().length == 0) {
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

		if (isAbsolute()) {
			return target(location, distributor);
		}

		return new PathTarget(location, distributor, this, start);
	}

	public Ref target(LocationInfo location, Distributor distributor) {
		return new PathTarget(location, distributor, this);
	}

	public Path rebuild() {

		final PathFragment[] rebuilt = rebuild(this.fragments);

		if (rebuilt == this.fragments) {
			return this;
		}
		if (isAbsolute()) {
			return new AbsolutePath(rebuilt);
		}

		return new Path(rebuilt);
	}

	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer) {

		Scope toScope = reproducer.getScope();
		final int len = this.fragments.length;

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

			final PathFragment fragment = this.fragments[i];
			final PathReproduction reproduction =
				fragment.reproduce(location, reproducer, toScope);

			if (reproduction == null) {
				return null;
			}
			if (reproduction.isUnchanged()) {
				// Left the rest of the path unchanged too.
				return partiallyReproducedPath(
						reproduced.append(reproduction.getExternalPath()),
						i + 1);
			}

			final Path reproducedPath = reproduction.getReproducedPath();
			final Container resolution =
				reproducedPath.resolve(location, dummyUser(), toScope);

			if (resolution == null) {
				return null;
			}

			reproduced = reproduced.append(reproducedPath);

			if (reproduction.isOutOfClause()) {
				return outOfClausePath(
						reproduced,
						reproduction.getExternalPath().append(
								new Path(copyOfRange(
										this.fragments,
										i + 1,
										this.fragments.length))));
			}

			toScope = resolution.getScope();
		}

		return reproducedPath(reproduced);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + (this.absolute ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(this.fragments);

		return result;
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

		if (this.absolute != other.absolute) {
			return false;
		}
		if (!Arrays.equals(this.fragments, other.fragments)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return toString(this.fragments.length);
	}

	public String toString(int length) {
		if (length == 0) {
			return isAbsolute() ? "</>" : "<>";
		}

		final StringBuilder out = new StringBuilder();

		for (int i = 0; i < length; ++i) {

			final PathFragment fragment = this.fragments[i];

			if (i == 0) {
				if (!isAbsolute() || fragment.isAbsolute()) {
					out.append('<');
				} else {
					out.append("</");
				}
			} else {
				out.append('/');
			}

			out.append(fragment);
		}
		out.append('>');

		return out.toString();
	}

	PathTracker startWalk(UserInfo user, Scope start, PathWalker walker) {
		if (!walker.start(this, start)) {
			return null;
		}
		return new PathTracker(user, walker);
	}

	final HostOp write(CodeDirs dirs, HostOp start) {

		HostOp found = start;

		for (int i = 0; i < this.fragments.length; ++i) {
			found = this.fragments[i].write(dirs, found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
	}

	private Artifact<?> walkPathToArtifact(
			LocationInfo location,
			UserInfo user,
			Scope start,
			PathWalker walker) {

		final Container found = walkPath(location, user, start, walker);

		if (found == null) {
			return null;
		}

		final Artifact<?> artifact = found.toArtifact();

		assert artifact != null :
			"Path " + this + " should lead to artifact";

		return artifact;
	}

	private Container walkPath(
			LocationInfo location,
			UserInfo user,
			Scope start,
			PathWalker walker) {

		final PathTracker tracker = startWalk(user, start, walker);

		if (tracker == null) {
			return null;
		}

		Container result = start.getContainer();
		Scope prev = start;

		for (int i = 0; i < this.fragments.length; ++i) {
			result = this.fragments[i].resolve(
					location,
					tracker.nextUser(),
					this,
					i,
					prev,
					tracker);
			if (tracker.isAborted()) {
				return null;
			}
			if (result == null) {
				tracker.abortedAt(prev, this.fragments[i]);
				return null;
			}
			prev = result.getScope();
		}

		if (!tracker.done(result)) {
			return null;
		}

		return result;
	}

	private PathFragment[] rebuild(PathFragment[] fragments) {
		if (fragments.length <= 1) {
			return fragments;
		}

		int stripped = 0;
		int prevIdx = 0;
		int nextIdx = 1;

		for (;;) {

			final PathFragment prev = fragments[prevIdx];
			final PathFragment next = fragments[nextIdx];
			final PathFragment rebuilt = next.rebuild(prev);

			if (rebuilt != null) {
				++stripped;
				fragments[prevIdx] = rebuilt;
				fragments[nextIdx] = null;
				if (++nextIdx >= fragments.length) {
					break;
				}
				continue;
			}

			prevIdx = nextIdx;
			if (++nextIdx >= fragments.length) {
				break;
			}
		}
		if (stripped == 0) {
			return fragments;
		}

		final PathFragment[] result =
			new PathFragment[fragments.length - stripped];
		int idx = 0;

		for (PathFragment fragment : fragments) {
			if (fragment != null) {
				result[idx++] = fragment;
			}
		}

		assert idx == result.length :
			"Wrong path fragments count: " + idx + ", but "
			+ result.length + " expected, when rebuilding " + this;

		return rebuild(result);
	}

	private PathReproduction partiallyReproducedPath(
			Path reproduced,
			int firstUnchangedIdx) {
		if (firstUnchangedIdx == 0) {
			return unchangedPath(this);
		}

		final int fragmentsLeft = this.fragments.length - firstUnchangedIdx;
		final PathFragment[] newFragments = Arrays.copyOf(
				reproduced.fragments,
				reproduced.fragments.length + fragmentsLeft);

		arraycopy(
				this.fragments,
				firstUnchangedIdx,
				newFragments,
				reproduced.fragments.length,
				fragmentsLeft);

		return reproducedPath(new Path(newFragments));
	}

}
