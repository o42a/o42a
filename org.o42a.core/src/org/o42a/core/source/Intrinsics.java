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
package org.o42a.core.source;

import org.o42a.core.Container;
import org.o42a.core.Namespace;
import org.o42a.core.object.Obj;
import org.o42a.util.string.Name;


public interface Intrinsics {

	CompilerLogger getCompilerLogger();

	Obj getVoid();

	Obj getFalse();

	Obj getNone();

	Container getTop();

	Namespace getModuleNamespace();

	Obj getRoot();

	Obj getDirective();

	Obj getMacro();

	Obj getInteger();

	Obj getFloat();

	Obj getString();

	Obj getLink();

	Obj getVariable();

	Obj getArray();

	Obj getRow();

	Obj getFlow();

	Module getModule(Name moduleName);

	Module getMainModule();

	FullResolution fullResolution();

	default boolean is(Intrinsics other) {
		return this == other;
	}

}
