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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


final class RelTypeSource extends TypeSource {

	private final String restPath;
	private AnnotationMirror relatedSources;

	RelTypeSource(
			TypesWithSources types,
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo,
			String restPath) {
		super(types, name, type, annotation, value, relativeTo);
		this.restPath = restPath;
	}

	public String getRestPath() {
		return this.restPath;
	}

	public final AnnotationMirror getRelatedSources() {
		return this.relatedSources;
	}

	@Override
	public final void addRelatedSources(AnnotationMirror relatedSources) {
		this.relatedSources = relatedSources;
	}

	public void reportUnrelated() {
		getMessenger().printMessage(
				Diagnostic.Kind.ERROR,
				"The class this source is relative to does not have a source",
				getType(),
				getAnnotation(),
				getRelativeTo());
	}

}
