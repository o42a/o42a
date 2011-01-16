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
package org.o42a.core.st.sentence;

import org.o42a.core.LocationSpec;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.MemberRegistry;
import org.o42a.core.st.DefinitionTarget;


public abstract class DeclarativeIssue extends DeclarativeSentence {

	private final MemberRegistry memberRegistry;

	public DeclarativeIssue(
			LocationSpec location,
			DeclarativeBlock block,
			DeclarativeFactory sentenceFactory) {
		super(location, block, sentenceFactory);
		this.memberRegistry =
			block.getMemberRegistry().prohibitDeclarations();
	}

	@Override
	public final boolean isClaim() {
		return false;
	}

	@Override
	public boolean isIssue() {
		return true;
	}

	@Override
	public MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

	static final class Claiming extends DeclarativeIssue {

		Claiming(
				LocationSpec location,
				DeclarativeBlock block,
				DeclarativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

		@Override
		protected Definitions define(DefinitionTarget target) {

			final Definitions definitions = super.define(target);

			if (definitions == null) {
				return null;
			}

			return definitions.claim();
		}

	}

	static final class Proposing extends DeclarativeIssue {

		Proposing(
				LocationSpec location,
				DeclarativeBlock block,
				DeclarativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

	}

}
