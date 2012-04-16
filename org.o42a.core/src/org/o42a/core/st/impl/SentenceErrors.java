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
package org.o42a.core.st.impl;

import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.DefTargets;
import org.o42a.util.log.LogInfo;


public final class SentenceErrors {

	public static void prohibitedIssueBraces(
			CompilerLogger logger,
			LogInfo location) {
		logger.error(
				"prohibited_issue_braces",
				location,
				"Issue can not contain braces");
	}

	public static void declarationNotAlone(
			CompilerLogger logger,
			DefTargets targets) {
		if (targets.haveClause()) {
			logger.error(
					"not_alone_clause",
					targets,
					"Clause declaration is not"
					+ " the only statement of sentence");
		} else {
			logger.error(
					"not_alone_field",
					targets,
					"Field declaration is not"
					+ " the only statement of sentence");
		}
	}

	private SentenceErrors() {
	}

}
