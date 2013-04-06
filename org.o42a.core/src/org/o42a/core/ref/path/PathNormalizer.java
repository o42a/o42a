/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.Prediction.startPrediction;
import static org.o42a.core.ref.impl.prediction.EnclosingPrediction.enclosingPrediction;

import java.util.ArrayList;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Scope;
import org.o42a.core.ref.*;
import org.o42a.core.ref.impl.normalizer.*;
import org.o42a.core.ref.impl.prediction.InitialPrediction;
import org.o42a.util.fn.Cancelable;


public final class PathNormalizer {

	static PathNormalizer pathNormalizer(
			Normalizer normalizer,
			Scope origin,
			BoundPath path) {

		final PathNormalizer pathNormalizer =
				new PathNormalizer(normalizer, startPrediction(origin), path);

		if (!pathNormalizer.init()) {
			path.doubt(normalizer.getAnalyzer()).abortNormalization();
			normalizer.cancelAll();
			return null;
		}

		return pathNormalizer;
	}

	private final Normalizer parentNormalizer;
	private final Normalizer normalizer;
	private final PathNormalizer parent;
	private final BoundPath path;
	private final NestedNormalizer nested;
	private final Data data;
	private final Prediction origin;

	private NormalizedSteps normalizedSteps;
	private int firstNonIgnored = -1;
	private boolean overrideNonIgnored;
	private boolean staticNormalization;

	private Prediction stepStart;
	private Prediction stepPrediction;
	private Prediction nextPrediction;
	private int stepIndex;
	private boolean stepNormalized;

	private PathNormalizer(
			Normalizer parentNormalizer,
			Prediction origin,
			BoundPath path) {
		this.parentNormalizer = parentNormalizer;
		this.normalizer = parentNormalizer.createNested();
		this.parent = null;
		this.path = path;
		this.nested = null;
		this.data = new Data(path);
		this.origin = origin;
	}

	private PathNormalizer(
			PathNormalizer parent,
			BoundPath path,
			NestedNormalizer nested) {
		this.parentNormalizer = parent.normalizer;
		this.normalizer = this.parentNormalizer.createNested();
		this.parent = parent;
		this.path = path;
		this.nested = nested;
		this.data = parent.data;
		this.data.append(path);
		this.origin = parent.lastPrediction();
	}

	public final Analyzer getAnalyzer() {
		return getNormalizer().getAnalyzer();
	}

	public final Normalizer getNormalizer() {
		return this.normalizer;
	}

	public final boolean isNested() {
		return this.parent != null;
	}

	public final NestedNormalizer getNested() {
		return this.nested;
	}

	public final Prediction getOrigin() {
		return this.origin;
	}

	public final Scope getNormalizedStart() {
		return getNormalizer().getNormalizedScope();
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

	public final Prediction stepStart() {
		return this.stepStart;
	}

	public final Prediction lastPrediction() {
		if (this.stepPrediction == null) {
			return this.stepStart;
		}
		return this.stepPrediction;
	}

	public final Prediction nextPrediction() {
		return this.nextPrediction;
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

	public final void skip(Prediction prediction, NormalAppender normalStep) {
		add(prediction, normalStep);
	}

	public final void inline(Prediction prediction, InlineStep normalStep) {
		add(prediction, normalStep);
		dontIgnore();
		if (isLastStep()) {
			this.data.normalizationFinished = true;
		}
	}

	public final void skipStep() {
		add(
				lastPrediction(),
				new SameNormalStep(getPath().getSteps()[getStepIndex()]));
	}

	public final void skipToNext(Prediction prediction) {
		this.stepPrediction = lastPrediction();
		this.nextPrediction = prediction;
		this.stepNormalized = true;
	}

	public final boolean up(
			Scope enclosing,
			Path enclosingPath,
			ReversePath revertPath) {
		if (isNormalizationStarted()) {

			final Prediction lastPrediction = lastPrediction();

			this.normalizedSteps.addNormalStep(
					new NormalPathStep(enclosingPath));
			overrideNonIgnored();

			this.stepPrediction = enclosingPrediction(
					lastPrediction,
					enclosing,
					enclosingPath,
					revertPath);
			this.nextPrediction = null;

			final Step step = getPath().getSteps()[getStepIndex()];
			final Path nonNormalizedRemainder =
					step.nonNormalizedRemainder(this);

			if (!nonNormalizedRemainder.isSelf()) {
				this.normalizedSteps.addNormalStep(
						new NormalPathStep(nonNormalizedRemainder));
			}
			addRest(getStepIndex() + 1);
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

		final Prediction lastPrediction = lastPrediction();

		this.stepNormalized = true;
		this.stepPrediction = enclosingPrediction(
				lastPrediction,
				enclosing,
				enclosingPath,
				revertPath);
		this.nextPrediction = null;

		return true;
	}

	public void append(BoundPath path, NestedNormalizer nested) {

		final PathNormalizer normalizer =
				new PathNormalizer(this, path, nested);
		final NormalPath normalPath = normalizer.normalize();

		if (!normalPath.isNormalized()) {
			this.normalizer.cancelAll();
			cancel();
			return;
		}

		if (normalizer.firstNonIgnored >= 0) {

			final int index =
					this.normalizedSteps.size() + normalizer.firstNonIgnored;

			if (normalizer.overrideNonIgnored) {
				overrideNonIgnored(index);
			} else {
				dontIgnore(index);
			}
		}

		normalPath.appendTo(this.normalizedSteps);

		if (normalizer.isNormalizationFinished()) {
			addRest(getStepIndex() + 1);
		} else {
			this.stepNormalized = normalizer.stepNormalized;
			this.stepPrediction = normalizer.stepPrediction;
			this.nextPrediction = normalizer.nextPrediction;
		}
	}

	public final void cancel() {
		this.stepNormalized = false;
	}

	public final boolean cancelIncompleteNormalization(Path path) {
		return cancelIncompleteNormalization(path, 0);
	}

	public final boolean finish() {
		if (!isNormalizationStarted()) {
			cancel();
			return false;
		}
		if (isAbsolute()) {
			cancel();
			return false;
		}

		if (this.nextPrediction != null) {
			// Previous step is skipped. Add it too.
			addRest(getStepIndex() - 1);
		} else {
			addRest(getStepIndex());
		}
		this.stepPrediction = null;
		this.nextPrediction = null;
		this.data.normalizationFinished = true;

		return true;
	}

	@Override
	public String toString() {
		return "PathNormalizer[" + this.path + ']';
	}

	NormalPath normalize() {
		this.normalizedSteps =
				new NormalizedSteps(getNormalizer(), this.path.length());
		if (isStatic()) {
			return normalizeStatic();
		}
		return normalizeRelative();
	}

	private boolean init() {
		if (upTo(getOrigin().getScope())) {
			return true;
		}
		if (!getOrigin().getScope().contains(getNormalizedStart())) {
			return this.staticNormalization;
		}

		this.data.normalizationStarted = true;

		return true;
	}

	private NormalPath unnormalized() {
		this.parentNormalizer.cancelAll();
		return new UnNormalizedPath(getPath());
	}

	private NormalPath normalizeRelative() {
		this.staticNormalization = false;

		final BoundPath path = getPath();
		final Step[] steps = path.getSteps();

		this.stepStart = getOrigin();

		while (this.stepIndex < steps.length) {
			this.stepPrediction = null;
			this.stepNormalized = false;

			steps[this.stepIndex].normalize(this);
			if (isNormalizationFinished()) {
				if (cancelationRequired()) {
					return unnormalized();
				}
				return new NormalizedPath(
						getNormalizedStart(),
						this.path,
						this.normalizedSteps.steps(),
						this.firstNonIgnored,
						isAbsolute(),
						isStatic()).done(getAnalyzer(), !isNested());
			}
			if (!isStepNormalized()) {
				// Normalization failed.
				// Leave the path as is.
				return unnormalized();
			}

			this.stepStart = this.stepPrediction;
			++this.stepIndex;
		}

		if (!isNormalizationStarted() && !isNested()) {
			return unnormalized();
		}

		return new NormalizedPath(
				getNormalizedStart(),
				this.path,
				this.normalizedSteps.steps(),
				this.firstNonIgnored,
				isAbsolute(),
				isStatic()).done(getAnalyzer(), !isNested());
	}

	private NormalPath normalizeStatic() {
		this.staticNormalization = true;

		final BoundPath path = getPath();
		final Step[] steps = path.getSteps();
		final Data stored = this.data.clone();

		this.stepIndex = path.startIndex() - 1;

		final Scope startObjectScope = path.startObjectScope();

		this.stepStart = exactPrediction(
				new InitialPrediction(startObjectScope),
				startObjectScope);

		if (!isAbsolute()) {
			this.normalizedSteps.addNormalStep(new SameNormalStep(
					new StaticStep(getNormalizedStart(), startObjectScope)));
		}
		while (this.stepIndex < steps.length) {
			this.stepPrediction = null;
			this.stepNormalized = false;

			steps[this.stepIndex].normalizeStatic(this);
			if (isNormalizationFinished()) {
				if (cancelationRequired()) {
					return unnormalized();
				}
				return new NormalizedPath(
						getNormalizedStart(),
						this.path,
						this.normalizedSteps.steps(),
						this.firstNonIgnored,
						isAbsolute(),
						isStatic()).done(getAnalyzer(), !isNested());
			}
			if (!isStepNormalized()) {
				break;
			}

			this.stepStart = this.stepPrediction;
			++this.stepIndex;
		}

		getNormalizer().cancelAll();

		if (path.isAbsolute()) {
			this.data.restore(stored);
			this.firstNonIgnored = 0;
			this.data.normalizationFinished = true;
			path.doubt(getAnalyzer()).abortNormalization();
			return new UnchangedNormalPath(
					path.getPath().bind(path, getNormalizedStart()));
		}

		return unnormalized();
	}

	private boolean cancelationRequired() {
		if (this.normalizer.isCancelled()) {
			return true;
		}
		return cancelIncompleteNormalization(
				getPath().getPath(),
				this.stepIndex + 1);
	}

	private boolean cancelIncompleteNormalization(Path path, int fromIndex) {

		final Step[] steps = path.getSteps();

		for (int i = fromIndex; i < steps.length; ++i) {
			if (steps[i].cancelIncompleteNormalization(this)) {
				return true;
			}
		}

		return false;
	}

	private boolean upTo(Scope scope) {
		if (this.staticNormalization) {
			return true;
		}

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

	private final void add(Prediction prediction, NormalStep normalStep) {
		this.stepPrediction = prediction;
		this.nextPrediction = null;
		this.stepNormalized = true;
		this.normalizedSteps.addNormalStep(normalStep);
	}

	private final void dontIgnore() {
		dontIgnore(this.normalizedSteps.size() - 1);
	}

	private final void dontIgnore(int index) {
		if (this.firstNonIgnored < 0) {
			this.firstNonIgnored = index;
		}
	}

	private final void overrideNonIgnored() {
		overrideNonIgnored(this.normalizedSteps.size() - 1);
	}

	private final void overrideNonIgnored(int firstNonIgnored) {
		this.firstNonIgnored = firstNonIgnored;
		this.overrideNonIgnored = true;
	}

	private void addRest(int nextStep) {
		if (nextStep < getPath().length()) {
			this.normalizedSteps.addNormalStep(new SubPathNormalStep(
					getPath().getPath(),
					nextStep,
					getPath().length()));
		}
	}

	private static final class Data implements Cloneable {

		private boolean normalizationStarted;
		private boolean normalizationFinished;
		private boolean isAbsolute;
		private boolean isStatic;

		Data(BoundPath path) {
			this.isAbsolute = path.isAbsolute();
			this.isStatic = path.isStatic();
		}

		@Override
		protected Data clone() {
			try {
				return (Data) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		final void append(BoundPath path) {
			this.isAbsolute |= path.isAbsolute();
			this.isStatic |= path.isStatic();
		}

		final void restore(Data stored) {
			this.normalizationStarted = stored.normalizationStarted;
			this.normalizationFinished = stored.normalizationFinished;
			this.isAbsolute = stored.normalizationFinished;
			this.isStatic = stored.normalizationFinished;
		}

	}

	private final class NormalizedSteps
			extends Normal
			implements NormalSteps, Cancelable {

		private final ArrayList<NormalStep> steps;

		NormalizedSteps(Normalizer normalizer, int length) {
			super(normalizer);
			this.steps = new ArrayList<>(length);
		}

		@Override
		public final void addNormalStep(NormalStep step) {
			this.steps.add(step);
			if (getNormalizer().isCancelled()) {
				step.cancel();
			}
		}

		public final ArrayList<NormalStep> steps() {
			return this.steps;
		}

		public final int size() {
			return this.steps.size();
		}

		@Override
		public void cancel() {
			getPath()
			.doubt(getNormalizer().getAnalyzer())
			.abortNormalization();
			if (this.steps != null) {
				for (NormalStep step : this.steps) {
					step.cancel();
				}
			}
		}

		@Override
		protected Cancelable cancelable() {
			return this;
		}

	}

}
