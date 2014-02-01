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

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;


abstract class TypeSource {

	private final TypesWithSources types;
	private TypeSourceName name;
	private TypeElement type;
	private AnnotationMirror annotation;
	private AnnotationValue value;
	private AnnotationValue relativeTo;

	public TypeSource(
			TypesWithSources types,
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo) {
		this.types = types;
		this.name = name;
		this.type = type;
		this.annotation = annotation;
		this.value = value;
		this.relativeTo = relativeTo;
	}

	public final TypeSourceName getName() {
		return this.name;
	}

	public final SourceKind getSourceKind() {
		return getName().getSourceKind();
	}

	public final TypeElement getType() {
		return this.type;
	}

	public final AnnotationMirror getAnnotation() {
		return this.annotation;
	}

	public final AnnotationValue getValue() {
		return this.value;
	}

	public final AnnotationValue getRelativeTo() {
		return this.relativeTo;
	}

	public final TypesWithSources getTypes() {
		return this.types;
	}

	public final ProcessingEnvironment getProcessingEnv() {
		return getTypes().getProcessingEnv();
	}

	public final Messager getMessenger() {
		return getTypes().getMessenger();
	}

	public abstract void addRelatedSources(AnnotationMirror relatedSources);

	public void override(
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo) {
		this.name = name;
		this.type = type;
		this.annotation = annotation;
		this.value = value;
		this.relativeTo = relativeTo;
	}

	@Override
	public String toString() {
		if (this.type == null) {
			return super.toString();
		}
		return this.type.toString();
	}

}
