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
package org.o42a.core.value.link.impl;

import static org.o42a.core.object.def.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;

import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.value.ValueEscapeMode;


public class LinkValueEscapeMode implements ValueEscapeMode {

	public static final LinkValueEscapeMode LINK_VALUE_ESCAPE_MODE =
			new LinkValueEscapeMode();

	private LinkValueEscapeMode() {
	}

	@Override
	public EscapeMode valueEscapeMode(Obj object) {

		final DefTarget target = object.value().getDefinitions().target();

		if (!target.exists()) {
			return ESCAPE_POSSIBLE;
		}
		if (target.isUnknown()) {
			return ESCAPE_IMPOSSIBLE;
		}

		return target.getRef().escapeMode(
				object.getScope().getEnclosingScope());
	}

	@Override
	public String toString() {
		return "LINK_VALUE_ESCAPE_MODE";
	}

}
