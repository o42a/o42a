/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ref.impl;

import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;

import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;


public class YieldStatement extends Statement {

	private final Ref value;

	public YieldStatement(LocationInfo location, Ref value) {
		super(location, value.distribute());
		this.value = value;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public EscapeMode getEscapeMode() {
		return ESCAPE_POSSIBLE;
	}

	@Override
	public boolean isValid() {
		return this.value.isValid();
	}

	@Override
	public Command command(CommandEnv env) {
		return new YieldCommand(getValue(), env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {

		final Ref value = this.value.reproduce(reproducer);

		if (value == null) {
			return null;
		}

		return new YieldStatement(this, value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "<<" + this.value;
	}

}
