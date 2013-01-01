/*
    Build Tools
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;


final class AnnotationTypeValueVisitor
		extends SimpleAnnotationValueVisitor6<TypeMirror, Void> {

	private static final AnnotationTypeValueVisitor VISITOR =
			new AnnotationTypeValueVisitor();

	public static TypeMirror annotationTypeValue(
			AnnotationValue annotationValue) {
		return annotationValue.accept(VISITOR, null);
	}

	private AnnotationTypeValueVisitor() {
	}

	@Override
	public TypeMirror visitType(TypeMirror t, Void p) {
		return t;
	}

	@Override
	protected TypeMirror defaultAction(Object o, Void p) {
		return null;
	}


}
