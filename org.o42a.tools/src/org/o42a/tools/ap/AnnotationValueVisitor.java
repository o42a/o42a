/*
    Build Tools
    Copyright (C) 2014 Ruslan Lopatin

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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;


public class AnnotationValueVisitor
		extends SimpleAnnotationValueVisitor8<AnnotationMirror, Void> {

	private static final AnnotationValueVisitor VISITOR =
			new AnnotationValueVisitor();

	public static AnnotationMirror annotationValue(
			AnnotationValue annotationValue) {
		return annotationValue.accept(VISITOR, null);
	}

	@Override
	public AnnotationMirror visitAnnotation(AnnotationMirror a, Void p) {
		return a;
	}

	@Override
	protected AnnotationMirror defaultAction(Object o, Void p) {
		return null;
	}

}
