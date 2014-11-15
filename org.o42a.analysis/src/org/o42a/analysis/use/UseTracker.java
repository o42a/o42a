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


public class UseTracker extends FlagTracker<UseCase, UseFlag> {

	public final UseFlag getUseFlag() {
		return lastFlag();
	}

	public final boolean useBy(Function<UseCase, UseFlag> detect) {
		return check(detect);
	}

	public final <U extends Usage<U>> boolean useBy(Uses<U> use) {
		return check(uc -> use.selectUse(uc, use.allUsages()));
	}

	@Override
	protected UseCase useCaseOf(UseFlag flag) {
		return flag.getUseCase();
	}

	@Override
	protected boolean flagIsActual(UseCase useCase, UseFlag flag) {
		return useCase.caseFlag(flag);
	}

	@Override
	protected boolean flagIsKnown(UseFlag flag) {
		return flag.isKnown();
	}

	@Override
	protected boolean flagIsUsed(UseFlag flag) {
		return flag.isUsed();
	}

	@Override
	protected UseFlag checkFlag(UseCase useCase) {
		return useCase.checkUseFlag();
	}

	@Override
	protected UseFlag unusedFlag(UseCase useCase) {
		return useCase.unusedFlag();
	}

}
