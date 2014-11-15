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

import java.util.function.BiFunction;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.core.object.Obj;


public interface DetectEscapeFlag
		extends BiFunction<EscapeAnalyzer, Obj, EscapeFlag> {

	DetectEscapeFlag OWN_ESCAPE_MODE =
			(analyzer, obj) -> obj.analysis().ownEscapeFlag(analyzer);

	DetectEscapeFlag ANCESTOR_ESCAPE_MODE = new DetectEscapeFlag() {

		@Override
		public boolean objectDefinitionsIgnored() {
			return true;
		}

		@Override
		public EscapeFlag apply(EscapeAnalyzer analyzer, Obj object) {
			return object.analysis().ancestorEscapeFlag(analyzer);
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
