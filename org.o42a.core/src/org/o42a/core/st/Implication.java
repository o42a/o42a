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
package org.o42a.core.st;

import org.o42a.core.PlaceInfo;
import org.o42a.core.Scope;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.ValueStruct;


public interface Implication<L extends Implication<L>> extends PlaceInfo {

	Statement getStatement();

	DefinitionTargets getDefinitionTargets();

	ValueStruct<?, ?> valueStruct(Scope scope);

	/**
	 * Called to replace the statement with another one.
	 *
	 * <p>Supported only for inclusion statement.<p>
	 *
	 * @param statement replacement statement.
	 *
	 * @return replacement definer.
	 */
	L replaceWith(Statement statement);

	Instruction toInstruction(Resolver resolver);

}
