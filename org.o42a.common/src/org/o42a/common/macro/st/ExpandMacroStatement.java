/*
    Compiler Commons
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.common.macro.st;

import org.o42a.core.ref.Ref;
import org.o42a.core.st.*;


final class ExpandMacroStatement extends Statement {

	private final Ref expansion;

	ExpandMacroStatement(Ref expansion) {
		super(expansion, expansion.distribute());
		this.expansion = expansion;
	}

	public final Ref getExpansion() {
		return this.expansion;
	}

	@Override
	public boolean isValid() {
		return getExpansion().isValid();
	}

	@Override
	public Definer define(CommandEnv env) {
		return new ExpandMacroDefiner(this, env);
	}

	@Override
	public Command command(CommandEnv env) {
		return new ExpandMacroCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref expansion = this.expansion.reproduce(reproducer);

		if (expansion == null) {
			return null;
		}

		return new ExpandMacroStatement(expansion);
	}

	@Override
	public String toString() {
		if (this.expansion == null) {
			return super.toString();
		}
		return '=' + this.expansion.toString();
	}

}
