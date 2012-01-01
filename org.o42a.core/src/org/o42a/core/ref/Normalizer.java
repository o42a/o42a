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
package org.o42a.core.ref;

import org.o42a.core.Scope;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseCaseInfo;
import org.o42a.util.use.User;


public final class Normalizer implements UseCaseInfo {

	private final UseCase useCase;
	private final Scope normalizedScope;
	private final boolean isStatic;

	public Normalizer(
			UseCaseInfo useCase,
			Scope normalizedScope,
			boolean isStatic) {
		this.isStatic = isStatic;
		this.useCase = useCase.toUseCase();
		this.normalizedScope = normalizedScope;
	}

	public final Scope getNormalizedScope() {
		return this.normalizedScope;
	}

	public final boolean isStatic() {
		return this.isStatic;
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
		if (this.normalizedScope == null) {
			return super.toString();
		}
		return "Normalizer[to " + this.normalizedScope
				+ " by " + this.useCase + ']';
	}

}
