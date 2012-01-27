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

import static org.o42a.core.ref.path.PathBindings.NO_PATH_BINDINGS;

import java.util.Arrays;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.ArrayUtil;
import org.o42a.util.log.Loggable;


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

	@Override
	public final Loggable getLoggable() {
		return this.path.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.path.getContext();
	}

	public final boolean isStatic() {
		return this.path.isStatic();
	}

	public final Path restPath() {
		return new Path(
				this.path.getKind(),
				NO_PATH_BINDINGS,
				this.path.isStatic(),
				Arrays.copyOfRange(
						this.steps,
						this.nextIdx,
						this.steps.length));
	}

	public final void combineWithLocalOwner(Obj owner) {
		this.previousStep.combineWithLocalOwner(this, owner);
	}

	public final Step getPreviousStep() {
		return this.previousStep;
	}

	public final void replace(Step rebuilt) {
		this.rebuiltSteps[this.rebuiltIdx] = this.previousStep = rebuilt;
		this.replacement = 1;
	}

	public final void replaceRest(Step rebuilt) {
		this.rebuiltSteps[this.rebuiltIdx] = this.previousStep = rebuilt;
		this.replacement = 2;
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
