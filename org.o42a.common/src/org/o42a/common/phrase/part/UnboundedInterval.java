/*
    Compiler Commons
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.common.phrase.part;

import static org.o42a.core.member.clause.ClauseId.UNBOUNDED_INTERVAL;

import org.o42a.common.phrase.PhraseContext;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;


public class UnboundedInterval extends PhraseContinuation {

	UnboundedInterval(LocationInfo location, PhrasePart preceding) {
		super(location, preceding);
	}

	@Override
	public NextClause nextClause(PhraseContext context) {
		return context.clauseById(this, UNBOUNDED_INTERVAL);
	}

	@Override
	public Ref substitute(Distributor distributor) {
		return getPhrase().getAncestor().getRef().rescope(
				distributor.getScope());
	}

	@Override
	public void define(Block definition) {
	}

	@Override
	public String toString() {
		return "(-...-)";
	}

}
