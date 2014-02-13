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

import org.o42a.core.member.Inclusions;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Declaratives;


public class Inclusion extends Statement {

	private final Declaratives statements;

	public Inclusion(LocationInfo location, Declaratives statements) {
		super(location, statements.nextDistributor());
		this.statements = statements;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public final Statement reproduce(Reproducer reproducer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Command command(CommandEnv env) {
		return new InclusionCommand(this, env);
	}

	@Override
	public String toString() {
		return "***";
	}

	final boolean include() {

		final Inclusions inclusions =
				this.statements.getMemberRegistry().inclusions();

		if (!inclusions.hasInclusions()) {
			// Implicit inclusions support status may change
			// as additional field variants may be added at a time
			// after this instruction created.
			return false;
		}
		if (this.statements.isInterrogation()) {
			// Implicit inclusions not supported
			// inside interrogative sentences.
			return false;
		}

		return true;
	}

}
