/*
    Compilation Analysis
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
package org.o42a.analysis.use;


public final class SimpleUsage extends Usage<SimpleUsage> {

	public static final AllUsages<SimpleUsage> ALL_SIMPLE_USAGES =
			new AllUsages<>(SimpleUsage.class);

	public static final SimpleUsage SIMPLE_USAGE = new SimpleUsage();

	public static final Uses<SimpleUsage> alwaysUsed() {
		return ALL_SIMPLE_USAGES.alwaysUsed();
	}

	public static final Uses<SimpleUsage> neverUsed() {
		return ALL_SIMPLE_USAGES.neverUsed();
	}

	public static final Usable<SimpleUsage> simpleUsable(Object used) {
		return ALL_SIMPLE_USAGES.usable(used);
	}

	public static final Usable<SimpleUsage> simpleUsable(
			String name,
			Object used) {
		return ALL_SIMPLE_USAGES.usable(name, used);
	}

	private SimpleUsage() {
		super(ALL_SIMPLE_USAGES, "Usage");
	}

}
