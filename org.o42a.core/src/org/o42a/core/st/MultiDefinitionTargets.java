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

import java.util.HashSet;

import org.o42a.core.member.MemberKey;


final class MultiDefinitionTargets extends DefinitionTargets {

	private final HashSet<DefinitionKey> definitions;

	MultiDefinitionTargets(byte mask, HashSet<DefinitionKey> definitions) {
		super(mask);
		this.definitions = definitions;
	}

	@Override
	public boolean haveField(MemberKey memberKey) {
		return this.definitions.contains(memberKey);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "DefinitionTargets[]";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append("DefinitionTargets[");
		for (DefinitionKey key : this.definitions) {
			if (comma) {
				out.append(", ");
			}
			out.append(key);
		}
		out.append(']');

		return out.toString();
	}

	@Override
	HashSet<DefinitionKey> definitions() {
		return this.definitions;
	}

}
