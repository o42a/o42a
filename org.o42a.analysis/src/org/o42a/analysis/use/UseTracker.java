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


public class UseTracker {

	private UseFlag useFlag;
	private int updateRev;
	private int checkRev;
	private boolean hasUnknown;

	public final UseFlag getUseFlag() {
		return this.useFlag;
	}

	public final boolean start(UseCase useCase) {
		if (!useCase.caseFlag(this.useFlag)) {
			// Use flag belongs to another use case.
			// Evaluate the use status.
			this.useFlag = useCase.checkUseFlag();
			this.updateRev = useCase.getUpdateRev();
			this.checkRev = useCase.start(this);
			this.hasUnknown = false;
			return true;
		}

		final int updateRev = useCase.getUpdateRev();

		if (this.updateRev != updateRev) {
			// Use graph updated since last check.
			// Re-evaluate the use status.
			this.useFlag = useCase.checkUseFlag();
			this.updateRev = updateRev;
			this.checkRev = useCase.start(this);
			this.hasUnknown = false;
			return true;
		}

		// No use graph updates since last check.
		if (this.useFlag.isKnown()) {
			// Use status already evaluated.
			return false;
		}

		final int checkRev = useCase.start(this);

		if (this.checkRev == checkRev) {
			// Use status evaluation is already in progress.
			return false;
		}
		this.checkRev = checkRev;
		this.hasUnknown = false;

		return true;
	}

	public final <U extends Usage<U>> boolean useBy(
			Function<UseCase, UseFlag> detect) {

		final UseCase useCase = this.useFlag.getUseCase();
		final UseFlag used = detect.apply(useCase);

		if (!used.isUsed()) {
			this.hasUnknown |= !used.isKnown();
			return false;
		}

		useCase.end(this);
		this.useFlag = used;

		return true;
	}

	public final <U extends Usage<U>> boolean useBy(Uses<U> use) {

		final UseCase useCase = this.useFlag.getUseCase();
		final UseFlag used = use.selectUse(useCase, use.allUsages());

		if (!used.isUsed()) {
			this.hasUnknown |= !used.isKnown();
			return false;
		}

		useCase.end(this);
		this.useFlag = used;

		return true;
	}

	public final UseFlag unused() {

		final UseCase useCase = this.useFlag.getUseCase();

		if (useCase.end(this) || !this.hasUnknown) {
			return this.useFlag = useCase.unusedFlag();
		}

		return this.useFlag;
	}

}
