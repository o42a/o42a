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
package org.o42a.compiler.ip.access;

import static org.o42a.compiler.ip.access.AccessRules.ACCESS_FROM_PLACEMENT;

import org.o42a.compiler.ip.file.OtherContextDistributor;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.AccessSource;
import org.o42a.core.source.CompilerContext;
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

	public final AccessDistributor fromType() {

		final AccessRules accessRules = getAccessRules();
		final AccessRules typeRules = accessRules.typeRules();

		if (accessRules == typeRules) {
			return this;
		}

		return typeRules.distribute(this.distributor);
	}

	public final AccessDistributor fromDeclaration() {

		final AccessRules accessRules = getAccessRules();
		final AccessRules declarationRules = accessRules.declarationRules();

		if (accessRules == declarationRules) {
			return this;
		}

		return declarationRules.distribute(this.distributor);
	}

	public final AccessDistributor fromClauseReuse() {

		final AccessRules accessRules = getAccessRules();
		final AccessRules declarationRules = accessRules.clauseReuseRules();

		if (accessRules == declarationRules) {
			return this;
		}

		return declarationRules.distribute(this.distributor);
	}

	public final AccessDistributor fromPlacement() {
		if (getAccessRules() == ACCESS_FROM_PLACEMENT) {
			return this;
		}
		return ACCESS_FROM_PLACEMENT.distribute(this);
	}

	public final AccessDistributor distributeAccessIn(Container container) {

		final Distributor distributor =
				this.distributor.distributeIn(container);

		if (this.distributor == distributor) {
			return this;
		}

		return getAccessRules().distribute(distributor);
	}

	public final AccessDistributor distributeIn(CompilerContext context) {
		if (getContext() == context) {
			return this;
		}
		return getAccessRules().distribute(
				OtherContextDistributor.distributeIn(
						this.distributor,
						context));
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
