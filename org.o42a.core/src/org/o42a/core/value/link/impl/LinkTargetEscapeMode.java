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

import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.EscapeMode;
import org.o42a.core.value.ValueEscapeMode;


public class LinkTargetEscapeMode implements ValueEscapeMode {

	public static final LinkTargetEscapeMode LINK_TARGET_ESCAPE_MODE =
			new LinkTargetEscapeMode();

	private LinkTargetEscapeMode() {
	}

	@Override
	public EscapeMode valueEscapeMode(Obj object) {
		return object.value()
				.getDefinitions()
				.target()
				.escapeMode(object.getScope().getEnclosingScope());
	}

	@Override
	public String toString() {
		return "LINK_TARGET_ESCAPE_MODE";
	}

}
