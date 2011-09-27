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
package org.o42a.core.st.impl.declarative;

import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Declaratives;


public abstract class Inclusion extends Statement {

	private InclusionDefiner<?> definer;

	public Inclusion(LocationInfo location, Declaratives statements) {
		super(location, statements.nextDistributor());
	}

	public final StatementEnv getInitialEnv() {
		return this.definer.env();
	}

	@Override
	public Definer define(StatementEnv env) {
		return this.definer = createDefiner(env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

	protected abstract InclusionDefiner<?> createDefiner(StatementEnv env);

}
