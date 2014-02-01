/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value.macro;

import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.ScopedLogger;
import org.o42a.core.value.macro.impl.DefaultMacroConsumer;
import org.o42a.core.value.macro.impl.DefaultMacroExpansionLogger;


public interface MacroConsumer {

	Consumer DEFAULT_CONSUMER = DefaultMacroConsumer.INSTANCE;

	MacroConsumer DEFAULT_MACRO_CONSUMER = DefaultMacroConsumer.INSTANCE;

	/**
	 * Default macro expansion error logger. It does not report the expansion
	 * errors when it happens inside the prototype object.
	 */
	ScopedLogger DEFAULT_MACRO_EXPANSION_LOGGER =
			DefaultMacroExpansionLogger.INSTANCE;

	/**
	 * Scoped macro expansion logger.
	 *
	 * <p>It should provided by in order to conditionally
	 * {@link MacroExpander#getLogger() log errors} during expansion.</p>
	 *
	 * @return scoped logger.
	 */
	ScopedLogger getExpansionLogger();

	Ref expandMacro(Ref macroExpansion);

}
