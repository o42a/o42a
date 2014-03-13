/*
    Compiler Core
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
package org.o42a.core.st.impl;

import static org.o42a.core.st.sentence.SentenceKind.DECLARATIVE_SENTENCE;
import static org.o42a.core.st.sentence.SentenceKind.IMPERATIVE_SENTENCE;

import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.*;


public final class DefaultSentence extends Sentence {

	public DefaultSentence(
			LocationInfo location,
			Block block,
			SentenceFactory<?> sentenceFactory) {
		super(location, block, sentenceFactory);
	}

	@Override
	public SentenceKind getKind() {
		return isImperative() ? IMPERATIVE_SENTENCE : DECLARATIVE_SENTENCE;
	}

}
