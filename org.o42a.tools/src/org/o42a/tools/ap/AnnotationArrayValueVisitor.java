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

import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;


final class AnnotationArrayValueVisitor
		extends SimpleAnnotationValueVisitor6<
				List<? extends AnnotationValue>,
				Void> {

	private static final AnnotationArrayValueVisitor VISITOR =
			new AnnotationArrayValueVisitor();

	public static List<? extends AnnotationValue> annotationArrayValue(
			AnnotationValue value) {
		return value.accept(VISITOR, null);
	}

	private AnnotationArrayValueVisitor() {
	}

	@Override
	public List<? extends AnnotationValue> visitArray(
			List<? extends AnnotationValue> vals,
			Void p) {
		return vals;
	}

	@Override
	protected List<? extends AnnotationValue> defaultAction(Object o, Void p) {
		return null;
	}

}
