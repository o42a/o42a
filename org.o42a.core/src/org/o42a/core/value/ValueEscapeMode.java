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
package org.o42a.core.value;

import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;

import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.EscapeMode;


/**
 * Object value escape mode detector.
 */
public interface ValueEscapeMode {

	/**
	 * The values of the type with this escape mode allow the escaping
	 * of object references.
	 */
	ValueEscapeMode VALUE_ESCAPE_POSSIBLE = new ValueEscapeMode() {

		@Override
		public EscapeMode valueEscapeMode(Obj object) {
			return ESCAPE_POSSIBLE;
		}

		@Override
		public String toString() {
			return "VALUE_ESCAPE_POSSIBLE";
		}

	};

	/**
	 * The object references escaping is possible only if the value
	 * definition {@link Definitions#getEscapeMode()} allows value escaping.
	 */
	ValueEscapeMode DEFINITIONS_VALUE_ESCAPE = new ValueEscapeMode() {

		@Override
		public EscapeMode valueEscapeMode(Obj object) {
			return object.value().getDefinitions().getEscapeMode();
		}

		@Override
		public String toString() {
			return "DEFINITIONS_VALUE_ESCAPE";
		}

	};

	/**
	 * Detects an escape mode of the object value.
	 *
	 * @param object an object, which value's escape mode to detect.
	 *
	 * @return value escape mode.
	 */
	EscapeMode valueEscapeMode(Obj object);

}
