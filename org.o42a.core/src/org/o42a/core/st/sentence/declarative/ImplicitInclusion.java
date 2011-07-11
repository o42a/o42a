/*
    Compiler Core
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
package org.o42a.core.st.sentence.declarative;

import static org.o42a.core.SectionTag.DEFAULT_SECTION_TAG;

import org.o42a.core.LocationInfo;
import org.o42a.core.member.Inclusions;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;


public class ImplicitInclusion extends Inclusion {

	private Inclusions inclusions;

	public ImplicitInclusion(LocationInfo location, Declaratives statements) {
		super(location, statements);
		this.inclusions = statements.getMemberRegistry().inclusions();
	}

	@Override
	public String toString() {
		return "***";
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		if (!include()) {
			return SKIP_INSTRUCTION;
		}
		return super.toInstruction(resolver);
	}

	@Override
	protected void includeInto(DeclarativeBlock block) {
		if (!include()) {
			return;
		}
		getContext().include(block, DEFAULT_SECTION_TAG);
	}

	private final boolean include() {
		// Explicit inclusions present.
		// Implicit inclusions shouldn't be handled.
		return !this.inclusions.hasInclusions();
	}

}
