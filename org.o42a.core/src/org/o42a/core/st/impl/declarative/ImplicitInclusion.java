/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.core.member.Inclusions;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;


public class ImplicitInclusion extends Inclusion {

	private final Declaratives statements;

	public ImplicitInclusion(LocationInfo location, Declaratives statements) {
		super(location, statements);
		this.statements = statements;
	}

	@Override
	public String toString() {
		return "***";
	}

	@Override
	public Command command(CommandEnv env) {
		return new ImplicitInclusionCommand(this, env);
	}

	private final boolean include() {

		final Inclusions inclusions =
				this.statements.getMemberRegistry().inclusions();

		if (!inclusions.implicitInclusionsSupported()) {
			// Implicit inclusions support status may change
			// as additional field variants may be added at a time
			// after this instruction created.
			return false;
		}
		if (inclusions.hasExplicitInclusions()) {
			// Implicit inclusions won't be performed
			// if at least one implicit inclusion present.
			return false;
		}
		if (this.statements.isInsideIssue()) {
			// Implicit inclusions not supported
			// inside conditional declarations.
			return false;
		}

		return true;
	}

	private static final class ImplicitInclusionCommand
			extends InclusionCommand<ImplicitInclusion> {

		ImplicitInclusionCommand(
				ImplicitInclusion inclusion,
				CommandEnv env) {
			super(inclusion, env);
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			if (!getInclusion().include()) {
				return SKIP_INSTRUCTION;
			}
			return super.toInstruction(resolver);
		}

		@Override
		protected void includeInto(DeclarativeBlock block) {
			if (!getInclusion().include()) {
				return;
			}
			getContext().include(block, IMPLICIT_SECTION_TAG);
		}

	}

}
