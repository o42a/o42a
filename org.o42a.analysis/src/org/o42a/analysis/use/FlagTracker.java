/*
    Compilation Analysis
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
package org.o42a.analysis.use;

import java.util.function.Function;


public abstract class FlagTracker<A extends UseCaseInfo, F> {

	private F lastFlag;
	private int updateRev;
	private int checkRev;
	private boolean hasUnknown;

	public final F lastFlag() {
		return this.lastFlag;
	}

	public final boolean start(A useCase) {

		final UseCase uc = useCase.toUseCase();

		if (!flagIsActual(useCase, this.lastFlag)) {
			// Use flag belongs to another use case.
			// Evaluate the use status.
			this.lastFlag = checkFlag(useCase);
			this.updateRev = uc.getUpdateRev();
			this.checkRev = uc.start(this);
			this.hasUnknown = false;
			return true;
		}

		final int updateRev = uc.getUpdateRev();

		if (this.updateRev != updateRev) {
			// Use graph updated since last check.
			// Re-evaluate the use status.
			this.lastFlag = checkFlag(useCase);
			this.updateRev = updateRev;
			this.checkRev = uc.start(this);
			this.hasUnknown = false;
			return true;
		}

		// No use graph updates since last check.
		if (flagIsKnown(this.lastFlag)) {
			// Use status already evaluated.
			return false;
		}

		final int checkRev = uc.start(this);

		if (this.checkRev == checkRev) {
			// Use status evaluation is already in progress.
			return false;
		}
		this.checkRev = checkRev;
		this.hasUnknown = false;

		return true;
	}

	public final <U extends Usage<U>> boolean check(Function<A, F> detect) {

		final A useCase = useCaseOf(this.lastFlag);
		final F flag = detect.apply(useCase);

		if (!flagIsUsed(flag)) {
			this.hasUnknown |= !flagIsKnown(flag);
			return false;
		}

		useCase.toUseCase().end(this);
		this.lastFlag = flag;

		return true;
	}

	public final F unused() {

		final A useCase = useCaseOf(this.lastFlag);

		if (useCase.toUseCase().end(this) || !this.hasUnknown) {
			return this.lastFlag = unusedFlag(useCase);
		}

		return this.lastFlag;
	}

	protected abstract A useCaseOf(F flag);

	protected abstract boolean flagIsActual(A useCase, F flag);

	protected abstract boolean flagIsKnown(F flag);

	protected abstract boolean flagIsUsed(F flag);

	protected abstract F checkFlag(A useCase);

	protected abstract F unusedFlag(A useCase);

}
