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
