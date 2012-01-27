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
package org.o42a.core.artifact.object;

import org.o42a.util.use.*;


public class ValueUsage extends Usage<ValueUsage> {

	public static final AllUsages<ValueUsage> ALL_VALUE_USAGES =
			new AllUsages<ValueUsage>(ValueUsage.class);

	public static final ValueUsage STATIC_VALUE_USAGE =
			new ValueUsage("StaticValue");
	public static final ValueUsage RUNTIME_VALUE_USAGE =
			new ValueUsage("RuntimeValue");
	public static final ValueUsage EXPLICIT_STATIC_VALUE_USAGE =
			new ValueUsage("ExplicitStaticValue");
	public static final ValueUsage EXPLICIT_RUNTINE_VALUE_USAGE =
			new ValueUsage("ExplicitRuntimeValue");

	public static final Uses<ValueUsage> alwaysUsed() {
		return ALL_VALUE_USAGES.alwaysUsed();
	}

	public static final Uses<ValueUsage> neverUsed() {
		return ALL_VALUE_USAGES.neverUsed();
	}

	public static final Usable<ValueUsage> usable(Object used) {
		return ALL_VALUE_USAGES.usable(used);
	}

	public static final Usable<ValueUsage> usable(
			String name,
			Object used) {
		return ALL_VALUE_USAGES.usable(name, used);
	}

	private ValueUsage(String name) {
		super(ALL_VALUE_USAGES, name);
	}

}
