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

import static org.o42a.core.ref.path.Path.EMPTY_STATIC_PATH;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import java.util.ArrayList;
import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.impl.normalizer.UnNormalizedPath;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseCaseInfo;
import org.o42a.util.use.User;


public class PathNormalizer implements UseCaseInfo {

	private final Normalizer normalizer;
	private final Scope origin;
	private final BoundPath path;
	private final ArrayList<NormalStep> normalSteps;

	private Scope stepStart;
	private Scope stepResolution;

	private final int startIndex;
	private int stepIndex;

	private boolean normalizationStarted;
	private boolean stepNormalized;

	PathNormalizer(Normalizer normalizer, Scope origin, BoundPath path) {
		this.normalizer = normalizer;
		this.origin = origin;
		this.path = path;
		this.normalSteps = new ArrayList<NormalStep>(path.length());
		if (!path.isStatic()) {
			this.startIndex = 0;
		} else {
			this.startIndex = path.startIndex();
		}
	}

	public final Normalizer getNormalizer() {
		return this.normalizer;
	}

	public final Scope getOrigin() {
		return this.origin;
	}

	public final Scope getNormalizedStart() {
		return this.normalizer.getNormalizedScope();
	}

	public final BoundPath getPath() {
		return this.path;
	}

	public final int getStepIndex() {
		return this.stepIndex;
	}

	public final Scope getStepStart() {
		return this.stepStart;
	}

	public final boolean isStepIgnored() {
		return this.startIndex > getStepIndex();
	}

	public final boolean isLastStep() {
		return getStepIndex() == getPath().length() - 1;
	}

	public final boolean isStepNormalized() {
		return this.stepNormalized;
	}

	public final void add(Scope resolution, NormalStep normalStep) {
		this.stepResolution = resolution;
		this.normalizationStarted = true;
		this.stepNormalized = true;
		this.normalSteps.add(normalStep);
	}

	public final boolean up(Scope enclosing) {
		if (this.normalizationStarted) {
			return false;
		}

		// Enclosing scope not reached yet.
		// No need to add the enclosing scope step.
		this.stepNormalized = true;
		this.stepResolution = enclosing;

		if (enclosing != getNormalizedStart()) {
			// Enclosing not reached.
			return false;
		}

		this.normalizationStarted = true;

		return true;
	}

	@Override
	public final User<?> toUser() {
		return getNormalizer().toUser();
	}

	@Override
	public final UseCase toUseCase() {
		return getNormalizer().toUseCase();
	}

	@Override
	public String toString() {
		return "PathNormalizer[" + this.path + ']';
	}

	NormalPath normalize() {

		final BoundPath path = getPath();

		if (path.isAbsolute()) {
			this.stepStart = path.root(getOrigin());
		} else {
			this.stepStart = getOrigin();
		}

		final Step[] steps = path.getSteps();

		while (this.stepIndex < steps.length) {
			this.stepResolution = null;
			this.stepNormalized = false;

			steps[this.stepIndex].normalize(this);
			if (!isStepNormalized()) {
				// Normalization failed.
				// Leave the path as is.
				cancelAll(this.normalSteps);
				return new UnNormalizedPath(getNormalizedStart(), path);
			}

			this.stepStart = this.stepResolution;
			++this.stepIndex;
		}

		return new NormalizedPath(
				getNormalizedStart(),
				this.path,
				this.normalSteps);
	}

	private static void cancelAll(Iterable<NormalStep> normalSteps) {
		for (NormalStep step : normalSteps) {
			step.cancel();
		}
	}

	private static class NormalizedPath implements NormalPath {

		private final Scope origin;
		private final BoundPath path;
		private final ArrayList<NormalStep> normalSteps;

		NormalizedPath(
				Scope origin,
				BoundPath path,
				ArrayList<NormalStep> normalSteps) {
			this.origin = origin;
			this.path = path;
			this.normalSteps = normalSteps;
		}

		@Override
		public final boolean isNormalized() {
			return true;
		}

		@Override
		public final Scope getOrigin() {
			return this.origin;
		}

		@Override
		public BoundPath toPath() {

			Path result;

			if (this.path.isAbsolute()) {
				result = ROOT_PATH;
			} else if (!this.path.isStatic()) {
				result = SELF_PATH;
			} else {
				result = EMPTY_STATIC_PATH;
			}

			for (NormalStep normalStep : this.normalSteps) {
				result = normalStep.appendTo(result);
			}

			return result.bind(this.path, getOrigin());
		}

		@Override
		public void cancel() {
			cancelAll(this.normalSteps);
		}

		@Override
		public String toString() {
			if (this.normalSteps == null) {
				return super.toString();
			}

			final StringBuilder out = new StringBuilder();

			out.append("NormalPath<");
			if (this.path.isAbsolute()) {
				out.append('/');
			}

			final Iterator<NormalStep> steps = this.normalSteps.iterator();

			out.append(steps.next());
			while (steps.hasNext()) {
				out.append('/').append(steps.next());
			}
			out.append('>');

			return out.toString();
		}

	}

}
