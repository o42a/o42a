/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.core.value.ValueRequest.noValueRequest;

import org.o42a.core.source.CompilerLogger;
import org.o42a.core.value.ValueRequest;


public abstract class DefinerEnv extends ImplicationEnv {

	public static DefinerEnv defaultEnv(CompilerLogger logger) {
		return new DefaultEnv(logger);
	}

	private static final class DefaultEnv extends DefinerEnv {

		private final ValueRequest valueRequest;

		DefaultEnv(CompilerLogger logger) {
			this.valueRequest = noValueRequest(logger);
		}

		@Override
		public ValueRequest getValueRequest() {
			return this.valueRequest;
		}

		@Override
		public String toString() {
			return "DefaultEnv";
		}

	}

}
