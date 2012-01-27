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
package org.o42a.core.st.impl.declarative;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.core.value.ValueStruct;


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
	public final InlineCommand inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	public final void normalizeImperative(Normalizer normalizer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Statement reproduce(Reproducer reproducer) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final void fullyResolveImperative(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final StOp createOp(CodeBuilder builder) {
		throw new UnsupportedOperationException();
	}

	protected abstract InclusionDefiner<?> createDefiner(StatementEnv env);

}
