/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.clause;

import static org.o42a.compiler.ip.clause.ClauseAccessRules.clauseAccessRules;

import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.compiler.ip.access.ContainedAccess;
import org.o42a.core.member.clause.ClauseBuilder;


public class ClauseAccess extends ContainedAccess<ClauseBuilder> {

	ClauseAccess(AccessRules rules, ClauseBuilder target) {
		super(
				clauseAccessRules(target.getDeclaration(), rules),
				target);
	}

}
