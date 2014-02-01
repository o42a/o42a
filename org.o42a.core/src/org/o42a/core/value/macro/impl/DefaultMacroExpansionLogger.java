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
package org.o42a.core.value.macro.impl;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.source.ScopedLogger;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


public class DefaultMacroExpansionLogger extends ScopedLogger {

	public static final DefaultMacroExpansionLogger INSTANCE =
			new DefaultMacroExpansionLogger();

	private DefaultMacroExpansionLogger() {
	}

	@Override
	public void log(
			Scope scope,
			Logger logger,
			LogRecord message) {
		if (isInsidePrototype(scope)) {
			// Do not report an error when expansion happens inside prototype.
			return;
		}
		logger.log(message);
	}

	private boolean isInsidePrototype(Scope scope) {

		final Obj object = scope.toObject();

		if (object != null) {
			if (object.isPrototype() || object.isAbstract()) {
				return true;
			}
		} else if (scope.isTopScope()) {
			return false;
		}

		return isInsidePrototype(scope.getEnclosingScope());
	}

}
