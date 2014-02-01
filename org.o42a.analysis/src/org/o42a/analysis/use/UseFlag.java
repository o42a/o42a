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


public final class UseFlag {

	private final UseCase useCase;
	private final byte used;

	UseFlag(UseCase useCase, byte used) {
		this.useCase = useCase;
		this.used = used;
	}

	public final UseCase getUseCase() {
		return this.useCase;
	}

	public final boolean isUsed() {
		return this.used > 0;
	}

	public final boolean isKnown() {
		return this.used != 0;
	}

	@Override
	public String toString() {
		if (this.used > 0) {
			return "UsedBy[" + this.useCase + ']';
		}
		if (this.used == 0) {
			return "CheckUseBy[" + this.useCase + ']';
		}
		return "UnusedBy[" + this.useCase + ']';
	}

}
