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
package org.o42a.core.ref.phrase;

import org.o42a.core.LocationSpec;
import org.o42a.core.st.sentence.Block;


public class PhraseName extends PhraseContinuation {

	private final String name;

	PhraseName(LocationSpec location, PhrasePart prev, String name) {
		super(location, prev);
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	protected PhraseName name(LocationSpec location, String name) {
		getLogger().prohibitedPhraseName(location);
		return null;
	}

	@Override
	protected NextClause nextClause(PhraseContext context) {
		return context.clauseByName(this, getName());
	}

	@Override
	protected void define(Block<?> definition) {
	}

}
