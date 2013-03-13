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

import org.o42a.core.member.clause.ClauseId;


public enum UnaryPhraseOperator {

	PLUS("+", ClauseId.PLUS),
	MINUS("-", ClauseId.MINUS);

	private final String sign;
	private final ClauseId clauseId;

	UnaryPhraseOperator(String sign, ClauseId clauseId) {
		this.sign = sign;
		this.clauseId = clauseId;
	}

	public final String getSign() {
		return this.sign;
	}

	public final ClauseId getClauseId() {
		return this.clauseId;
	}

}