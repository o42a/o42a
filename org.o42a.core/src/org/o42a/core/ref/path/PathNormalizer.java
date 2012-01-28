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
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.util.Cancellation.cancelAll;
import static org.o42a.util.use.User.dummyUser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.o42a.codegen.Analyzer;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.impl.normalizer.*;


public final class PathNormalizer {

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
		this.origin = parent.lastPrediction();
		this.path = path;
		this.normalSteps = new ArrayList<NormalStep>(path.length());
	}

	public final Analyzer getAnalyzer() {
		return getNormalizer().getAnalyzer();
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

	public final Prediction stepStart() {
		return this.stepStart;
	}

	public final Prediction lastPrediction() {
		if (this.stepPrediction == null) {
			return this.stepStart;
		}
		return this.stepPrediction;
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

	public final void skip(
			Prediction prediction,
			NormalAppender normalStep) {
		add(prediction, normalStep);
	}

	public final void inline(
			Prediction prediction,
			InlineStep normalStep) {
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

	public final boolean up(Scope enclosing) {
		if (isNormalizationStarted()) {

			final Scope current = lastPrediction().getScope();

			this.normalSteps.add(
					new NormalPathStep(current.getEnclosingScopePath()));
			overrideNonIgnored();
			addRest();
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
			addRest();
		}
	}

	public final void cancel() {
		this.stepNormalized = false;
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
						isStatic()).done(this.parent == null);
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
				isStatic()).done(this.parent == null);
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

	private final void add(Prediction prediction, NormalStep normalStep) {
		this.stepPrediction = prediction;
		this.stepNormalized = true;
		this.normalSteps.add(normalStep);
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

	private void addRest() {

		final int nextStep = getStepIndex() + 1;

		if (nextStep < getPath().length()) {
			this.normalSteps.add(new PathRemainderNormalStep(
					getPath().getPath(),
					nextStep));
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
		private final ArrayList<NormalStep> normalSteps;
		private final int firstNonIgnored;
		private final boolean isAbsolute;
		private final boolean isStatic;

		private InlineStep inline;
		private BoundPath path;

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
		public void cancel() {
			cancelAll(this.normalSteps);
		}

		@Override
		public void appendTo(List<NormalStep> normalSteps) {
			normalSteps.addAll(this.normalSteps);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs, HostOp host) {
			if (this.inline != null) {
				this.inline.writeLogicalValue(dirs, host);
			} else {
				this.path.op(dirs, host).writeLogicalValue(dirs);
			}
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			if (this.inline != null) {
				return this.inline.writeValue(dirs, host);
			}
			return this.path.op(dirs.dirs(), host).writeValue(dirs);
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

		final NormalPath done(boolean done) {
			if (done) {
				ignoreLeading();
				build();
			}
			return this;
		}

		private void ignoreLeading() {
			for (int i = 0; i < this.firstNonIgnored; ++i) {
				this.normalSteps.get(i).ignore();
			}
		}

		private NormalPath build() {

			InlineStep precedingInline = null;
			Path path;

			if (this.isAbsolute) {
				path = ROOT_PATH;
			} else {
				path = SELF_PATH;
			}

			for (int i = this.firstNonIgnored, len = this.normalSteps.size();
					i < len;
					++i) {

				final NormalStep normalStep = this.normalSteps.get(i);
				final InlineStep inline = normalStep.toInline();

				if (inline != null) {
					inline.after(precedingInline);
					precedingInline = inline;
					continue;
				}

				assert precedingInline == null :
					"Non-in-line normal step (" + normalStep
					+ ") after the in-line one (" + precedingInline
					+ ")";

				path = normalStep.toAppender().appendTo(path);
			}

			if (precedingInline != null) {
				// In-line normal step.
				this.inline = precedingInline;
				return this;
			}
			if (!this.isStatic) {
				this.path = path.bind(this.path, getOrigin());
			} else {
				this.path = path.bindStatically(this.path, getOrigin());
			}

			this.path.resolve(fullPathResolver(
					getOrigin(),
					dummyUser(),
					VALUE_REF_USAGE));

			return this;
		}

	}

}
