/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ref.Prediction.scopePrediction;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.impl.normalizer.*;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseCaseInfo;
import org.o42a.util.use.User;


public final class PathNormalizer implements UseCaseInfo {

	static PathNormalizer pathNormalizer(
			Normalizer normalizer,
			Scope origin,
			BoundPath path) {

		final PathNormalizer pathNormalizer =
				new PathNormalizer(normalizer, scopePrediction(origin), path);

		if (!pathNormalizer.init()) {
			return null;
		}

		return pathNormalizer;
	}

	private final PathNormalizer parent;
	private final Data data;
	private final Prediction origin;
	private final BoundPath path;

	private final ArrayList<NormalStep> normalSteps;
	private int firstNonIgnored = -1;
	private boolean overrideNonIgnored;

	private Prediction stepStart;
	private Prediction stepPrediction;
	private int stepIndex;
	private boolean stepNormalized;

	private PathNormalizer(
			Normalizer normalizer,
			Prediction origin,
			BoundPath path) {
		this.parent = null;
		this.data = new Data(normalizer, path);
		this.origin = origin;
		this.path = path;
		this.normalSteps = new ArrayList<NormalStep>(path.length());
	}

	private PathNormalizer(PathNormalizer parent, BoundPath path) {
		this.parent = parent;
		this.data = parent.data;
		this.data.append(path);
		this.origin = parent.getStepStart();
		this.path = path;
		this.normalSteps = new ArrayList<NormalStep>(path.length());
	}

	public final Normalizer getNormalizer() {
		return this.data.normalizer;
	}

	public final Prediction getOrigin() {
		return this.origin;
	}

	public final Scope getNormalizedStart() {
		return this.data.normalizer.getNormalizedScope();
	}

	public final BoundPath getPath() {
		return this.path;
	}

	public final boolean isAbsolute() {
		return this.data.isAbsolute;
	}

	public final boolean isStatic() {
		return this.data.isStatic;
	}

	public final int getStepIndex() {
		return this.stepIndex;
	}

	public final Prediction getStepStart() {
		return this.stepStart;
	}

	public final boolean isNormalizationStarted() {
		return this.data.normalizationStarted;
	}

	public final boolean isNormalizationFinished() {
		return this.data.normalizationFinished;
	}

	public final boolean isLastStep() {
		if (this.parent != null && !this.parent.isLastStep()) {
			return false;
		}
		return getStepIndex() + 1 >= getPath().length();
	}

	public final boolean isStepNormalized() {
		return this.stepNormalized;
	}

	public final void add(Prediction prediction, NormalStep normalStep) {
		this.stepPrediction = prediction;
		this.stepNormalized = true;
		this.normalSteps.add(normalStep);
	}

	public final void inline(Prediction prediction, NormalStep normalStep) {
		add(prediction, normalStep);
		dontIgnore();
		if (isLastStep()) {
			this.data.normalizationFinished = true;
		}
	}

	public final void addStep() {
		add(
				getStepStart(),
				new SameNormalStep(getPath().getSteps()[getStepIndex()]));
	}

	public final void inlineStep() {
		inline(
				getStepStart(),
				new SameNormalStep(getPath().getSteps()[getStepIndex()]));
	}

	public final boolean up(Scope enclosing) {
		if (isNormalizationStarted()) {
			add(
					scopePrediction(enclosing),
					new PathRemainderNormalStep(
							getPath().getPath(),
							getStepIndex()));
			overrideNonIgnored();
			this.data.normalizationFinished = true;
			return false;
		}

		// Enclosing scope not reached yet.
		// No need to add the enclosing scope step.
		if (!upTo(enclosing)) {
			// Enclosing scope not inside normalized path start.
			cancel();
			return false;
		}

		this.stepNormalized = true;
		this.stepPrediction = scopePrediction(enclosing);

		return true;
	}

	public void append(BoundPath path) {

		final PathNormalizer normalizer = new PathNormalizer(this, path);
		final NormalPath normalPath = normalizer.normalize();

		if (!normalPath.isNormalized()) {
			cancel();
			return;
		}

		if (normalizer.firstNonIgnored >= 0) {

			final int index =
					this.normalSteps.size() + normalizer.firstNonIgnored;

			if (normalizer.overrideNonIgnored) {
				overrideNonIgnored(index);
			} else {
				dontIgnore(index);
			}
		}

		normalPath.appendTo(this.normalSteps);

		this.stepNormalized = normalizer.stepNormalized;
		this.stepPrediction = normalizer.stepPrediction;
		if (normalizer.isNormalizationFinished()) {

			final int nextStep = getStepIndex() + 1;

			if (nextStep < getPath().length()) {
				this.normalSteps.add(new PathRemainderNormalStep(
						getPath().getPath(),
						nextStep));
			}
		}
	}

	public final void cancel() {
		this.stepNormalized = false;
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
			this.firstNonIgnored = 0;
			this.data.normalizationFinished = true;

			return new UnchangedNormalPath(
					path.getPath().bind(path, getNormalizedStart()));
		}

		this.stepStart = getOrigin();

		final Step[] steps = path.getSteps();

		while (this.stepIndex < steps.length) {
			this.stepPrediction = null;
			this.stepNormalized = false;

			steps[this.stepIndex].normalize(this);
			if (isNormalizationFinished()) {
				return new NormalizedPath(
						getNormalizedStart(),
						this.path,
						this.normalSteps,
						this.firstNonIgnored,
						isAbsolute(),
						isStatic());
			}
			if (!isStepNormalized()) {
				// Normalization failed.
				// Leave the path as is.
				cancelAll(this.normalSteps);
				return new UnNormalizedPath(path);
			}

			this.stepStart = this.stepPrediction;
			++this.stepIndex;
		}

		if (!isNormalizationStarted() && this.parent == null) {
			return new UnNormalizedPath(path);
		}

		return new NormalizedPath(
				getNormalizedStart(),
				this.path,
				this.normalSteps,
				this.firstNonIgnored,
				isAbsolute(),
				isStatic());
	}

	private boolean init() {
		if (upTo(getOrigin().getScope())) {
			return true;
		}
		if (!getOrigin().getScope().contains(getNormalizedStart())) {
			return false;
		}

		this.data.normalizationStarted = true;

		return true;
	}

	private boolean upTo(Scope scope) {

		final Scope normalizedStart = getNormalizedStart();

		if (scope == normalizedStart) {
			this.data.normalizationStarted = true;
			return true;
		}
		if (normalizedStart.contains(scope)) {
			return true;
		}

		return false;
	}

	private final void dontIgnore() {
		dontIgnore(this.normalSteps.size() - 1);
	}

	private final void dontIgnore(int index) {
		if (this.firstNonIgnored < 0) {
			this.firstNonIgnored = index;
		}
	}

	private final void overrideNonIgnored() {
		overrideNonIgnored(this.normalSteps.size() - 1);
	}

	private final void overrideNonIgnored(int firstNonIgnored) {
		this.firstNonIgnored = firstNonIgnored;
		this.overrideNonIgnored = true;
	}

	private static void cancelAll(Iterable<NormalStep> normalSteps) {
		for (NormalStep step : normalSteps) {
			step.cancel();
		}
	}

	private static final class Data {

		private final Normalizer normalizer;
		private boolean normalizationStarted;
		private boolean normalizationFinished;
		private boolean isAbsolute;
		private boolean isStatic;

		Data(Normalizer normalizer, BoundPath path) {
			this.normalizer = normalizer;
			this.isAbsolute = path.isAbsolute();
			this.isStatic = path.isStatic();
		}

		final void append(BoundPath path) {
			this.isAbsolute |= path.isAbsolute();
			this.isStatic |= path.isStatic();
		}

	}

	private static final class NormalizedPath implements NormalPath {

		private final Scope origin;
		private final BoundPath path;
		private final ArrayList<NormalStep> normalSteps;
		private final int firstNonIgnored;
		private final boolean isAbsolute;
		private final boolean isStatic;

		NormalizedPath(
				Scope origin,
				BoundPath path,
				ArrayList<NormalStep> normalSteps,
				int firstNonIgnored,
				boolean isAbsolute,
				boolean isStatic) {
			this.origin = origin;
			this.path = path;
			this.normalSteps = normalSteps;
			this.firstNonIgnored = firstNonIgnored;
			this.isAbsolute = isAbsolute;
			this.isStatic = isStatic;
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

			if (this.isAbsolute) {
				result = ROOT_PATH;
			} else {
				result = SELF_PATH;
			}

			int index = 0;

			for (NormalStep normalStep : this.normalSteps) {
				if (index < this.firstNonIgnored) {
					normalStep.ignore();
				} else {
					result = normalStep.appendTo(result);
				}
				++index;
			}

			if (!this.isStatic) {
				return result.bind(this.path, getOrigin());
			}

			return result.bindStatically(this.path, getOrigin());
		}

		@Override
		public void cancel() {
			cancelAll(this.normalSteps);
		}

		@Override
		public void appendTo(List<NormalStep> normalSteps) {
			normalSteps.addAll(this.normalSteps);
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
