/*
    Utilities
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.util;

import java.util.function.Supplier;


public final class Misc {

	public static <T> T coalesce(T arg1, T arg2) {
		return arg1 != null ? arg1 : arg2;
	}

	public static <T> T coalesce(T arg1, Supplier<T> arg2) {
		return arg1 != null ? arg1 : arg2.get();
	}

	private Misc() {
	}

}
