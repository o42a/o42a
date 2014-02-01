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

import static org.o42a.common.object.AnnotatedModule.SOURCES_DESCRIPTOR_SUFFIX;
import static org.o42a.tools.ap.AnnotationArrayValueVisitor.annotationArrayValue;
import static org.o42a.tools.ap.TypesWithSources.VALUE;
import static org.o42a.tools.ap.TypesWithSources.nameAndRest;
import static org.o42a.tools.ap.UnderscoredCPWriter.underscoredName;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.o42a.util.io.SourceFileName;


class TypeWithSource extends TypeSource implements RelTypeSources {

	private final TypeWithSource parent;
	private String descriptorName;
	private String packageName;

	private TreeMap<String, TypeWithSource> subEntries;
	private HashMap<String, Integer> usedNames;
	private int implicitNameSeq;
	private boolean implicit;
	private boolean error;
	private AnnotationMirror relatedSources;

	TypeWithSource(
			TypesWithSources types,
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo,
			boolean implicit) {
		this(types, null, name, type, annotation, value, relativeTo, implicit);
	}

	TypeWithSource(
			TypeWithSource parent,
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo,
			boolean implicit) {
		this(
				parent.getTypes(),
				parent,
				name,
				type,
				annotation,
				value,
				relativeTo,
				implicit);
	}

	private TypeWithSource(
			TypesWithSources types,
			TypeWithSource parent,
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo,
			boolean implicit) {
		super(types, name, type, annotation, value, relativeTo);
		this.parent = parent;
		this.implicit = implicit;
		if (!implicit) {
			setExplicit();
		} else {
			this.descriptorName = parent.implicitDescriptorName(name);
			this.packageName = parent.getPackageName();
		}
	}

	public final TypeWithSource getParent() {
		return this.parent;
	}

	public final boolean isImplicit() {
		return this.implicit;
	}

	public final String getDescriptorName() {
		return this.descriptorName;
	}

	public final String getPackageName() {
		return this.packageName;
	}

	public final String getQualifiedName() {
		return getPackageName() + '.' + getDescriptorName();
	}

	public final boolean isModule() {
		return getTypes().getModule() == this;
	}

	@Override
	public void addRelatedSources(AnnotationMirror relatedSources) {
		this.relatedSources = relatedSources;
	}

	@Override
	public void override(
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo) {
		super.override(name, type, annotation, value, relativeTo);
		if (!this.implicit) {
			throw new IllegalStateException(
					"Already explicit: " + this);
		}
		setExplicit();
	}

	public TypeWithSource add(
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo,
			String restPath) {

		final TypeWithSource existing;
		final TypeWithSource subEntry;
		final String key = name.getKey();

		if (this.subEntries == null) {
			this.subEntries = new TreeMap<>();
			existing = null;
		} else {
			existing = this.subEntries.get(key);
		}

		if (existing == null) {
			subEntry = new TypeWithSource(
					this,
					name,
					type,
					annotation,
					value,
					relativeTo,
					restPath != null);
			this.subEntries.put(key, subEntry);
		} else if (restPath != null) {
			subEntry = existing;
		} else if (existing.isImplicit()) {
			existing.override(name, type, annotation, value, relativeTo);
			return existing;
		} else {
			reportDuplicateSource(name, type, annotation);
			if (error()) {
				reportDuplicateSource(
						name,
						existing.getType(),
						existing.getAnnotation());
			}
			return null;
		}

		if (restPath == null) {
			return subEntry;
		}

		final String[] path = nameAndRest(restPath);

		return subEntry.add(
				new TypeSourceName(path[0]),
				type,
				annotation,
				value,
				relativeTo,
				path[1]);
	}

	private void reportDuplicateSource(
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation) {
		getMessenger().printMessage(
				Diagnostic.Kind.ERROR,
				"Two or more types refer the same source " + name,
				type,
				annotation);
	}

	@Override
	public void add(RelTypeSource source) {

		final TypeWithSource added = add(
				source.getName(),
				source.getType(),
				source.getAnnotation(),
				source.getValue(),
				source.getRelativeTo(),
				source.getRestPath());

		if (added == null) {
			return;
		}

		final AnnotationMirror relatedSources = source.getRelatedSources();

		if (relatedSources != null) {
			added.addRelatedSources(relatedSources);
		}
	}

	public final boolean error() {
		if (this.error) {
			return false;
		}
		this.error = true;
		return true;
	}

	@Override
	public void replaceBy(TypeWithSource other) {
		throw new IllegalStateException(
				"Attempt to register " + getType() + " twice");
	}

	@Override
	public void validate() {
	}

	@Override
	public String toString() {
		if (this.descriptorName == null) {
			return super.toString();
		}
		return getQualifiedName();
	}

	public void emitDescriptor() throws IOException {

		final Filer filer = getProcessingEnv().getFiler();
		final JavaFileObject descriptor = filer.createSourceFile(
				getQualifiedName(),
				originatingElements());
		final PrintWriter out = new PrintWriter(descriptor.openWriter());

		try {
			emit(out);
		} finally {
			out.close();
		}
	}

	private void setExplicit() {
		this.implicit = false;
		this.descriptorName =
				getType().getSimpleName() + SOURCES_DESCRIPTOR_SUFFIX;
		this.packageName =
				getProcessingEnv().getElementUtils()
				.getPackageOf(getType()).getQualifiedName().toString();
		getTypes().registerType(this);
	}

	private final String implicitDescriptorName(TypeSourceName sourceName) {

		final String localName = localDescriptorName(sourceName);

		if (!isImplicit()) {
			return getType().getSimpleName() + "__" + localName;
		}

		return getParent().getDescriptorName() + "__" + localName;
	}

	private String localDescriptorName(TypeSourceName sourceName) {

		final SourceFileName fileName = new SourceFileName(sourceName.getKey());
		final String localName;

		if (!fileName.isValid()) {
			return "_" + (++this.implicitNameSeq);
		}
		if (fileName.isAdapter()) {

			final org.o42a.util.string.Name[] adaptee =
					fileName.getAdapterId();

			localName = "$" + underscoredName(adaptee[adaptee.length - 1]);
		} else {
			localName = underscoredName(fileName.getFieldName());
		}

		final String briefName;

		if (localName.length() > 13) {
			briefName = localName.substring(0, 13);
		} else {
			briefName = localName;
		}

		if (this.usedNames == null) {
			this.usedNames = new HashMap<>();
		}

		final Integer counter =
				this.usedNames.put(briefName, Integer.valueOf(1));

		if (counter == null) {
			return briefName;
		}

		this.usedNames.put(briefName, counter + 1);

		return briefName + '_' + counter;
	}

	private Element[] originatingElements() {
		if (this.subEntries == null) {
			return new Element[] {getType()};
		}

		final ArrayList<Element> originatingElements =
				new ArrayList<>(this.subEntries.size());

		appendOriginatingElementsTo(originatingElements);

		return originatingElements.toArray(
				new Element[originatingElements.size()]);
	}

	private void appendOriginatingElementsTo(
			ArrayList<Element> originatingElements) {
		if (!isImplicit()) {
			originatingElements.add(getType());
		}
		if (this.subEntries != null) {
			for (TypeWithSource source : this.subEntries.values()) {
				source.appendOriginatingElementsTo(originatingElements);
			}
		}
	}

	private void emit(PrintWriter out) throws IOException {
		out.println("// GENERATED FILE. DO NOT MODIFY.");

		final String packageName = getPackageName();

		if (!packageName.isEmpty()) {
			out.print("package ");
			out.print(packageName);
			out.println(";");
			out.println();
		}

		out.println("import org.o42a.common.object.AnnotatedSources;");
		out.println("import org.o42a.common.source.*;");
		out.println("import org.o42a.core.member.field.Field;");
		out.println("import org.o42a.core.object.Obj;");

		out.println();
		out.println();

		printComment(out);
		out.append("public final class ").append(getDescriptorName());
		out.println(" implements AnnotatedSources {");

		if (isModule()) {
			emitBase(out);
			out.println();
		} else {
			out.println();
			out.println("\tprivate final AnnotatedSources parent;");
		}

		out.append("\tprivate ").append(urlSourceType());
		out.println(" sourceTree;");

		if (!isModule()) {
			emitConstructor(out);
		}

		emitGetSourceTree(out);
		emitFields(out);

		out.println();
		out.println("}");
	}

	private void printComment(PrintWriter out) {
		out.println("/**");
		if (!isImplicit()) {
			out.append(" * o42a sources for {@link ");
			out.append(getType().getSimpleName());
			out.println("}.");
			out.println(" * ");
		}

		out.append(" * ");
		switch (getSourceKind()) {
		case DIR:
			out.append("Directory: ");
			break;
		case FILE:
			out.append("File: ");
			break;
		case EMPTY:
			out.append("Source: ");
		}
		printPath(out);
		out.println();

		out.println(" */");
	}

	private void printPath(PrintWriter out) {
		if (this.parent != null) {
			this.parent.printDir(out);
		}
		out.append(getName().getName());
	}

	private void printDir(PrintWriter out) {
		if (this.parent != null) {
			this.parent.printDir(out);
		}
		out.append(getName().getKey()).append('/');
	}

	private void emitBase(PrintWriter out) {

		final Name className = getType().getSimpleName();

		out.println();
		out.append("\tprivate static final Class<? extends ");
		out.append(className).println("> MODULE_CLASS =");
		out.append("\t\t\t").append(className).println(".class;");

		out.println();
		out.println("\tprivate static java.net.URL base() {");
		out.println("\t\ttry {");

		out.println();
		out.println("\t\t\tfinal java.net.URL self ="
				+ " MODULE_CLASS.getResource(");
		out.println("\t\t\t\t\tMODULE_CLASS.getSimpleName() + \".class\");");

		out.println();
		out.print("\t\t\treturn new java.net.URL(self, \"");
		printBasePath(out);
		out.println("\");");

		out.println("\t\t} catch (java.net.MalformedURLException e) {");
		out.println("\t\t\tthrow new ExceptionInInitializerError(e);");
		out.println("\t\t}");
		out.println("\t}");
	}

	private void printBasePath(PrintWriter out) {

		final Elements utils = getProcessingEnv().getElementUtils();
		final PackageElement packageElement = utils.getPackageOf(getType());
		final String packageName = packageElement.getQualifiedName().toString();

		if (packageName.isEmpty()) {
			return;
		}

		int fromIdx = 0;

		boolean slash = false;

		for (;;) {
			if (slash) {
				out.print('/');
			} else {
				slash = true;
			}
			out.print("..");

			final int dotIdx = packageName.indexOf('.', fromIdx);

			if (dotIdx < 0) {
				break;
			}

			fromIdx = dotIdx + 1;
		}
	}

	private void emitConstructor(PrintWriter out) {
		out.println();
		out.append("\tpublic ").append(getDescriptorName());
		out.println("(AnnotatedSources parent) {");
		out.println("\t\tthis.parent = parent;");
		out.println("\t}");
	}

	private void emitGetSourceTree(PrintWriter out) {

		final String urlSourceType = urlSourceType();

		out.println();
		out.println("\t@Override");
		out.append("\tpublic ").append(urlSourceType);
		out.println(" getSourceTree() {");

		out.println("\t\tif (this.sourceTree != null) {");
		out.println("\t\t\treturn this.sourceTree;");
		out.println("\t\t}");

		if (this.relatedSources != null) {
			out.println();
			out.append("\t\t");
		} else {
			out.append("\t\treturn ");
		}
		out.append("this.sourceTree = new ").append(urlSourceType).println('(');
		if (isModule()) {
			out.println("\t\t\t\tnull,");
			out.println("\t\t\t\tbase(),");
		} else {
			out.println("\t\t\t\tthis.parent.getSourceTree(),");
		}
		out.append("\t\t\t\t\"").append(getName().getName());
		out.println("\");");

		if (this.relatedSources != null) {
			out.println();
			printRelatedSources(out);
			out.println();
			out.println("\t\treturn this.sourceTree;");
		}

		out.println("\t}");
	}

	private String urlSourceType() {
		if (this.relatedSources != null) {
			return "URLSources";
		}

		switch (getSourceKind()) {
		case FILE:
		case DIR:
			return "SingleURLSource";
		case EMPTY:
			return "EmptyURLSource";
		}

		throw new IllegalStateException(
				"Unsupported kind of source file: " + getSourceKind());
	}

	private void printRelatedSources(PrintWriter out) {

		AnnotationValue value = null;

		for (Map.Entry<
				? extends ExecutableElement,
				? extends AnnotationValue> e
				:this.relatedSources.getElementValues().entrySet()) {
			if (e.getKey().getSimpleName().contentEquals(VALUE)) {
				value = e.getValue();
			}
		}

		if (value == null) {
			return;
		}

		for (AnnotationValue val : annotationArrayValue(value)) {
			printRelatedSource(out, val);
		}
	}

	private void printRelatedSource(PrintWriter out, AnnotationValue value) {

		final String path = value.getValue().toString();

		if (path.startsWith("/")) {
			getMessenger().printMessage(
					Diagnostic.Kind.WARNING,
					"Source path should be relative",
					getType(),
					this.relatedSources,
					value);
		}

		out.append("\t\tthis.sourceTree.add(\"").append(path);
		out.println("\");");
	}

	private void emitFields(PrintWriter out) throws IOException {
		out.println();
		out.println("\t@Override");
		out.println(
				"\tpublic Field[]"
				+ " fields(Obj owner) {");

		if (this.subEntries == null) {
			out.println("\t\treturn new Field[0];");
		} else {
			out.println("\t\treturn new Field[] {");
			printFields(out);
			out.println("\t\t};");
		}

		out.println("\t}");
	}

	private void printFields(PrintWriter out) throws IOException {
		for (TypeWithSource subEntry : this.subEntries.values()) {
			subEntry.printField(out);
		}
	}

	private void printField(PrintWriter out) throws IOException {
		emitDescriptor();

		out.append("\t\t\tnew ");
		if (isImplicit()) {
			out.append("org.o42a.common.object.AnnotatedObject");
		} else {
			out.append(getType().getQualifiedName());
		}
		out.println("(");
		out.println("\t\t\t\t\towner,");
		out.append("\t\t\t\t\tnew ").append(getQualifiedName());
		out.println("(this))");
		out.println("\t\t\t.getScope().toField(),");
	}

}
