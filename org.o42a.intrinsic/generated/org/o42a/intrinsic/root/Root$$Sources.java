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
public final class Root$$Sources implements AnnotatedSources {

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
		this.sourceTree.add("array.o42a");
		this.sourceTree.add("operators.o42a");

		return this.sourceTree;
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[] {
			new org.o42a.intrinsic.array.ConstantArrayValueTypeObject(
					owner,
					new org.o42a.intrinsic.array.ConstantArrayValueTypeObject$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.root.DirectiveValueTypeObject(
					owner,
					new org.o42a.intrinsic.root.DirectiveValueTypeObject$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.FloatValueTypeObject(
					owner,
					new org.o42a.intrinsic.numeric.FloatValueTypeObject$$Sources(this))
			.getScope().toField(),
			new org.o42a.common.object.AnnotatedObject(
					owner,
					new org.o42a.intrinsic.root.Root$$floats(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.IntegerValueTypeObject(
					owner,
					new org.o42a.intrinsic.numeric.IntegerValueTypeObject$$Sources(this))
			.getScope().toField(),
			new org.o42a.common.object.AnnotatedObject(
					owner,
					new org.o42a.intrinsic.root.Root$$integers(this))
			.getScope().toField(),
			new org.o42a.intrinsic.string.StringValueTypeObject(
					owner,
					new org.o42a.intrinsic.string.StringValueTypeObject$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.string.Strings(
					owner,
					new org.o42a.intrinsic.string.Strings$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.root.UseNamespace(
					owner,
					new org.o42a.intrinsic.root.UseNamespace$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.root.UseObject(
					owner,
					new org.o42a.intrinsic.root.UseObject$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.array.VariableArrayValueTypeObject(
					owner,
					new org.o42a.intrinsic.array.VariableArrayValueTypeObject$$Sources(this))
			.getScope().toField(),
		};
	}

}
