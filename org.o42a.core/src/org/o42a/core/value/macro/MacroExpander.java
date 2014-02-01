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

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.source.ScopedLogger;


/**
 * The macro expansion context.
 *
 * <p>This is passed to {@link Macro macro} expansion methods whenever
 * expansion required.</p>
 */
public interface MacroExpander extends LocationInfo {

	/**
	 * The scope the macro expansion occurs in.
	 *
	 * @return expansion scope.
	 */
	Scope getScope();

	/**
	 * The reference to expanded macro.
	 *
	 * @return macro reference.
	 */
	Ref getMacroRef();

	/**
	 * The resolved macro object.
	 *
	 * @return the object this macro is value of.
	 */
	Obj getMacroObject();

	/**
	 * Conditional logger.
	 *
	 * <p>This logger uses the {@link MacroConsumer#getExpansionLogger()
	 * expansion logger} to conditionally log the messages with
	 * {@link #getExplicitLogger() explicit logger}.</p>
	 *
	 * @return logger.
	 */
	CompilerLogger getLogger();

	/**
	 * Returns a macro expansion logger, which is used to construct the
	 * {@link #getLogger() conditional logger}.
	 *
	 * @return scoped logger.
	 */
	ScopedLogger getExpansionLogger();

	/**
	 * Explicit logger.
	 *
	 * <p>Unlike a {@link #getLogger() conditional logger}, this one always logs
	 * the messages.</p>
	 *
	 * @return logger.
	 */
	CompilerLogger getExplicitLogger();

}
