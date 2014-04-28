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

import static org.o42a.tools.ap.AnnotationTypeValueVisitor.annotationTypeValue;
import static org.o42a.tools.ap.TypeNameVisitor.typeName;
import static org.o42a.util.string.StringUtil.removeLeadingChars;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;


public class TypesWithSources {

	static String[] nameAndRest(String path) {

		final int slashIdx = path.indexOf('/');

		if (slashIdx < 0) {
			return new String[] {path, null};
		}

		final String name = path.substring(0, slashIdx + 1);
		final String rest =
				removeLeadingChars(path.substring(slashIdx + 1), '/');

		return new String[] {name, rest.isEmpty() ? null : rest};
	}

	private static final String SOURCE_PATH =
			"org.o42a.common.object.SourcePath";
	private static final String RELATED_SOURCE =
			"org.o42a.common.object.RelatedSource";
	private static final String RELATED_SOURCES =
			"org.o42a.common.object.RelatedSources";

	private static final String RELATIVE_TO = "relativeTo";
	static final String VALUE = "value";

	private final ProcessingEnvironment processingEnv;

	private TypeWithSource module;
	private final HashMap<Name, RelTypeSources> relative = new HashMap<>();

	public TypesWithSources(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	public final ProcessingEnvironment getProcessingEnv() {
		return this.processingEnv;
	}

	public final Messager getMessenger() {
		return getProcessingEnv().getMessager();
	}

	public final TypeWithSource getModule() {
		return this.module;
	}

	public void processAnnotations(TypeElement type) {

		AnnotationMirror sourcePath = null;
		AnnotationMirror relatedSources = null;
		final List<? extends AnnotationMirror> annotations =
				type.getAnnotationMirrors();

		for (AnnotationMirror annotation : annotations) {

			final TypeElement annotationType =
					(TypeElement) annotation.getAnnotationType().asElement();
			final Name annotationName = annotationType.getQualifiedName();

			if (annotationName == null) {
				continue;
			}
			if (annotationName.contentEquals(SOURCE_PATH)) {
				sourcePath = annotation;
				continue;
			}
			if (annotationName.contentEquals(RELATED_SOURCES)) {
				relatedSources = annotation;
			}
			if (annotationName.contentEquals(RELATED_SOURCE)) {
				relatedSources = annotation;
			}
		}

		if (sourcePath == null) {
			return;
		}

		final TypeSource typeSource = processSourcePath(type, sourcePath);

		if (typeSource == null) {
			return;
		}
		if (relatedSources != null) {
			typeSource.addRelatedSources(relatedSources);
		}
	}

	public void emitDescriptor() throws IOException {
		if (!validate()) {
			return;
		}
		this.module.emitDescriptor();
	}

	final String path(
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value) {

		final String pathValue = value.getValue().toString();
		final String path = removeLeadingChars(pathValue, '/');

		if (path != pathValue) {
			getMessenger().printMessage(
					Diagnostic.Kind.WARNING,
					"Source path should be relative",
					type,
					annotation,
					value);
		}

		return path;
	}

	final void registerType(TypeWithSource typeWithSource) {

		final Name typeName = typeWithSource.getType().getQualifiedName();
		final RelTypeSources pending =
				this.relative.put(typeName, typeWithSource);

		if (pending != null) {
			pending.replaceBy(typeWithSource);
		}
	}

	private TypeSource processSourcePath(
			TypeElement type,
			AnnotationMirror annotation) {
		if (type.getNestingKind() != NestingKind.TOP_LEVEL) {
			getMessenger().printMessage(
					Diagnostic.Kind.ERROR,
					"Only top-level classes may be annotated with @"
					+ SOURCE_PATH + " annotation",
					type,
					annotation);
			return null;
		}

		AnnotationValue value = null;
		AnnotationValue relativeTo = null;

		for (Map.Entry<
				? extends ExecutableElement,
				? extends AnnotationValue> e
				: annotation.getElementValues().entrySet()) {

			final ExecutableElement element = e.getKey();
			final Name simpleName = element.getSimpleName();

			if (simpleName.contentEquals(VALUE)) {
				value = e.getValue();
				continue;
			}
			if (simpleName.contentEquals(RELATIVE_TO)) {
				relativeTo = e.getValue();
			}
		}

		if (value == null) {
			getMessenger().printMessage(
					Diagnostic.Kind.ERROR,
					"No source path specified",
					type,
					annotation);
			return null;
		}

		if (relativeTo == null) {
			return addAbsolute(type, annotation, value, relativeTo);
		}

		return addRelative(type, annotation, value, relativeTo);
	}

	private TypeSource addAbsolute(
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo) {

		final String[] path = nameAndRest(path(type, annotation, value));
		final TypeSourceName name = new TypeSourceName(path[0]);
		final String restPath = path[1];

		if (this.module == null) {
			this.module = new TypeWithSource(
					this,
					name,
					type,
					annotation,
					value,
					relativeTo,
					restPath != null);
			if (restPath == null) {
				return this.module;
			}
		} else if (!name.getKey().equals(this.module.getName().getKey())) {
			reportDuplicateRoot(type, annotation, relativeTo);
			if (this.module.error()) {
				reportDuplicateRoot(
						this.module.getType(),
						this.module.getAnnotation(),
						this.module.getRelativeTo());
			}
			return null;
		} else if (restPath == null) {
			if (!this.module.isImplicit()) {
				reportDuplicateRoot(type, annotation, relativeTo);
				if (this.module.error()) {
					reportDuplicateRoot(
							this.module.getType(),
							this.module.getAnnotation(),
							this.module.getRelativeTo());
				}
				return null;
			}

			this.module.override(name, type, annotation, value, relativeTo);

			return this.module;
		}

		this.module.add(name, type, annotation, value, relativeTo, restPath);

		return this.module;
	}

	private TypeSource addRelative(
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo) {

		final TypeMirror relativeToType = annotationTypeValue(relativeTo);
		final Name typeName =
				relativeToType != null ? typeName(relativeToType) : null;

		if (typeName == null) {
			getMessenger().printMessage(
					Diagnostic.Kind.ERROR,
					"Type, the path is relative to not specified",
					type,
					annotation,
					relativeTo);
			return null;
		}

		final RelTypeSources sources;
		final RelTypeSources existingSources = this.relative.get(typeName);

		if (existingSources != null) {
			sources = existingSources;
		} else {
			sources = new PendingTypeSources();
			this.relative.put(typeName, sources);
		}

		final String[] path = nameAndRest(path(type, annotation, value));
		final RelTypeSource relative = new RelTypeSource(
				this,
				new TypeSourceName(path[0]),
				type,
				annotation,
				value,
				relativeTo,
				path[1]);

		sources.add(relative);

		return relative;
	}

	private boolean validate() {
		if (this.module == null) {
			// Nothing to generate.
			return false;
		}
		if (this.module.isImplicit()) {
			getMessenger().printMessage(
					Diagnostic.Kind.ERROR,
					"Annotated module not found");
			return false;
		}
		if (this.relative != null) {
			for (RelTypeSources sources : this.relative.values()) {
				sources.validate();
			}
		}
		return true;
	}

	private void reportDuplicateRoot(
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue relativeTo) {
		getMessenger().printMessage(
				Diagnostic.Kind.ERROR,
				"Module should have only one source root",
				type,
				annotation,
				relativeTo);
	}

}
