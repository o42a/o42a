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

import org.o42a.codegen.code.Code;
import org.o42a.core.LocationSpec;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.member.MemberRegistry;


abstract class ImperativeIssue extends ImperativeSentence {

	private final MemberRegistry memberRegistry;

	ImperativeIssue(
			LocationSpec location,
			ImperativeBlock block,
			ImperativeFactory sentenceFactory) {
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

	@Override
	protected void allocate(LocalBuilder builder, Code code) {
		// no declarations within issue - nothing to allocate
	}

	static final class Claiming extends ImperativeIssue {

		Claiming(
				LocationSpec location,
				ImperativeBlock block,
				ImperativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

	}

	static final class Proposing extends ImperativeIssue {

		Proposing(
				LocationSpec location,
				ImperativeBlock block,
				ImperativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

	}

}
