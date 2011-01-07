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

import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.Clause;
import org.o42a.util.ArrayUtil;


public class NextClause implements Cloneable {

	private static final NextClause[] NO_IMPLIED = new NextClause[0];

	public static NextClause clauseNotFound(MemberId memberId) {
		assert memberId != null :
			"Member identifier not specified";
		return new ClauseNotFound(memberId);
	}

	public static NextClause declarationsClause(Clause clause) {
		return new DeclarationsClause(clause);
	}

	public static NextClause nextClause(MemberId memberId, Clause clause) {
		assert memberId != null :
			"Member identifier not specified";
		assert clause != null :
			"Clause not specified";
		return new NextClause(memberId, clause, clause.getEnclosingClause());
	}

	public static NextClause nextClause(
			MemberId memberId,
			Clause clause,
			Clause container) {
		assert memberId != null :
			"Member identifier not specified";
		assert clause != null :
			"Clause not specified";
		return new NextClause(memberId, clause, container);
	}

	private final MemberId memberId;
	private final Clause clause;
	private Clause container;
	private NextClause[] implicit;

	private NextClause(MemberId memberId, Clause clause, Clause container) {
		this.memberId = memberId;
		this.clause = clause;
		this.container = container;
		this.implicit = NO_IMPLIED;
	}

	public boolean found() {
		return true;
	}

	public final MemberId getMemberId() {
		return this.memberId;
	}

	public final Clause getClause() {
		return this.clause;
	}

	public final Clause getContainer() {
		return this.container;
	}

	public final NextClause[] getImplicit() {
		return this.implicit;
	}

	public final NextClause setImplicit(NextClause implicit) {

		final NextClause clone = clone();

		clone.implicit = ArrayUtil.prepend(implicit, this.implicit);

		return clone;
	}

	public final NextClause setContainer(Clause container) {

		final NextClause clone = clone();

		if (this.implicit.length == 0) {
			clone.container = container;
		} else {
			clone.implicit[0] = this.implicit[0].setContainer(container);
		}

		return clone;
	}

	@Override
	public String toString() {
		return this.memberId + "(" + this.clause + ")";
	}

	@Override
	protected NextClause clone() {
		try {
			return (NextClause) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	private static final class ClauseNotFound extends NextClause {

		ClauseNotFound(MemberId memberId) {
			super(memberId, null, null);
		}

		@Override
		public boolean found() {
			return false;
		}

		@Override
		public String toString() {
			return getMemberId() + "(NOT FOUND)";
		}

	}

	private static final class DeclarationsClause extends NextClause {

		DeclarationsClause(Clause clause) {
			super(null, clause, clause);
		}

		@Override
		public String toString() {
			if (getClause() == null) {
				return "DeclarationsClause()";
			}
			return "DeclarationsClause(" + getClause() + ")";
		}

	}

}
