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
package org.o42a.core.object.meta;

import java.util.function.Function;

import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;


public interface DetectEscapeMode extends Function<Obj, EscapeMode> {

	DetectEscapeMode OWN_ESCAPE_MODE = obj -> obj.analysis().ownEscapeMode();

	DetectEscapeMode ANCESTOR_ESCAPE_MODE = new DetectEscapeMode() {

		@Override
		public boolean objectDefinitionsIgnored() {
			return true;
		}

		@Override
		public EscapeMode apply(Obj t) {
			return t.analysis().ancestorEscapeMode();
		}

		@Override
		public String toString() {
			return "ANCESTOR_ESCAPE_MODE";
		}

	};

	default boolean objectDefinitionsIgnored() {
		return false;
	}

}
