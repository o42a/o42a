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
package org.o42a.core.object.value;

import org.o42a.analysis.use.*;


public class ValuePartUsage extends Usage<ValuePartUsage> {

	public static final AllUsages<ValuePartUsage> ALL_VALUE_PART_USAGES =
			new AllUsages<>(ValuePartUsage.class);

	public static final ValuePartUsage VALUE_PART_USAGE =
			new ValuePartUsage("ValuePartUsage");
	public static final ValuePartUsage VALUE_PART_ACCESS =
			new ValuePartUsage("ValuePartAccess");

	public static final Uses<ValuePartUsage> alwaysUsed() {
		return ALL_VALUE_PART_USAGES.alwaysUsed();
	}

	public static final Uses<ValuePartUsage> neverUsed() {
		return ALL_VALUE_PART_USAGES.neverUsed();
	}

	public static final Usable<ValuePartUsage> usable(Object used) {
		return ALL_VALUE_PART_USAGES.usable(used);
	}

	public static final Usable<ValuePartUsage> usable(
			String name,
			Object used) {
		return ALL_VALUE_PART_USAGES.usable(name, used);
	}

	private ValuePartUsage(String name) {
		super(ALL_VALUE_PART_USAGES, name);
	}

}
