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
package org.o42a.core.member;

import org.o42a.core.LocationInfo;


public abstract class Inclusions {

	private static final Inclusions NO_DECLARATIONS = new NoDeclarations();
	private static final Inclusions NO_INCLUSIONS = new NoInclusions();

	public static Inclusions noDeclarations() {
		return NO_DECLARATIONS;
	}

	public static Inclusions noInclusions() {
		return NO_INCLUSIONS;
	}

	public abstract boolean registerInclusion(
			LocationInfo location,
			String tag);

	private static final class NoDeclarations extends Inclusions {

		@Override
		public boolean registerInclusion(LocationInfo location, String tag) {
			location.getContext().getLogger().prohibitedDeclaration(location);
			return false;
		}

		@Override
		public String toString() {
			return "NoDeclarations";
		}

	}

	private static final class NoInclusions extends Inclusions {

		@Override
		public boolean registerInclusion(LocationInfo location, String tag) {
			location.getContext().getLogger().error(
					"prohibited_inclusion",
					location,
					"Inclusions not allowed here");
			return false;
		}

		@Override
		public String toString() {
			return "NoInclusions";
		}

	}

}
