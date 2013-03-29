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
import static org.o42a.core.member.AccessSource.FROM_DEFINITION;

import org.o42a.core.*;
import org.o42a.core.member.AccessSource;
import org.o42a.core.source.Location;


public final class AccessDistributor extends Distributor {

	public static AccessDistributor accessDistributor(
			Distributor distributor,
			AccessSource accessSource) {
		if (distributor.getClass() != AccessDistributor.class) {
			return new AccessDistributor(distributor, accessSource);
		}
		return ((AccessDistributor) distributor).setAccessSource(accessSource);
	}

	public static AccessDistributor fromDeclaration(Distributor distributor) {
		return accessDistributor(distributor, FROM_DECLARATION);
	}

	public static AccessDistributor fromDefinition(Distributor distributor) {
		return accessDistributor(distributor, FROM_DEFINITION);
	}

	public static AccessDistributor fromDeclaration(ContainerInfo contained) {
		return fromDeclaration(contained.distribute());
	}

	public static AccessDistributor fromDefinition(ContainerInfo contained) {
		return fromDefinition(contained.distribute());
	}

	private final Distributor distributor;
	private final AccessSource accessSource;

	private AccessDistributor(
			Distributor distributor,
			AccessSource accessSource) {
		this.distributor = distributor;
		this.accessSource = accessSource;
	}

	public final AccessSource getAccessSource() {
		return this.accessSource;
	}

	public final AccessDistributor setAccessSource(AccessSource accessSource) {
		if (this.accessSource == accessSource) {
			return this;
		}
		return new AccessDistributor(this.distributor, accessSource);
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
