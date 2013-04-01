/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref;

import static org.o42a.core.member.AccessSource.FROM_DECLARATION;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.AccessSource;
import org.o42a.core.source.Location;


public final class AccessDistributor extends Distributor {

	private final Distributor distributor;
	private final AccessRules accessRules;

	AccessDistributor(Distributor distributor, AccessRules accessRules) {
		this.distributor = distributor;
		this.accessRules = accessRules;
	}

	public final AccessRules getAccessRules() {
		return this.accessRules;
	}

	public final AccessDistributor setAccessRules(AccessRules accessRules) {
		if (this.accessRules == accessRules) {
			return this;
		}
		return new AccessDistributor(this.distributor, accessRules);
	}

	public final AccessSource getAccessSource() {
		return getAccessRules().getSource();
	}

	public final AccessDistributor setAccessSource(AccessSource accessSource) {

		final AccessRules oldRules = getAccessRules();
		final AccessRules newRules = oldRules.setSource(accessSource);

		if (oldRules == newRules) {
			return this;
		}

		return new AccessDistributor(this.distributor, newRules);
	}

	public final AccessDistributor fromDeclaration() {
		return setAccessSource(FROM_DECLARATION);
	}

	@Override
	public final Container getContainer() {
		return this.distributor.getContainer();
	}

	@Override
	public final Scope getScope() {
		return this.distributor.getScope();
	}

	@Override
	public final Location getLocation() {
		return this.distributor.getLocation();
	}

	@Override
	public String toString() {
		if (this.distributor == null) {
			return super.toString();
		}
		return this.distributor.toString();
	}

}
