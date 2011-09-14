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
import static org.o42a.core.ref.path.PathResolution.NO_PATH_RESOLUTION;
import static org.o42a.core.ref.path.PathResolution.PATH_RESOLUTION_ERROR;
import static org.o42a.core.ref.path.PathResolution.pathResolution;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.util.use.User.dummyUser;

import java.util.Arrays;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;


public class Path {

	public static final AbsolutePath ROOT_PATH = new AbsolutePath();

	public static final Path SELF_PATH = new Path(new PathFragment[0]);

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

	Path(boolean absolute, PathFragment... fragments) {
		this.absolute = absolute;
		this.fragments = fragments;
		assert assertFragmentsNotNull(fragments);
	}

	Path(PathFragment... fragments) {
		this.absolute = false;
		this.fragments = fragments;
		assert assertFragmentsNotNull(fragments);
	}

	public final boolean isAbsolute() {
		return this.absolute;
	}

	public final boolean isSelf() {
		return this.fragments.length == 0 && !isAbsolute();
	}

	public final PathFragment[] getFragments() {
		return this.fragments;
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

	public Path append(PathFragment fragment) {
		if (fragment == null) {
			throw new NullPointerException("Path key not specified");
		}
		if (fragment.isAbsolute()) {
			return new AbsolutePath(fragment);
		}

		final PathFragment[] newFragments =
				ArrayUtil.append(this.fragments, fragment);

		if (!isAbsolute()) {
			return new Path(newFragments);
		}

		return new AbsolutePath(newFragments);
	}

	public Path append(MemberKey memberKey) {
		if (memberKey == null) {
			throw new NullPointerException("Field key not specified");
		}
		return append(new MemberFragment(memberKey));
	}

	public Path materialize() {

		final int length = this.fragments.length;

		if (length == 0) {
			return this;
		}

		final PathFragment lastFragment = this.fragments[length - 1];
		final PathFragment materializer = lastFragment.materialize();

		if (materializer == null) {
			return this;
		}

		return append(materializer);
	}

	public Path append(Path path) {
		assert path != null :
			"Path to append not specified";

		if (path.isAbsolute()) {
			return path;
		}

		final PathFragment[] newFragments =
				ArrayUtil.append(this.fragments, path.fragments);

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

		if (isSelf()) {
			return start;
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

	public Path rebuildWithRef(Ref followingRef) {

		final Path path = followingRef.getPath();

		if (path != null) {
			return append(path).rebuild();
		}

		final int length = this.fragments.length;

		if (length == 0) {
			return null;
		}

		final int lastIdx = length - 1;
		final PathFragment lastFragment = this.fragments[lastIdx];
		final PathFragment rebuilt = lastFragment.combineWithRef(followingRef);

		if (rebuilt == null) {
			return null;
		}

		final PathFragment[] newFragments = this.fragments.clone();

		newFragments[lastIdx] = rebuilt;

		if (isAbsolute()) {
			return new AbsolutePath(newFragments);
		}

		return new Path(newFragments);
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
										this.fragments,
										i + 1,
										this.fragments.length))));
			}

			toScope = resolution.getResult().getScope();
		}

		return reproducedPath(reproduced);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.fragments);
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

		return Arrays.equals(this.fragments, other.fragments);
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

	PathTracker startWalk(
			PathResolver resolver,
			Scope start,
			PathWalker walker) {
		if (!walker.start(this, start)) {
			return null;
		}
		return new PathTracker(resolver, walker);
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

	private static boolean assertFragmentsNotNull(PathFragment[] fragments) {
		for (PathFragment fragment : fragments) {
			assert fragment != null :
				"Path fragment is null";
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

		for (int i = 0; i < this.fragments.length; ++i) {
			result = this.fragments[i].resolve(
					tracker.nextResolver(),
					this,
					i,
					prev,
					tracker);
			if (tracker.isAborted()) {
				return NO_PATH_RESOLUTION;
			}
			if (result == null) {
				tracker.abortedAt(prev, this.fragments[i]);
				return PATH_RESOLUTION_ERROR;
			}
			prev = result.getScope();
		}

		if (!tracker.done(result)) {
			return null;
		}

		return pathResolution(this, result);
	}

	private PathFragment[] rebuild(PathFragment[] fragments) {
		if (fragments.length <= 1) {
			return fragments;
		}

		final PathFragment[] rebuiltFragments =
				new PathFragment[fragments.length];
		PathFragment prev = rebuiltFragments[0] = fragments[0];
		int nextIdx = 1;
		int rebuiltIdx = 0;

		for (;;) {

			final PathFragment next = fragments[nextIdx];
			final PathFragment rebuilt = next.rebuild(prev);

			if (rebuilt != null) {
				rebuiltFragments[rebuiltIdx] = prev = rebuilt;
				if (++nextIdx >= fragments.length) {
					break;
				}
				continue;
			}

			rebuiltFragments[++rebuiltIdx] = prev = next;
			if (++nextIdx >= fragments.length) {
				break;
			}
		}

		final int rebuiltLen = rebuiltIdx + 1;

		if (rebuiltLen == fragments.length) {
			return fragments;
		}

		return rebuild(ArrayUtil.clip(rebuiltFragments, rebuiltLen));
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
