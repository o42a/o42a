/*
    Test Framework
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.lib.test.run;

import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.lib.test.run.ObjectTestsRunner.runObjectTests;

import org.o42a.common.intrinsic.IntrinsicDirective;
import org.o42a.core.Location;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;
import org.o42a.lib.test.TestModule;
import org.o42a.util.log.LoggableData;


public class RunTests extends IntrinsicDirective {

	private final TestModule module;

	public RunTests(TestModule module) {
		super(
				fieldDeclaration(
						new Location(
								module.getContext(),
								new LoggableData("<run tests>")),
						module.distribute(),
						MemberId.memberName("run_tests"))
				.prototype());
		this.module = module;
	}

	@Override
	public <S extends Statements<S>> void apply(Block<S> block, Ref directive) {

		final S statements = block.propose(directive).alternative(directive);

		statements.expression(runObjectTests(
				directive,
				statements.nextDistributor(),
				this.module));
	}

	@Override
	protected void postResolve() {
		super.postResolve();
		includeSource();
	}

}
