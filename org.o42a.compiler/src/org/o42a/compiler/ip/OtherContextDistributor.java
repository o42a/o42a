/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.ip;

import org.o42a.core.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.log.Loggable;


public class OtherContextDistributor extends Distributor {

	private final CompilerContext context;
	private final Distributor distributor;

	public OtherContextDistributor(
			CompilerContext context,
			Distributor distributor) {
		this.context = context;
		this.distributor = distributor;
	}

	@Override
	public CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Loggable getLoggable() {
		return this.distributor.getLoggable();
	}

	@Override
	public ScopePlace getPlace() {
		return this.distributor.getPlace();
	}

	@Override
	public Container getContainer() {
		return this.distributor.getContainer();
	}

	@Override
	public Scope getScope() {
		return this.distributor.getScope();
	}

}
