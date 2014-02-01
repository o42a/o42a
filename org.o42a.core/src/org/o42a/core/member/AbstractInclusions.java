/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public abstract class AbstractInclusions extends Inclusions {

	private HashMap<Name, LocationInfo> inclusions;

	@Override
	public boolean implicitInclusionsSupported() {
		return true;
	}

	@Override
	public boolean hasExplicitInclusions() {
		return this.inclusions != null;
	}

	@Override
	public boolean include(LocationInfo location, Name tag) {
		if (this.inclusions == null) {
			this.inclusions = new HashMap<>();
		}

		final LocationInfo previousLocation =
				this.inclusions.put(tag, location);

		if (previousLocation == null) {
			return true;
		}

		this.inclusions.put(tag, previousLocation);
		location.getLocation().getLogger().error(
				"duplicate_inclusion",
				location.getLocation().addAnother(previousLocation),
				"Section '%s' already included into '%s'",
				tag,
				includedIntoName());

		return false;
	}

	protected abstract String includedIntoName();

}
