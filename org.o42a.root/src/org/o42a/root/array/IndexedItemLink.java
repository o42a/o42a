/*
    Root Object Definition
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.root.array;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.link.*;


final class IndexedItemLink extends KnownLink {

	IndexedItemLink(
			LocationInfo location,
			Distributor distributor,
			TargetRef targetRef) {
		super(location, distributor, targetRef);
	}

	private IndexedItemLink(IndexedItemLink prototype, TargetRef targetRef) {
		super(prototype, targetRef);
	}

	@Override
	protected KnownLink prefixWith(PrefixPath prefix) {
		return new IndexedItemLink(this, getTargetRef().prefixWith(prefix));
	}

	@Override
	public LinkValueType getValueType() {
		return LinkValueType.LINK;
	}

	@Override
	protected Link findLinkIn(Scope enclosing) {

		final TargetRef targetRef = getTargetRef().upgradeScope(enclosing);

		return new IndexedItemLink(this, targetRef);
	}

}
