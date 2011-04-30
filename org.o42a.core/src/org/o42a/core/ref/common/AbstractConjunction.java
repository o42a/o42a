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
package org.o42a.core.ref.common;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.LogicalValue;


public abstract class AbstractConjunction extends Logical {

	public AbstractConjunction(LocationInfo location, Scope scope) {
		super(location, scope);
	}

	@Override
	public LogicalValue getConstantValue() {

		LogicalValue result = null;
		final int numClaims = numClaims();

		for (int i = 0; i < numClaims; ++i) {

			final Logical claim = claim(i);
			final LogicalValue value = claim.getConstantValue();

			if (value.isFalse()) {
				return value;
			}
			result = value.and(result);
		}

		return result;
	}

	@Override
	public LogicalValue logicalValue(Scope scope) {
		assertCompatible(scope);

		LogicalValue result = null;
		final int numClaims = numClaims();

		for (int i = 0; i < numClaims; ++i) {

			final Logical claim = claim(i);
			final LogicalValue value = claim.logicalValue(scope);

			if (value.isFalse()) {
				return value;
			}
			result = value.and(result);
		}

		return result;
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();
		dirs = dirs.begin("and", "Logical AND: " + this);

		final int numClaims = numClaims();

		for (int i = 0; i < numClaims; ++i) {
			claim(i).write(dirs, host);
		}

		dirs.end();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(');

		final int numClaims = numClaims();

		for (int i = 0; i < numClaims; ++i) {
			if (out.length() > 1) {
				out.append(" && ");
			}
			out.append(claim(i));
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected Logical[] expandConjunction() {

		final int numClaims = numClaims();
		final Logical[] claims = new Logical[numClaims];

		for (int i = 0; i < numClaims; ++i) {
			claims[i] = claim(i);
		}

		return claims;
	}

	protected abstract int numClaims();

	protected abstract Logical claim(int index);

	@Override
	protected void fullyResolve() {
		for (Logical claim : expandConjunction()) {
			claim.resolveAll();
		}
	}

}
