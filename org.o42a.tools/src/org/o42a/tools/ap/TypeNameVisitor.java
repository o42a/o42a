/*
    Build Tools
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.tools.ap;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;


final class TypeNameVisitor extends SimpleTypeVisitor6<Name, Void> {

	private static final TypeNameVisitor VISITOR = new TypeNameVisitor();

	public static Name typeName(TypeMirror type) {
		return type.accept(VISITOR, null);
	}

	@Override
	public Name visitDeclared(DeclaredType t, Void p) {

		final TypeElement typeElement = (TypeElement) t.asElement();

		return typeElement.getQualifiedName();
	}

	@Override
	protected Name defaultAction(TypeMirror e, Void p) {
		return null;
	}

}
