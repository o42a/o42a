/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static java.util.Collections.emptySet;

import java.util.Set;

import org.o42a.core.member.MemberKey;


final class EmptyDefinitionTargets extends DefinitionTargets {

	EmptyDefinitionTargets() {
		super((byte) 0);
	}

	@Override
	public boolean haveField(MemberKey fieldKey) {
		return false;
	}

	@Override
	Set<DefinitionKey> definitions() {
		return emptySet();
	}

	@Override
	public String toString() {
		return "EmptyDefinitionTargets";
	}

}
