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

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


@SupportedAnnotationTypes("org.o42a.common.object.SourcePath")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SourcePathAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(
			Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {

		final Iterator<? extends TypeElement> iterator =
				annotations.iterator();

		if (!iterator.hasNext()) {
			return false;
		}

		final TypeElement annotation = iterator.next();
		final TypesWithSources types =
				new TypesWithSources(this.processingEnv);

		for (Element annotated
				: roundEnv.getElementsAnnotatedWith(annotation)) {

			final TypeElement type = (TypeElement) annotated;

			types.processAnnotations(type);
		}

		try {
			types.emitDescriptor();
		} catch (IOException e) {
			this.processingEnv.getMessager().printMessage(
					Diagnostic.Kind.ERROR,
					"I/O error: " + e.getMessage());
		}

		return true;
	}

}
