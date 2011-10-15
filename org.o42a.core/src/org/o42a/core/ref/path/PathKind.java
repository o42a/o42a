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

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.PathReproduction.outOfClausePath;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.util.use.User.dummyUser;

import java.util.Arrays;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.AbsolutePathTarget;
import org.o42a.core.ref.impl.path.PathTarget;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public enum PathKind {

	RELATIVE_PATH() {

		@Override
		protected Ref target(
				LocationInfo location,
				Distributor distributor,
				Path path,
				Ref start) {
			if (start != null && path.isSelf()) {
				return start;
			}
			return new PathTarget(location, distributor, path, start);
		}

		@Override
		protected PathReproduction reproduce(
				Reproducer reproducer,
				BoundPath path) {
			return reproduceRelative(reproducer, path);
		}

	},

	ABSOLUTE_PATH() {

		@Override
		protected Ref target(
				LocationInfo location,
				Distributor distributor,
				Path path,
				Ref start) {
			return new AbsolutePathTarget(location, distributor, path);
		}

		@Override
		protected PathReproduction reproduce(
				Reproducer reproducer,
				BoundPath path) {
			return unchangedPath(path.getPath());
		}

	};

	private final Path emptyPath = new Path(this);

	public final boolean isStatic() {
		return this != RELATIVE_PATH;
	}

	public final Path emptyPath() {
		return this.emptyPath;
	}

	protected abstract Ref target(
			LocationInfo location,
			Distributor distributor,
			Path path,
			Ref start);

	protected abstract PathReproduction reproduce(
			Reproducer reproducer,
			BoundPath path);

	private static PathReproduction reproduceRelative(
			Reproducer reproducer,
			BoundPath path) {

		Scope toScope = reproducer.getScope();
		final Step[] steps = path.getSteps();
		final int len = steps.length;

		if (len == 0) {

			final Clause clause =
					reproducer.getReproducingScope()
					.getContainer()
					.toClause();

			if (clause == null) {
				return outOfClausePath(SELF_PATH, SELF_PATH);
			}

			return unchangedPath(SELF_PATH);
		}

		Path reproduced = SELF_PATH;

		for (int i = 0; i < len; ++i) {

			final Step step = steps[i];
			final PathReproduction reproduction =
					step.reproduce(path, reproducer, toScope);

			if (reproduction == null) {
				return null;
			}
			if (reproduction.isUnchanged()) {
				// Left the rest of the path unchanged too.
				return partiallyReproducedPath(path, reproduced, i);
			}

			final Path reproducedPath = reproduction.getReproducedPath();
			final PathResolution resolution =
					reproducedPath.bind(path, toScope).resolve(
							pathResolver(dummyUser()),
							toScope);

			if (!resolution.isResolved()) {
				return null;
			}

			reproduced = reproduced.append(reproducedPath);

			if (reproduction.isOutOfClause()) {
				return outOfClausePath(
						reproduced,
						reproduction.getExternalPath().append(
								new Path(
										RELATIVE_PATH,
										copyOfRange(
												steps,
												i + 1,
												steps.length))));
			}

			toScope = resolution.getResult().getScope();
		}

		return reproducedPath(reproduced);
	}

	private static PathReproduction partiallyReproducedPath(
			BoundPath path,
			Path reproduced,
			int firstUnchangedIdx) {
		if (firstUnchangedIdx == 0) {
			return unchangedPath(path.getPath());
		}

		final Step[] steps = path.getSteps();
		final Step[] reproducedSteps = reproduced.getSteps();
		final int stepsLeft = steps.length - firstUnchangedIdx;
		final Step[] newSteps = Arrays.copyOf(
				reproducedSteps,
				reproducedSteps.length + stepsLeft);

		arraycopy(
				steps,
				firstUnchangedIdx,
				newSteps,
				reproducedSteps.length,
				stepsLeft);

		return reproducedPath(new Path(RELATIVE_PATH, newSteps));
	}

}
