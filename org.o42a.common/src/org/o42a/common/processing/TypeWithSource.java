/*
    Modules Commons
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.common.processing;

import static org.o42a.common.object.AnnotatedModule.SOURCES_DESCRIPTOR_SUFFIX;
import static org.o42a.common.processing.TypesWithSources.nameAndRest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.processing.Filer;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.URLSourceTree;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


class TypeWithSource extends TypeSource implements RelTypeSources {

	private boolean error;

	private HashMap<String, TypeWithSource> subEntries;
	private boolean implicit;

	TypeWithSource(
			TypesWithSources types,
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo,
			boolean implicit) {
		super(types, name, type, annotation, value, relativeTo);
		this.implicit = implicit;
		if (!implicit) {
			types.registerType(this);
		}
	}

	public final boolean isImplicit() {
		return this.implicit;
	}

	public final boolean isModule() {
		return getTypes().getModule() == this;
	}

	@Override
	public void override(
			TypeSourceName name,
			TypeElement type,
			AnnotationMirror annotation,
			AnnotationValue value,
			AnnotationValue relativeTo) {
		super.override(name, type, annotation, value, relativeTo);
		this.implicit = false;
	}

	public boolean add(
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
			this.subEntries = new HashMap<String, TypeWithSource>();
			existing = null;
		} else {
			existing = this.subEntries.get(key);
		}

		if (existing == null) {
			subEntry = new TypeWithSource(
					getTypes(),
					name,
					type,
					annotation,
					value,
					relativeTo,
					restPath == null);
			this.subEntries.put(key, subEntry);
		} else if (restPath != null) {
			subEntry = existing;
		} else {

			final TypeSourceName existingName = existing.getName();
			final TypeSourceName preferred = name.preferred(existingName);

			if (preferred == null) {
				getMessenger().printMessage(
						Diagnostic.Kind.ERROR,
						"Two or more types refer the same source "
						+ getName(),
						type,
						annotation);
				if (error()) {
					getMessenger().printMessage(
							Diagnostic.Kind.ERROR,
							"Two or more types refer to the same source "
							+ getName(),
							getType(),
							getAnnotation());
				}

				return false;
			}

			if (preferred == name) {
				existing.override(name, type, annotation, value, relativeTo);
			}

			return true;
		}

		if (restPath == null) {
			return true;
		}

		final String[] path =
				nameAndRest(getTypes().path(type, annotation, value));

		return subEntry.add(
				new TypeSourceName(path[0]),
				type,
				annotation,
				value,
				relativeTo,
				restPath);
	}

	@Override
	public void add(RelTypeSource source) {
		add(
				source.getName(),
				source.getType(),
				source.getAnnotation(),
				source.getValue(),
				source.getRelativeTo(),
				source.getRestPath());
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

	public void appendOriginatingElementsTo(
			ArrayList<TypeElement> originatingElements) {
		if (!isImplicit()) {
			originatingElements.add(getType());
		}
		if (this.subEntries != null) {
			for (TypeWithSource source : this.subEntries.values()) {
				source.appendOriginatingElementsTo(originatingElements);
			}
		}
	}

	public String emitDescriptor(String pathPrefix) throws IOException {

		final String descriptorClass =
				getType().getQualifiedName()
				+ SOURCES_DESCRIPTOR_SUFFIX;
		final Filer filer = getProcessingEnv().getFiler();
		final JavaFileObject descriptor =
				filer.createSourceFile(descriptorClass, getType());
		final PrintWriter out = new PrintWriter(descriptor.openWriter());

		try {
			emit(out, pathPrefix);
		} finally {
			out.close();
		}

		return descriptorClass;
	}

	private void emit(PrintWriter out, String pathPrefix) throws IOException {

		final String className =
				getType().getSimpleName() + SOURCES_DESCRIPTOR_SUFFIX;
		final Elements utils = getProcessingEnv().getElementUtils();
		final PackageElement packageElement = utils.getPackageOf(getType());

		if (!packageElement.isUnnamed()) {
			out.print("package ");
			out.print(packageElement.getQualifiedName());
			out.println(";");
			out.println();
		}

		printImport(out, Iterator.class);
		out.println();

		printImport(out, AnnotatedSources.class);
		printImport(out, URLSourceTree.class.getPackage());
		printImport(out, MemberOwner.class);
		printImport(out, Field.class);

		out.println();
		out.println();

		out.append("public final class ").append(className);
		out.println(" implements AnnotatedSources {");

		out.println();
		out.append("\tprivate URLSourceTree sourceTree;");

		if (isModule()) {
			emitBase(out, className);
		} else {
			emitSetParent(out);
		}

		emitGetSourceTree(out, className);
		emitFields(out, pathPrefix);

		out.println();
		out.println("}");
	}

	private void printImport(PrintWriter out, Class<?> cls) {
		out.append("import ").append(cls.getCanonicalName()).println(";");
	}

	private void printImport(PrintWriter out, Package pkg) {
		out.append("import ").append(pkg.getName()).println(".*;");
	}

	private void emitBase(PrintWriter out, String className) {
		out.println();
		out.println("\tprivate static java.net.URL base() {");
		out.println("\t\ttry {");

		out.println();
		out.append("\t\t\tfinal java.net.URL self = ").append(className);
		out.append(".class").println(".getResource(");
		out.append("\t\t\t\t\t").append(className);
		out.println(".class.getSimpleName() + \".class\");");
		out.println();

		out.print("return new java.net.URL(\"");
		printBasePath(out);
		out.println("\");");

		out.println("\t\tcatch (java.net.MalformedURLException e) {");
		out.println("\t\t\tthrow new ExceptionInInitializerError(e);");
		out.println("\t\t}");
		out.println("\t}");
	}

	private void printBasePath(PrintWriter out) {

		final Elements utils = getProcessingEnv().getElementUtils();
		PackageElement packageElement = utils.getPackageOf(getType());
		boolean slash = false;

		while (!packageElement.isUnnamed()) {
			if (slash) {
				out.print('/');
			} else {
				slash = true;
			}
			out.print("..");
			packageElement = utils.getPackageOf(
					packageElement.getEnclosingElement());
		}
	}

	private void emitSetParent(PrintWriter out) {
		out.println();
		out.println("\tprivate AnnotatedSources parent;");

		out.println();
		out.println("\tpublic void setParent(AnnotatedSources parent) {");
		out.println("\t\tthis.parent = parent;");
		out.println("};");
	}

	private void emitGetSourceTree(PrintWriter out, String className) {
		out.println();
		out.println("\t@Override");
		out.println("\tpublic URLSourceTree getSourceTree() {");

		out.println("\t\t if (this.sourceTree != null) {");
		out.println("\t\t\treturn this.sourceTree;");
		out.println("\t\t}");

		out.append("\t\treturn this.sourceTree = new ");
		switch (getSourceKind()) {
		case FILE:
			out.append("SingleURLSource(");
			break;
		case DIR:
			out.append("SingleURLSource(");
			break;
		case EMPTY:
			out.append("EmptyURLSource(");
		}
		if (isModule()) {
			out.append("null, base(), ");
		} else {
			out.append("this.parent.getSourceTree(), ");
		}
		out.append('"').append(getName().getName()).append('"');
		out.append(getName().getName());
		out.println(");");

		out.println("\t}");
	}

	private void emitFields(
			PrintWriter out,
			String pathPrefix)
	throws IOException {
		out.println();
		out.println("\t@Override");
		out.println(
				"\tpublic Iterator<? extends Field<?>>"
				+ " fields(MemberOwner owner) {");

		if (getSourceKind() != SourceKind.FILE || this.subEntries == null) {
			out.println(
					"\t\treturn new java.util.Collections"
					+ ".<Field<?>>emptyList().iterator();");
		} else {

			final int numFields = this.subEntries.size();

			out.println();
			out.append(
					"\t\tfinal java.util.ArrayList<Field<?>> fields ="
					+ " new java.util.ArrayList<Field<?>>(");
			out.append(Integer.toString(numFields)).println(");");

			printSources(out, pathPrefix);

			out.append("\t\treturn fields.iterator();");
		}

		out.println("\t};");
	}

	private void printSources(
			PrintWriter out,
			String pathPrefix)
	throws IOException {
		out.println();

		int i = 0;

		for (TypeWithSource subEntry : this.subEntries.values()) {
			i = subEntry.printSource(out, pathPrefix, i);
		}
	}

	private int printSource(
			PrintWriter out,
			String pathPrefix,
			int index)
	throws IOException {
		if (isImplicit()) {

			int i = index;

			for (TypeWithSource subEntry : this.subEntries.values()) {
				i = subEntry.printSource(
						out,
						pathPrefix + getName().getName(),
						i);
			}

			return i;
		}

		final String descriptorClass = emitDescriptor("");
		final String idx = Integer.toString(index);

		out.println();
		out.append("\t\tfinal AnnotatedSource source");
		out.append(idx).append(" = new ").append(descriptorClass);
		out.println("();");
		out.println();
		out.append("\t\tsource").append(idx).println(".setParent(this);");
		out.append("\t\tfields.add(new ").append(getType().getQualifiedName());
		out.append("(owner, source").append(idx).println(");");

		return index + 1;
	}

}
