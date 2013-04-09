/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.core.source.LocationInfo;
import org.o42a.core.source.SectionTag;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.util.string.Name;


public class ExplicitInclusion extends Inclusion {

	private final Name tag;

	public ExplicitInclusion(
			LocationInfo location,
			Declaratives statements,
			Name tag) {
		super(location, statements);
		this.tag = tag;
	}

	public final Name getTag() {
		return this.tag;
	}

	@Override
	public Command command(CommandEnv env) {
		return new ExplicitInclusionCommand(this, env);
	}

	@Override
	public String toString() {
		if (this.tag == null) {
			return super.toString();
		}
		return "*** " + this.tag + " ***";
	}

	private static final class ExplicitInclusionCommand
			extends InclusionCommand<ExplicitInclusion> {

		ExplicitInclusionCommand(ExplicitInclusion inclusion, CommandEnv env) {
			super(inclusion, env);
		}

		@Override
		protected void includeInto(DeclarativeBlock block) {

			final SectionTag tag =
					getContext()
					.getSectionTag().
					append(getInclusion().getTag());

			getContext().include(block, tag);
		}


	}

}
