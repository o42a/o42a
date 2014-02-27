/*
    Abstract Syntax Tree
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
package org.o42a.ast.sentence;

import org.o42a.ast.atom.SignType;


public enum SentenceType implements SignType {

	DECLARATION(".", false),
	CONTINUATION("...", true),
	EXCLAMATION("!", false),
	CONTINUED_EXCLAMATION("!..", true),
	INTERROGATION("?", false),
	CONTINUED_INTERROGATION("?..", true);

	private final String sign;
	private final boolean supportsContinuation;

	SentenceType(String sign, boolean supportsContinuation) {
		this.sign = sign;
		this.supportsContinuation = supportsContinuation;
	}

	@Override
	public final String getSign() {
		return this.sign;
	}

	public final boolean supportsContinuation() {
		return this.supportsContinuation;
	}

}
