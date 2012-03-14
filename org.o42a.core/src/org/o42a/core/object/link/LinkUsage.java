/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.link;

import org.o42a.analysis.use.*;


public class LinkUsage extends Usage<LinkUsage> {

	public static final AllUsages<LinkUsage> ALL_LINK_USAGES =
			new AllUsages<LinkUsage>(LinkUsage.class);

	/**
	 * The link is explicitly derived.
	 */
	public static final LinkUsage LINK_DERIVATION =
			new LinkUsage("LinkDerivation");

	/**
	 * Field changes derived from ascendant.
	 */
	public static final LinkUsage DERIVED_LINK_FIELD_CHANGES =
			new LinkUsage("DerivedLinkFieldChanges");

	/**
	 * Fields changed. I.e. new field declared or existing field overridden.
	 */
	public static final LinkUsage LINK_FIELD_CHANGES =
			new LinkUsage("LinkFieldChanges");

	public static final UseSelector<LinkUsage> DERIVED_LINK_USE_SELECTOR =
			DERIVED_LINK_FIELD_CHANGES.or(LINK_FIELD_CHANGES);

	public static final Usable<LinkUsage> usable(Object used) {
		return ALL_LINK_USAGES.usable(used);
	}

	public static final Usable<LinkUsage> usable(
			String name,
			Object used) {
		return ALL_LINK_USAGES.usable(name, used);
	}

	private LinkUsage(String name) {
		super(ALL_LINK_USAGES, name);
	}

}
