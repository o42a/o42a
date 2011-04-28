/*
    Compiler Core
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
package org.o42a.core.ref;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ref.common.AbstractConjunction;
import org.o42a.core.st.Reproducer;


final class LogicalAnd extends AbstractConjunction {

	private final Logical[] claims;

	LogicalAnd(LocationInfo location, Scope scope, Logical[] claims) {
		super(location, scope);
		this.claims = claims;
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Logical[] claims = new Logical[this.claims.length];

		for (int i = 0; i < claims.length; ++i) {

			final Logical reproduced =
				this.claims[i].reproduce(reproducer);

			if (reproduced == null) {
				return null;
			}

			claims[i] = reproduced;
		}

		return new LogicalAnd(this, reproducer.getScope(), claims);
	}

	@Override
	protected Logical[] expandConjunction() {
		return this.claims;
	}

	@Override
	protected int numClaims() {
		return this.claims.length;
	}

	@Override
	protected Logical claim(int index) {
		return this.claims[index];
	}

}
