/*
    Utilities
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
package org.o42a.util.use;


final class AlwaysUsed<U extends Usage<U>> implements Uses<U> {

	private final AllUsages<U> allUsages;

	AlwaysUsed(AllUsages<U> allUsages) {
		this.allUsages = allUsages;
	}

	@Override
	public AllUsages<U> allUsages() {
		return this.allUsages;
	}

	@Override
	public UseFlag selectUse(UseCaseInfo useCase, UseSelector<U> selector) {
		return useCase.toUseCase().usedFlag();
	}

	@Override
	public boolean isUsed(UseCaseInfo useCase, UseSelector<U> selector) {
		return true;
	}

	@Override
	public String toString() {
		return "AlwaysUsed";
	}

}
