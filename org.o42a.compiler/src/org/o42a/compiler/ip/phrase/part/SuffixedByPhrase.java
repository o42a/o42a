/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.phrase.part;

import org.o42a.compiler.ip.phrase.ref.PhraseContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public class SuffixedByPhrase extends PhraseContinuation {

	private final Ref prefix;

	SuffixedByPhrase(LocationInfo location, PhrasePart preceding, Ref prefix) {
		super(location, preceding);
		this.prefix = prefix;
	}

	public final Ref getPrefix() {
		return this.prefix;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {

		final NextClause next = context.clauseById(this, ClauseId.SUFFIX);

		if (next.found()) {
			return next;
		}
		if (this.prefix == null) {
			return next;
		}
		if (!context.isObject()) {
			return next;
		}

		return next;
	}

	@Override
	public Ref substitute(Distributor distributor) {
		if (this.prefix == null) {
			return null;
		}
		return this.prefix.rescope(distributor.getScope());
	}

	@Override
	public void define(Block<?, ?> definition) {
		if (this.prefix == null) {
			return;// Do not assign any value.
		}

		final Statements<?, ?> statements =
				definition.propose(this).alternative(this);

		statements.selfAssign(this.prefix);
	}

	@Override
	public String toString() {
		if (this.prefix == null) {
			return "~";
		}
		return this.prefix.toString() + '~';
	}

}
