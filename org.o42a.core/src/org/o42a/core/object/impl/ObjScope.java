/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.object.impl;

import org.o42a.codegen.Generator;
import org.o42a.core.Distributor;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.object.impl.ObjectScopeIR;
import org.o42a.core.object.common.StandaloneObjectScope;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;


public final class ObjScope extends StandaloneObjectScope {

	private final ID id;

	public ObjScope(LocationInfo location, Distributor enclosing) {
		super(location, enclosing);
		this.id = enclosing.getScope().nextAnonymousId();
	}

	@Override
	public final ID getId() {
		return this.id;
	}

	@Override
	protected ScopeIR createIR(Generator generator) {
		return new ObjectScopeIR(generator, toObject());
	}

}
