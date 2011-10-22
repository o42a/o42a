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

import java.util.Arrays;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberKey;
import org.o42a.util.ArrayUtil;


public final class PathRebuilder {

	private final BoundPath path;
	private final Step[] steps;
	private Step prev;
	private int nextIdx;

	private final Step[] rebuiltSteps;
	private int rebuiltIdx;
	private byte replacement;

	PathRebuilder(BoundPath path, Step[] steps) {
		this.path = path;
		this.steps = steps;
		this.rebuiltSteps = new Step[steps.length];
		this.prev = this.rebuiltSteps[0] = steps[0];
		this.nextIdx = 1;
		this.rebuiltIdx = 0;
	}

	public Path restPath() {
		return new Path(
				this.path.getKind(),
				this.path.isStatic(),
				Arrays.copyOfRange(
						this.steps,
						this.nextIdx + 1,
						this.steps.length));
	}

	public final void combineWithMember(MemberKey memberKey) {
		this.prev.combineWithMember(this, memberKey);
	}

	public final void combineWithLocalOwner(Obj owner) {
		this.prev.combineWithLocalOwner(this, owner);
	}

	public final void combineWithObjectConstructor(
			ObjectConstructor constructor) {
		this.prev.combineWithObjectConstructor(this, constructor);
	}

	public final Step getPreviousStep() {
		return this.prev;
	}

	public final void replace(Step rebuilt) {
		this.rebuiltSteps[this.rebuiltIdx] = this.prev = rebuilt;
		this.replacement = 1;
	}

	public final void replaceRest(Step rebuilt) {
		this.rebuiltSteps[this.rebuiltIdx] = this.prev = rebuilt;
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
			this.rebuiltSteps[++this.rebuiltIdx] = this.prev = next;
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
		return this.replacement > 0;
	}

}
