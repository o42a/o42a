/*
    Compiler Commons
    Copyright (C) 2013 Ruslan Lopatin

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

import static org.o42a.core.member.clause.ClauseId.*;

import org.o42a.common.phrase.PhraseContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public class HalfBoundedInterval extends PhraseContinuation {

	private final RefBuilder value;
	private final boolean open;
	private final boolean leftBounded;

	HalfBoundedInterval(
			LocationInfo location,
			PhrasePart preceding,
			RefBuilder value,
			boolean open,
			boolean leftBounded) {
		super(location, preceding);
		this.value = value;
		this.open = open;
		this.leftBounded = leftBounded;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {
		return context.clauseById(this, clauseId());
	}

	@Override
	public Ref substitute(Distributor distributor) {
		if (this.value == null) {
			return null;
		}
		return this.value.buildRef(distributor);
	}

	@Override
	public void define(Block<?> definition) {
		if (this.value == null) {
			return;// Do not assign any value.
		}

		final Statements<?> statements =
				definition.propose(this).alternative(this);

		statements.selfAssign(this.value);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(!this.open && this.leftBounded ? '[' : '(');
		if (!this.leftBounded) {
			out.append('-');
		} else if (this.value != null) {
			out.append(this.value);
		} else {
			out.append('*');
		}
		out.append("...");
		if (this.leftBounded) {
			out.append('-');
		} else if (this.value != null) {
			out.append(this.value);
		} else {
			out.append('*');
		}
		out.append(!this.open && !this.leftBounded ? ']' : ')');

		return out.toString();
	}

	private ClauseId clauseId() {
		if (this.leftBounded) {
			if (this.open) {
				return LEFT_BOUNDED_OPEN_INTERVAL;
			}
			return LEFT_BOUNDED_CLOSED_INTERVAL;
		}
		if (this.open) {
			return RIGHT_BOUNDED_OPEN_INTERVAL;
		}
		return RIGHT_BOUNDED_CLOSED_INTERVAL;
	}

}
