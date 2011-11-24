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
package org.o42a.core.artifact.object;

import org.o42a.util.use.*;


public class TypeUsage extends Usage<TypeUsage> {

	public static final AllUsages<TypeUsage> ALL_TYPE_USAGES =
			new AllUsages<TypeUsage>(TypeUsage.class);

	public static final TypeUsage STATIC_TYPE_USAGE =
			new TypeUsage("StaticType");
	public static final TypeUsage RUNTIME_TYPE_USAGE =
			new TypeUsage("RuntimeType");

	public static final Uses<TypeUsage> alwaysUsed() {
		return ALL_TYPE_USAGES.alwaysUsed();
	}

	public static final Uses<TypeUsage> neverUsed() {
		return ALL_TYPE_USAGES.neverUsed();
	}

	public static final Usable<TypeUsage> usable(Object used) {
		return ALL_TYPE_USAGES.usable(used);
	}

	public static final Usable<TypeUsage> usable(
			String name,
			Object used) {
		return ALL_TYPE_USAGES.usable(name, used);
	}

	private TypeUsage(String name) {
		super(ALL_TYPE_USAGES, name);
	}

}
