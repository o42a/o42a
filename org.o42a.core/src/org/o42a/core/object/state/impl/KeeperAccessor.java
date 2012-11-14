/*
    Compiler
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
package org.o42a.core.object.state.impl;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;


public final class KeeperAccessor extends Obj {

	private final Keeper keeper;
	private final Ref value;

	public KeeperAccessor(Keeper keeper) {
		super(
				keeper,
				keeper.distributeIn(keeper.getContainer()));
		this.keeper = keeper;

		final Scope scope = getScope();

		this.value = getKeeper().getValue().rescope(scope);
		setValueStruct(this.value.valueStruct(scope));
	}

	public final Ref getValue() {
		return this.value;
	}

	public final Keeper getKeeper() {
		return this.keeper;
	}

	@Override
	public String toString() {
		if (this.keeper == null) {
			return super.toString();
		}
		return this.keeper.toString();
	}

	@Override
	protected Nesting createNesting() {
		return this.keeper.getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				this.keeper.ancestor(this));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new KeeperAccessDef(this).toDefinitions(type().getParameters());
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();
		this.keeper.resolveAll();
	}

}
