/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import java.util.Arrays;

import org.o42a.core.Scope;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.ArrayUtil;


public final class PathRebuilder implements LocationInfo {

	private final BoundPath path;
	private final Step[] steps;
	private Step previousStep;
	private int nextIdx;

	private final Step[] rebuiltSteps;
	private int rebuiltIdx;
	private byte replacement;

	PathRebuilder(BoundPath path, Step[] steps) {
		this.path = path;
		this.steps = steps;
		this.rebuiltSteps = new Step[steps.length];
		this.previousStep = this.rebuiltSteps[0] = steps[0];
		this.nextIdx = 1;
		this.rebuiltIdx = 0;
	}

	public final BoundPath getPath() {
		return this.path;
	}

	@Override
	public final Location getLocation() {
		return getPath().getLocation();
	}

	public final boolean isStatic() {
		return getPath().isStatic();
	}

	public final BoundPath restPath(Scope newOrigin) {
		return new Path(
				this.path.getKind(),
				this.path.isStatic(),
				this.path.getPath().getTemplate(),
				Arrays.copyOfRange(
						this.steps,
						this.nextIdx,
						this.steps.length))
				.bind(this, newOrigin);
	}

	public final Step getPreviousStep() {
		return this.previousStep;
	}

	public final BoundPath cutPath(int stepsToCut) {

		final Path rawPath = getPath().getRawPath();
		final Path path = new Path(
				rawPath.getKind(),
				rawPath.isStatic(),
				null,
				Arrays.copyOf(this.rebuiltSteps, this.nextIdx - stepsToCut));

		return path.bind(getPath(), getPath().getOrigin());
	}

	public final void replace(Step rebuilt) {
		this.rebuiltSteps[this.rebuiltIdx] = this.previousStep = rebuilt;
		this.replacement = 1;
	}

	public final void replaceRest(Step rebuilt) {
		this.rebuiltSteps[this.rebuiltIdx] = this.previousStep = rebuilt;
		this.replacement = 2;
	}

	public final void combinePreviousWithStatic(
			Step step,
			Scope expectedScope,
			Scope finalScope) {
		getPreviousStep().combineWithStatic(
				this,
				step,
				expectedScope,
				finalScope);
	}

	public final void combinePreviousWithConstructor(
			Step step,
			ObjectConstructor constructor) {
		getPreviousStep().combineWithConstructor(this, step, constructor);
	}

	public final void combinePreviousWithLocal(Step step, Local local) {
		getPreviousStep().combineWithLocal(this, step, local);
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return "PathRebuilder[" + this.path + ']';
	}

	Step[] rebuild() {
		for (;;) {

			final Step next = this.steps[this.nextIdx];

			if (rebuild(next)) {
				if (isRestReplaced()) {
					break;
				}
				if (++this.nextIdx >= this.steps.length) {
					break;
				}
				continue;
			}
			this.rebuiltSteps[++this.rebuiltIdx] = this.previousStep = next;
			if (++this.nextIdx >= this.steps.length) {
				break;
			}
		}

		final int rebuiltLen = this.rebuiltIdx + 1;

		if (rebuiltLen == this.steps.length) {
			return this.steps;
		}

		return ArrayUtil.clip(this.rebuiltSteps, rebuiltLen);
	}

	private boolean isRestReplaced() {
		return this.replacement == 2;
	}

	private boolean rebuild(Step next) {
		this.replacement = 0;
		next.rebuild(this);

		if (this.replacement > 0) {
			return true;
		}

		getPreviousStep().combineWith(this, next);

		return this.replacement > 0;
	}

}
