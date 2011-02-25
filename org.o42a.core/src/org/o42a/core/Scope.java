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
package org.o42a.core;

import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;


public interface Scope extends PlaceInfo {

	boolean isTopScope();

	Scope getEnclosingScope();

	Container getEnclosingContainer();

    Path getEnclosingScopePath();

	Field<?> toField();

	LocalScope toLocal();

	boolean isRuntime();

	boolean derivedFrom(Scope other);

	CompilerLogger getLogger();

	Path pathTo(Scope targetScope);

	Rescoper rescoperTo(Scope toScope);

	boolean contains(Scope other);

	ScopeIR ir(IRGenerator generator);

	void assertDerivedFrom(Scope other);

}
