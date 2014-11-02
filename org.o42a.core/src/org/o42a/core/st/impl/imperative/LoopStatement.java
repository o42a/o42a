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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.object.def.EscapeMode.ESCAPE_IMPOSSIBLE;

import org.o42a.core.Distributor;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.string.Name;


public final class LoopStatement extends Statement {

	private final Name name;
	private final boolean exit;

	public LoopStatement(
			LocationInfo location,
			Statements enclosing,
			Name name) {
		super(location, enclosing.nextDistributor());
		this.name = name;
		this.exit = enclosing.getSentence().getKind().isExclamatory();
	}

	private LoopStatement(
			LoopStatement prototype,
			Distributor distributor) {
		super(prototype, distributor);
		this.name = prototype.name;
		this.exit = prototype.exit;
	}

	public final Name getName() {
		return this.name;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public EscapeMode getEscapeMode() {
		return ESCAPE_IMPOSSIBLE;
	}

	@Override
	public Command command(CommandEnv env) {
		if (this.exit) {
			return new LoopCommand.ExitCommand(this, env);
		}
		return new LoopCommand.RepeatCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new LoopStatement(this, reproducer.distribute());
	}

	@Override
	public String toString() {
		if (!this.exit) {
			if (this.name == null) {
				return "(...)";
			}
			return "(... " + this.name + ')';
		}
		if (this.name == null) {
			return "(!..)";
		}
		return "(!.." + this.name + ')';
	}

}
