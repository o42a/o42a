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
package org.o42a.core.st.impl;

import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandTargets;


public final class SentenceErrors {

	public static void prohibitedInterrogativeField(LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_interrogative_field",
				location,
				"Field declarations prohibited inside interrogative sentences");
	}

	public static void prohibitedInterrogativeClause(LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_interrogative_clause",
				location,
				"Clause declarations prohibited inside"
				+ " interrogative sentences");
	}

	public static void prohibitedInterrogativeBraces(LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_interrogative_braces",
				location,
				"Interrogative sentences can not contain braces");
	}

	public static void prohibitedFlow(LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_flow",
				location,
				"Flow can not be declared here");
	}

	public static void prohibitedInterrogativeAssignment(
			LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_interrogative_assignment",
				location,
				"Assignments are prohibited within interrogative sentences");
	}

	public static void declarationNotAlone(
			CompilerLogger logger,
			CommandTargets targets) {
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
