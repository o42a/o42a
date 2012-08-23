/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.macro;

import org.o42a.core.object.macro.impl.DefaultMacroConsumer;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;


public interface MacroConsumer {

	Consumer DEFAULT_CONSUMER = DefaultMacroConsumer.INSTANCE;

	MacroConsumer DEFAULT_MACRO_CONSUMER = DefaultMacroConsumer.INSTANCE;

	MacroExpansionLogger getExpansionLogger();

	Ref expandMacro(Ref macroExpansion);

}
