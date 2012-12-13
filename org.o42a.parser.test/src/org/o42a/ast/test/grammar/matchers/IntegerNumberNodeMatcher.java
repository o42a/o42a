/*
    Parser Tests
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
package org.o42a.ast.test.grammar.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.Node;
import org.o42a.ast.atom.NumberNode;


public final class IntegerNumberNodeMatcher<T extends Node>
		extends BaseMatcher<T> {

	@SuppressWarnings("rawtypes")
	public static final IntegerNumberNodeMatcher<?> INTEGER_NUMBER =
			new IntegerNumberNodeMatcher();

	private IntegerNumberNodeMatcher() {
	}

	@Override
	public boolean matches(Object item) {
		if (!(item instanceof NumberNode)) {
			return false;
		}

		final NumberNode number = (NumberNode) item;

		return number.getFractional() == null && number.getExponent() == null;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Integer number");
	}

}
