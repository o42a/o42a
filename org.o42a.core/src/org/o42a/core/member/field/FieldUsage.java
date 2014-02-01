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
package org.o42a.core.member.field;

import org.o42a.analysis.use.*;


public class FieldUsage extends Usage<FieldUsage> {

	public static final AllUsages<FieldUsage> ALL_FIELD_USAGES =
			new AllUsages<>(FieldUsage.class);

	public static final FieldUsage FIELD_ACCESS =
			new FieldUsage("FieldAccess");
	public static final FieldUsage SUBSTANCE_USAGE =
			new FieldUsage("FieldSubstance");
	public static final FieldUsage NESTED_USAGE =
			new FieldUsage("NestedFields");

	public static final Uses<FieldUsage> alwaysUsed() {
		return ALL_FIELD_USAGES.alwaysUsed();
	}

	public static final Uses<FieldUsage> neverUsed() {
		return ALL_FIELD_USAGES.neverUsed();
	}

	public static final Usable<FieldUsage> usable(Object used) {
		return ALL_FIELD_USAGES.usable(used);
	}

	public static final Usable<FieldUsage> usable(
			String name,
			Object used) {
		return ALL_FIELD_USAGES.usable(name, used);
	}

	private FieldUsage(String name) {
		super(ALL_FIELD_USAGES, name);
	}

}
