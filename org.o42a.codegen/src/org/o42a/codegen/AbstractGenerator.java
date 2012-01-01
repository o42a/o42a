/*
    Compiler Code Generator
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
package org.o42a.codegen;

import static org.o42a.util.use.User.steadyUseCase;
import static org.o42a.util.use.User.useCase;

import org.o42a.codegen.debug.Debug;
import org.o42a.util.use.UseCase;


public abstract class AbstractGenerator extends Generator {

	private final Debug debug;
	private UseCase useCase;

	public AbstractGenerator(String id) {
		super(id);
		this.debug = new Debug(this);
		this.useCase = useCase(id);
		//setUsesAnalysed(false);
	}

	@Override
	public final Debug getDebug() {
		return this.debug;
	}

	@Override
	public final UseCase toUseCase() {
		return this.useCase;
	}

	@Override
	public final boolean isUsesAnalysed() {
		return !this.useCase.isSteady();
	}

	@Override
	public final void setUsesAnalysed(boolean usesAnalysed) {
		if (isUsesAnalysed() != usesAnalysed) {
			if (usesAnalysed) {
				this.useCase = useCase(getId());
			} else {
				this.useCase = steadyUseCase(getId());
			}
		}
	}

}
