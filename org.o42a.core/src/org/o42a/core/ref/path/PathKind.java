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
import static org.o42a.core.member.MemberRegistry.noDeclarations;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.PathReproduction.outOfClausePath;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.util.use.User.dummyUser;

import java.util.Arrays;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.AbsolutePathTarget;
import org.o42a.core.ref.impl.path.PathTarget;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.Statements;


public enum PathKind {

	RELATIVE_PATH(false) {

		@Override
		protected Ref target(
				LocationInfo location,
				Distributor distributor,
				Path path) {
			return new PathTarget(
					location,
					distributor,
					path.bind(location, distributor.getScope()));
		}

		@Override
		protected PathReproduction reproduce(
				Reproducer reproducer,
				BoundPath path) {
			return reproduceRelative(reproducer, path);
		}

	},

	ABSOLUTE_PATH(true) {

		@Override
		protected Ref target(
				LocationInfo location,
				Distributor distributor,
				Path path) {
			return new AbsolutePathTarget(location, distributor, path);
		}

		@Override
		protected PathReproduction reproduce(
				Reproducer reproducer,
				BoundPath path) {
			return unchangedPath(path.getPath());
		}

	};

	private final Path emptyPath;

	PathKind(boolean isStatic) {
		this.emptyPath = new Path(this, isStatic);
	}

	public final boolean isAbsolute() {
		return this == ABSOLUTE_PATH;
	}

	public final Path emptyPath() {
		return this.emptyPath;
	}

	protected abstract Ref target(
			LocationInfo location,
			Distributor distributor,
			Path path);

	protected abstract PathReproduction reproduce(
			Reproducer reproducer,
			BoundPath path);

	private static PathReproduction reproduceRelative(
			Reproducer reproducer,
			BoundPath path) {

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

		final PathResolver resolver = pathResolver(dummyUser());
		Scope fromScope = reproducer.getReproducingScope();
		Scope toScope = reproducer.getScope();
		Path reproduced = SELF_PATH;

		for (int i = 0; i < len; ++i) {

			final Step step = steps[i];
			final PathReproduction reproduction = step.reproduce(
					path,
					new StepReproducer(reproducer, fromScope, toScope));

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
										path.isStatic(),
										copyOfRange(
												steps,
												i + 1,
												steps.length))));
			}

			toScope = resolution.getResult().getScope();

			final Container resolvedStep = step.resolve(
					resolver,
					path,
					i,
					fromScope,
					DUMMY_PATH_WALKER);

			if (resolvedStep == null) {
				return null;
			}

			fromScope = resolvedStep.getScope();
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

		return reproducedPath(
				new Path(RELATIVE_PATH, path.isStatic(), newSteps));
	}

	private static final class StepReproducer extends Reproducer {

		private final Reproducer reproducer;

		StepReproducer(
				Reproducer reproducer,
				Scope reproducingScope,
				Scope scope) {
			super(
					reproducingScope,
					reproducer.distribute().distributeIn(
							scope.getContainer()));
			this.reproducer = reproducer;
		}

		@Override
		public Ref getPhrasePrefix() {
			return this.reproducer.getPhrasePrefix();
		}

		@Override
		public boolean phraseCreatesObject() {
			return this.reproducer.phraseCreatesObject();
		}

		@Override
		public MemberRegistry getMemberRegistry() {
			return noDeclarations();
		}

		@Override
		public Statements<?> getStatements() {
			return null;
		}

		@Override
		public Reproducer reproducerOf(Scope reproducingScope) {
			if (reproducingScope == getReproducingScope()) {
				return this;
			}
			return null;
		}

		@Override
		public void applyClause(
				LocationInfo location,
				Statements<?> statements,
				Clause clause) {
			throw new UnsupportedOperationException();
		}

	}

}
