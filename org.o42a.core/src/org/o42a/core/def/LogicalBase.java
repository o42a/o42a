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
package org.o42a.core.def;

import static org.o42a.core.def.Def.sourceOf;
import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ref.Logical;


public abstract class LogicalBase extends Scoped {

	public LogicalBase(LocationInfo location, Scope scope) {
		super(location, scope);
	}

	public LogicalDef toLogicalDef() {
		return new SingleLogicalDef(
				sourceOf(this),
				(Logical) this,
				transparentRescoper(getScope()));
	}

	public CondDef toCondDef() {
		return new LogicalCondDef((Logical) this);
	}

}
