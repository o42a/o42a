/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.field;

import static org.o42a.compiler.ip.type.TypeConsumer.typeConsumer;

import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;


public final class FieldNesting implements Nesting {

	private final FieldDeclaration declaration;

	public FieldNesting(FieldDeclaration declaration) {
		this.declaration = declaration;
	}

	@Override
	public Obj findObjectIn(Scope enclosing) {
		return findInScope(enclosing);
	}

	public final TypeConsumer toTypeConsumer() {
		return typeConsumer(this);
	}

	@Override
	public String toString() {
		if (this.declaration == null) {
			return super.toString();
		}
		return this.declaration.toString();
	}

	private Obj findInScope(Scope enclosing) {
		// Delay the field key construction until it's really required.
		return this.declaration.getFieldKey().findObjectIn(enclosing);
	}

}
