/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.member;


/**
 * The source of member access.
 *
 * <p>It affect members visibility. For example, a field declaration can not
 * see locals declared in the owning object's definition.</p>
 */
public enum AccessSource {

	/**
	 * Members accessed from type definition.
	 */
	FROM_TYPE,

	/**
	 * Members accessed from object declaration.
	 *
	 * <p>E.g. field declaration.</p>
	 */
	FROM_DECLARATION,

	/**
	 * Members accessed from clause reuse declaration.
	 */
	FROM_CLAUSE_REUSE,

	/**
	 * Members accessed from value definition.
	 */
	FROM_DEFINITION

}
