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
package org.o42a.core.source;

import org.o42a.core.Scope;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


/**
 * Scoped logger.
 *
 * <p>It can log errors conditionally, based on scope.</p>
 */
public abstract class ScopedLogger {

	/**
	 * Conditionally logs a given message.
	 *
	 * @param scope the scope from which the given message originated from.
	 * @param logger logger the logger to log error to.
	 * @param message a message to log.
	 */
	public abstract void log(Scope scope, Logger logger, LogRecord message);

	public final CompilerLogger compilerLogger(
			Scope scope,
			CompilerLogger logger) {
		return new ScopedCompilerLogger(this, scope, logger);
	}

	final class ScopedCompilerLogger extends CompilerLogger {

		private final ScopedLogger scopedLogger;
		private final Scope scope;

		public ScopedCompilerLogger(
				ScopedLogger scopedLogger,
				Scope scope,
				CompilerLogger logger) {
			super(logger);
			this.scopedLogger = scopedLogger;
			this.scope = scope;
		}

		@Override
		public void log(LogRecord record) {
			this.scopedLogger.log(this.scope, getLogger(), record);
		}

	}

}
