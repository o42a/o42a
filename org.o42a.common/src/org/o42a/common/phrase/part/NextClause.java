/*
    Compiler Commons
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.common.phrase.part.PartsAsPrefix.NOT_PREFIX;
import static org.o42a.common.phrase.part.PartsAsPrefix.PREFIX_WITH_LAST;

import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.Clause;
import org.o42a.util.ArrayUtil;


public class NextClause implements Cloneable {

	private static final NextClause[] NO_IMPLICIT = new NextClause[0];

	public static NextClause errorClause(Object reason) {
		assert reason != null :
			"Error reason not specified";
		return new ErrorClause(reason);
	}

	public static NextClause clauseNotFound(Object what) {
		assert what != null :
			"What is not found not specified";
		return new ClauseNotFound(what);
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
	private final PartsAsPrefix partsAsPrefix;
	private Clause container;
	private NextClause[] implicit = NO_IMPLICIT;

	private NextClause(MemberId memberId, Clause clause, Clause container) {
		this.memberId = memberId;
		this.clause = clause;
		if (clause != null && clause.isTerminator()) {
			this.partsAsPrefix = PREFIX_WITH_LAST;
		} else {
			this.partsAsPrefix = NOT_PREFIX;
		}
		this.container = container;
	}

	NextClause(PartsAsPrefix partsAsPrefix) {
		this.memberId = null;
		this.clause = null;
		this.partsAsPrefix = partsAsPrefix;
	}

	public boolean found() {
		return true;
	}

	public boolean isError() {
		return false;
	}

	public Object what() {
		return this.memberId;
	}

	public final PartsAsPrefix partsAsPrefix() {
		return this.partsAsPrefix;
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

	public boolean requiresInstance() {
		if (this.implicit.length != 0) {
			return this.implicit[0].requiresInstance();
		}
		if (getClause() == null) {
			// Next clause is an object itself.
			// New object will be constructed.
			return true;
		}
		// Next clause requires enclosing object instance to be created.
		return getClause().requiresInstance();
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

		private final Object what;

		ClauseNotFound(Object what) {
			super(null, null, null);
			this.what = what;
		}

		@Override
		public Object what() {
			return this.what;
		}

		@Override
		public boolean found() {
			return false;
		}

		@Override
		public String toString() {
			return this.what + "(NOT FOUND)";
		}

	}

	private static final class ErrorClause extends NextClause {

		private final Object what;

		ErrorClause(Object what) {
			super(null, null, null);
			this.what = what;
		}

		@Override
		public boolean isError() {
			return true;
		}

		@Override
		public Object what() {
			return this.what;
		}

		@Override
		public String toString() {
			return "ERROR(" + this.what + ")";
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
