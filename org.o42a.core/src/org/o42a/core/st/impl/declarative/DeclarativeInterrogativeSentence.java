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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.st.sentence.SentenceFactory.DECLARATIVE_INTERROGATION_FACTORY;

import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.impl.InterrogativeMemberRegistry;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.st.sentence.SentenceKind;


public final class DeclarativeInterrogativeSentence
		extends DeclarativeSentence {

	private final MemberRegistry memberRegistry;

	public DeclarativeInterrogativeSentence(
			LocationInfo location,
			DeclarativeBlock block) {
		super(location, block, DECLARATIVE_INTERROGATION_FACTORY);
		this.memberRegistry =
				new InterrogativeMemberRegistry(block.getMemberRegistry());
	}

	@Override
	public SentenceKind getKind() {
		return SentenceKind.INTERROGATIVE_SENTENCE;
	}

	@Override
	public MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

}
