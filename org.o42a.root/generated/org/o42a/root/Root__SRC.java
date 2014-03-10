// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link Root}.
 * 
 * File: root.o42a
 */
public final class Root__SRC implements AnnotatedSources {

	private static final Class<? extends Root> MODULE_CLASS =
			Root.class;

	private static java.net.URL base() {
		try {

			final java.net.URL self = MODULE_CLASS.getResource(
					MODULE_CLASS.getSimpleName() + ".class");

			return new java.net.URL(self, "../../..");
		} catch (java.net.MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private URLSources sourceTree;

	@Override
	public URLSources getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}

		this.sourceTree = new URLSources(
				null,
				base(),
				"root.o42a");

		this.sourceTree.add("assignable.o42a");
		this.sourceTree.add("number.o42a");
		this.sourceTree.add("indexed.o42a");
		this.sourceTree.add("property.o42a");

		return this.sourceTree;
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.array.ArrayValueTypeObject(
					owner,
					new org.o42a.root.array.ArrayValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.DirectiveValueTypeObject(
					owner,
					new org.o42a.root.DirectiveValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.array.Duplicates(
					owner,
					new org.o42a.root.array.Duplicates__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.FloatValueTypeObject(
					owner,
					new org.o42a.root.numeric.FloatValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.common.object.AnnotatedObject(
					owner,
					new org.o42a.root.Root__floats(this))
			.getScope().toField(),
			new org.o42a.root.flow.FlowValueTypeObject(
					owner,
					new org.o42a.root.flow.FlowValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.IntegerValueTypeObject(
					owner,
					new org.o42a.root.numeric.IntegerValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.common.object.AnnotatedObject(
					owner,
					new org.o42a.root.Root__integers(this))
			.getScope().toField(),
			new org.o42a.root.link.LinkValueTypeObject(
					owner,
					new org.o42a.root.link.LinkValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.MacroValueTypeObject(
					owner,
					new org.o42a.root.MacroValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.array.RowValueTypeObject(
					owner,
					new org.o42a.root.array.RowValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.string.StringValueTypeObject(
					owner,
					new org.o42a.root.string.StringValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.string.Strings(
					owner,
					new org.o42a.root.string.Strings__SRC(this))
			.getScope().toField(),
			new org.o42a.root.UseNamespace(
					owner,
					new org.o42a.root.UseNamespace__SRC(this))
			.getScope().toField(),
			new org.o42a.root.UseObject(
					owner,
					new org.o42a.root.UseObject__SRC(this))
			.getScope().toField(),
			new org.o42a.root.link.VariableValueTypeObject(
					owner,
					new org.o42a.root.link.VariableValueTypeObject__SRC(this))
			.getScope().toField(),
		};
	}

}
