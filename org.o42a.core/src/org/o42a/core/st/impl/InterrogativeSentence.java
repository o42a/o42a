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

import static org.o42a.core.st.sentence.SentenceKind.INTERROGATIVE_SENTENCE;

import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.*;


public final class InterrogativeSentence extends Sentence {

	private final MemberRegistry memberRegistry;

	public InterrogativeSentence(
			LocationInfo location,
			Block block,
			SentenceFactory<?> sentenceFactory) {
		super(location, block, sentenceFactory);
		this.memberRegistry =
				new InterrogativeMemberRegistry(block.getMemberRegistry());
	}

	@Override
	public SentenceKind getKind() {
		return INTERROGATIVE_SENTENCE;
	}

	@Override
	public MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

}
