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

import org.o42a.core.Scope;
import org.o42a.core.object.macro.impl.DefaultMacroExpansionLogger;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogRecord;


/**
 * Macro expansion error logger.
 *
 * <p>It should be provided by {@link MacroConsumer#getExpansionLogger() macro
 * consumer} in order to conditionally {@link MacroExpander#getLogger() log
 * errors} during expansion.</p>
 */
public interface MacroExpansionLogger {

	/**
	 * Default macro expansion error logger. It does not report the expansion
	 * errors when it happens inside the prototype object.
	 */
	MacroExpansionLogger DEFAULT_MACRO_EXPANSION_LOGGER =
			DefaultMacroExpansionLogger.INSTANCE;

	/**
	 * Reports the macro expansion error.
	 *
	 * <p>This method is called by {@link MacroExpander#getLogger() macro
	 * expansion logger} and may conditionally log the error.<p>
	 *
	 * @param scope scope in which the macro expansion happens.
	 * @param logger logger to log error to.
	 * @param error reported error message.
	 */
	void logExpansionError(Scope scope, CompilerLogger logger, LogRecord error);

}
