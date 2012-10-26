// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


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

			return new java.net.URL(self, "../../../..");
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

		this.sourceTree.add("number.o42a");
		this.sourceTree.add("indexed.o42a");
		this.sourceTree.add("operators.o42a");
		this.sourceTree.add("property.o42a");

		return this.sourceTree;
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[] {
			new org.o42a.intrinsic.array.ArrayValueTypeObject(
					owner,
					new org.o42a.intrinsic.array.ArrayValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.root.DirectiveValueTypeObject(
					owner,
					new org.o42a.intrinsic.root.DirectiveValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.array.DuplicatesArray(
					owner,
					new org.o42a.intrinsic.array.DuplicatesArray__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.FloatValueTypeObject(
					owner,
					new org.o42a.intrinsic.numeric.FloatValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.common.object.AnnotatedObject(
					owner,
					new org.o42a.intrinsic.root.Root__floats(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.IntegerValueTypeObject(
					owner,
					new org.o42a.intrinsic.numeric.IntegerValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.common.object.AnnotatedObject(
					owner,
					new org.o42a.intrinsic.root.Root__integers(this))
			.getScope().toField(),
			new org.o42a.intrinsic.link.LinkValueTypeObject(
					owner,
					new org.o42a.intrinsic.link.LinkValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.root.MacroValueTypeObject(
					owner,
					new org.o42a.intrinsic.root.MacroValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.array.RowValueTypeObject(
					owner,
					new org.o42a.intrinsic.array.RowValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.string.StringValueTypeObject(
					owner,
					new org.o42a.intrinsic.string.StringValueTypeObject__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.string.Strings(
					owner,
					new org.o42a.intrinsic.string.Strings__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.root.UseNamespace(
					owner,
					new org.o42a.intrinsic.root.UseNamespace__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.root.UseObject(
					owner,
					new org.o42a.intrinsic.root.UseObject__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.link.VariableValueTypeObject(
					owner,
					new org.o42a.intrinsic.link.VariableValueTypeObject__SRC(this))
			.getScope().toField(),
		};
	}

}
