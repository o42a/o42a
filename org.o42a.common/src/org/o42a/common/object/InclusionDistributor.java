/*
    Modules Commons
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.common.object;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.log.Loggable;


final class InclusionDistributor extends Distributor {

	private final Obj object;
	private final Namespace namespace;

	InclusionDistributor(Obj object) {
		this.object = object;
		this.namespace = new Namespace(this, object);
	}

	@Override
	public Loggable getLoggable() {
		return this.object.getLoggable();
	}

	@Override
	public CompilerContext getContext() {
		return this.object.getContext();
	}

	@Override
	public ScopePlace getPlace() {
		return this.object.getPlace();
	}

	@Override
	public Container getContainer() {
		return this.namespace;
	}

	@Override
	public Scope getScope() {
		return this.object.getScope();
	}

}
