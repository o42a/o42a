/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.source;

import org.o42a.core.Container;
import org.o42a.core.Namespace;
import org.o42a.core.object.Obj;


public abstract class Intrinsics {

	private final FullResolution fullResolution = new FullResolution();

	public abstract Obj getVoid();

	public abstract Obj getFalse();

	public abstract Container getTop();

	public abstract Namespace getModuleNamespace();

	public abstract Obj getRoot();

	public abstract Obj getDirective();

	public abstract Obj getInteger();

	public abstract Obj getFloat();

	public abstract Obj getString();

	public abstract Obj getVariableArray();

	public abstract Obj getConstantArray();

	public abstract Module getModule(String moduleId);

	public abstract Module getMainModule();

	public final FullResolution fullResolution() {
		return this.fullResolution;
	}

}
