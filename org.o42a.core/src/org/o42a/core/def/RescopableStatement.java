/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.def;

import org.o42a.core.Scope;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;


public abstract class RescopableStatement<R extends RescopableStatement<R>>
		extends Rescopable<R> {

	public RescopableStatement(Rescoper rescoper) {
		super(rescoper);
	}

	public abstract Statement getStatement();

	public R reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Scope rescoped = getRescoper().rescope(getScope());
		final Reproducer rescopedReproducer = reproducer.reproducerOf(rescoped);

		if (rescopedReproducer == null) {
			reproducer.getLogger().notReproducible(this);
			return null;
		}

		final Rescoper rescoper = getRescoper().reproduce(this, reproducer);

		if (rescoper == null) {
			return null;
		}

		final Statement statement =
			getStatement().reproduce(rescopedReproducer);

		if (statement == null) {
			return null;
		}

		return createReproduction(
				reproducer,
				rescopedReproducer,
				statement,
				rescoper);
	}

	@Override
	protected abstract R create(
			Rescoper rescoper,
			Rescoper additionalRescoper);

	protected abstract R createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Statement statement,
			Rescoper rescoper);

}
