/*
    Compiler Code Generator
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.codegen;

import static org.o42a.util.use.User.steadyUseCase;
import static org.o42a.util.use.User.useCase;

import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseCaseInfo;
import org.o42a.util.use.User;


public class Analyzer implements UseCaseInfo {

	private UseCase useCase;
	private boolean normalizationEnabled = true;

	public Analyzer(String name) {
		this.useCase = useCase(name);
	}

	public final boolean isUsesAnalysed() {
		return !this.useCase.isSteady();
	}

	public final Analyzer setUsesAnalysed(boolean usesAnalysed) {
		if (isUsesAnalysed() != usesAnalysed) {
			if (usesAnalysed) {
				this.useCase = useCase(this.useCase.getName());
			} else {
				this.useCase = steadyUseCase(this.useCase.getName());
			}
		}
		return this;
	}

	public final boolean isNormalizationEnabled() {
		return this.normalizationEnabled;
	}

	public final Analyzer setNormalizationEnabled(
			boolean normalizationEnabled) {
		this.normalizationEnabled = normalizationEnabled;
		return this;
	}

	@Override
	public final User<?> toUser() {
		return this.useCase;
	}

	@Override
	public final UseCase toUseCase() {
		return this.useCase;
	}

	@Override
	public String toString() {
		if (this.useCase == null) {
			return super.toString();
		}
		return "Analyzer[" + this.useCase.getName() + ']';
	}

}
