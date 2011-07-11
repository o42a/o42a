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

import java.util.HashMap;

import org.o42a.core.LocationInfo;


public abstract class AbstractInclusions extends Inclusions {

	private HashMap<String, LocationInfo> inclusions;

	@Override
	public boolean inclusionsSupported() {
		return true;
	}

	@Override
	public boolean hasInclusions() {
		return this.inclusions != null;
	}

	@Override
	public boolean include(LocationInfo location, String tag) {
		if (this.inclusions == null) {
			this.inclusions = new HashMap<String, LocationInfo>();
		}

		final LocationInfo previousLocation =
				this.inclusions.put(tag, location);

		if (previousLocation == null) {
			return true;
		}

		this.inclusions.put(tag, previousLocation);
		location.getContext().getLogger().error(
				"duplicate_inclusion",
				location.getLoggable().setPreviousLoggable(
						previousLocation.getLoggable()),
				"Section '%s' already included into '%s'",
				tag,
				includedIntoName());

		return false;
	}

	protected abstract String includedIntoName();

}
