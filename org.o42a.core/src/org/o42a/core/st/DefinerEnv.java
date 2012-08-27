/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.core.value.ValueRequest;


public abstract class DefinerEnv extends ImplicationEnv {

	private static final DefaultEnv DEFAULT_ENV = new DefaultEnv();

	public static DefinerEnv defaultEnv() {
		return DEFAULT_ENV;
	}

	private static final class DefaultEnv extends DefinerEnv {

		@Override
		public String toString() {
			return "DefaultEnv";
		}

		@Override
		protected ValueRequest buildValueRequest() {
			return null;
		}

	}

}
