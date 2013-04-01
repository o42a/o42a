/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import org.o42a.core.member.AccessSource;


final class SimpleAccessRules extends AccessRules {

	SimpleAccessRules(AccessSource source) {
		super(source);
	}

	@Override
	public AccessRules setSource(AccessSource source) {
		if (getSource().ordinal() <= source.ordinal()) {
			return this;
		}
		return new SimpleAccessRules(source);
	}

	@Override
	public String toString() {

		final AccessSource source = getSource();

		if (source == null) {
			return super.toString();
		}

		return source.toString();
	}

}
