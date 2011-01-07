/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.artifact.common;

import static org.o42a.core.def.Definitions.emptyDefinitions;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectScope;
import org.o42a.core.def.Definitions;


public abstract class PlainObject extends Obj {

	private Definitions explicitDefinitions;

	public PlainObject(LocationSpec location, Distributor enclosing) {
		super(location, enclosing);
	}

	protected PlainObject(Scope scope) {
		super(scope);
	}

	protected PlainObject(ObjectScope scope) {
		super(scope);
	}

	protected PlainObject(Scope scope, Obj sample) {
		super(scope, sample);
	}

	@Override
	protected Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions) {

		final Definitions explicitDefinitions =
			getExplicitDefinitions().upgradeScope(scope);

		if (ascendantDefinitions == null) {
			return explicitDefinitions;
		}

		return ascendantDefinitions.override(explicitDefinitions);
	}

	protected final Definitions getExplicitDefinitions() {
		if (this.explicitDefinitions == null) {
			this.explicitDefinitions = explicitDefinitions();
			if (this.explicitDefinitions == null) {
				this.explicitDefinitions = emptyDefinitions(this, getScope());
			}
		}
		return this.explicitDefinitions;
	}

	protected abstract Definitions explicitDefinitions();



}
