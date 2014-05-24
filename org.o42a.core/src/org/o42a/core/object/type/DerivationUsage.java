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
package org.o42a.core.object.type;

import org.o42a.analysis.use.*;


public final class DerivationUsage extends Usage<DerivationUsage> {

	public static final AllUsages<DerivationUsage> ALL_DERIVATION_USAGES =
			new AllUsages<>(DerivationUsage.class);

	public static final DerivationUsage DERIVATION_USAGE =
			new DerivationUsage("Derivation");

	public static final Uses<DerivationUsage> alwaysUsed() {
		return ALL_DERIVATION_USAGES.alwaysUsed();
	}

	public static final Uses<DerivationUsage> neverUsed() {
		return ALL_DERIVATION_USAGES.neverUsed();
	}

	public static final Usable<DerivationUsage> usable(Object used) {
		return ALL_DERIVATION_USAGES.usable(used);
	}

	public static final Usable<DerivationUsage> usable(
			String name,
			Object used) {
		return ALL_DERIVATION_USAGES.usable(name, used);
	}

	private DerivationUsage(String name) {
		super(ALL_DERIVATION_USAGES, name);
	}

}
