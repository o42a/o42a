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

import org.o42a.codegen.Generator;
import org.o42a.core.artifact.object.ConstructionMode;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;
import org.o42a.util.use.UserInfo;


public interface Scope extends PlaceInfo, UserInfo {

	boolean isTopScope();

	Scope getEnclosingScope();

	Container getEnclosingContainer();

    Path getEnclosingScopePath();

    Member toMember();

    Field<?> toField();

	LocalScope toLocal();

	ConstructionMode getConstructionMode();

	boolean derivedFrom(Scope other);

	CompilerLogger getLogger();

	Path pathTo(Scope targetScope);

	Rescoper rescoperTo(Scope toScope);

	boolean contains(Scope other);

	ScopeIR ir(Generator generator);

	void assertDerivedFrom(Scope other);

}
