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

import static org.o42a.lib.test.run.ObjectTestsRunner.runObjectTests;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.DirectiveObject;
import org.o42a.common.object.SourcePath;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.Statements;
import org.o42a.lib.test.TestModule;


@SourcePath(relativeTo = TestModule.class, value = "run_tests.o42a")
public class RunTests extends DirectiveObject {

	public RunTests(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public void apply(Ref directive, InstructionContext context) {

		final TestModule module =
				(TestModule) getField().getEnclosingScope().toObject();
		final Statements<?> statements =
			context.getBlock().propose(directive).alternative(directive);

		statements.expression(runObjectTests(
				directive,
				statements.nextDistributor(),
				module));
	}

}
