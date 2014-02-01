/*
    Compiler
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
package org.o42a.core.value.array;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.Statements;


final class ArrayContentReproducer extends Reproducer {

	private final Reproducer arrayReproducer;

	ArrayContentReproducer(
			Array array,
			Distributor distributor,
			Reproducer arrayReproducer) {
		super(array.getScope(), distributor);
		this.arrayReproducer = arrayReproducer;
	}

	@Override
	public Ref getPhrasePrefix() {
		return this.arrayReproducer.getPhrasePrefix();
	}

	@Override
	public boolean phraseCreatesObject() {
		return this.arrayReproducer.phraseCreatesObject();
	}

	@Override
	public MemberRegistry getMemberRegistry() {
		return MemberRegistry.noDeclarations();
	}

	@Override
	public Statements<?> getStatements() {
		return null;
	}

	@Override
	public Reproducer reproducerOf(Scope reproducingScope) {
		if (reproducingScope.is(getReproducingScope())) {
			return this;
		}
		return this.arrayReproducer.reproducerOf(reproducingScope);
	}

	@Override
	public void applyClause(
			LocationInfo location,
			Statements<?> statements,
			Clause clause) {
		throw new UnsupportedOperationException();
	}

}
