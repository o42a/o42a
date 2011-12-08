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

import org.o42a.core.Scope;


public class PathNormalizer {

	private final Scope start;
	private final BoundPath path;
	private final ArrayList<NormalStep> normalSteps;

	private Scope stepStart;
	private Scope stepResolution;
	private int stepIndex;

	private boolean normalizationStarted;
	private boolean stepNormalized;

	PathNormalizer(Scope start, BoundPath path) {
		this.start = start;
		this.path = path;
		this.normalSteps = new ArrayList<NormalStep>(path.length());
	}

	public final Scope getOrigin() {
		return this.start;
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

	public final void add(Scope resolution, NormalStep normalStep) {
		this.stepResolution = resolution;
		this.normalizationStarted = true;
		this.stepNormalized = true;
		this.normalSteps.add(normalStep);
	}

	public final void up(Scope enclosing) {
		if (this.normalizationStarted) {
			return;
		}

		// Enclosing scope not reached yet.
		// No need to add the enclosing scope step.
		this.stepNormalized = true;
		this.stepResolution = enclosing;

		if (enclosing == this.start) {
			// Enclosing scope reached.
			this.normalizationStarted = true;
		}
	}

	@Override
	public String toString() {
		return "PathNormalizer[" + this.path + ']';
	}

	BoundPath normalize() {

		final BoundPath path = getPath();

		if (path.isAbsolute()) {
			this.stepStart = path.root(getOrigin());
		} else {
			this.stepStart = path.getOrigin();
		}

		final Step[] steps = path.getSteps();

		while (this.stepIndex < steps.length) {
			this.stepResolution = null;
			this.stepNormalized = false;

			steps[this.stepIndex].normalize(this);
			if (!this.stepNormalized) {
				// Normalization failed.
				// Leave the path as is.
				return path;
			}

			this.stepStart = this.stepResolution;
			++this.stepIndex;
		}

		Path result;

		if (path.isAbsolute()) {
			result = ROOT_PATH;
		} else if (!path.isStatic()) {
			result = SELF_PATH;
		} else {
			result = EMPTY_STATIC_PATH;
		}

		for (NormalStep normalStep : this.normalSteps) {
			result = normalStep.appendTo(result);
		}

		return result.bind(path, getOrigin());
	}

}
