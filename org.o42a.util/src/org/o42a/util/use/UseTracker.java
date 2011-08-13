/*
    Utilities
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
package org.o42a.util.use;


public class UseTracker {

	private UseFlag useFlag;
	private int rev;

	public final UseFlag getUseFlag() {
		return this.useFlag;
	}

	public final boolean start(UseCase useCase) {
		if (!useCase.caseFlag(this.useFlag)) {
			this.rev = useCase.start(this);
			this.useFlag = useCase.checkUseFlag();
			return true;
		}
		if (this.useFlag.isKnown()) {
			return false;
		}

		final int rev = useCase.start(this);

		if (this.rev == rev) {
			return false;
		}
		this.rev = rev;

		return true;
	}

	public final boolean require(UseInfo use) {

		final UseCase useCase = this.useFlag.getUseCase();
		final UseFlag used = use.getUseBy(useCase);

		if (used.isUsed()) {
			useCase.end(this);
			return true;
		}

		if (useCase.end(this)) {
			this.useFlag = useCase.unusedFlag();
		} else {
			this.useFlag = used;
		}

		return false;
	}

	public final boolean useBy(UseInfo use) {

		final UseCase useCase = this.useFlag.getUseCase();
		final UseFlag used = use.getUseBy(useCase);

		if (!used.isUsed()) {
			if (useCase.end(this)) {
				this.useFlag = useCase.unusedFlag();
			}
			return false;
		}

		useCase.end(this);
		this.useFlag = used;

		return true;
	}

	public final UseFlag done() {

		final UseCase useCase = this.useFlag.getUseCase();

		if (useCase.end(this)) {
			return this.useFlag = useCase.unusedFlag();
		}

		return this.useFlag;
	}

}
