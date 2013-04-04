// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link TestModule}.
 * 
 * File: test.o42a
 */
public final class TestModule__SRC implements AnnotatedSources {

	private static final Class<? extends TestModule> MODULE_CLASS =
			TestModule.class;

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
				"test.o42a");

		this.sourceTree.add("rt-float.o42a");
		this.sourceTree.add("rt-integer.o42a");
		this.sourceTree.add("rt-string.o42a");

		return this.sourceTree;
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.common.object.AnnotatedObject(
					owner,
					new org.o42a.lib.test.TestModule__parser(this))
			.getScope().toField(),
			new org.o42a.lib.test.rt.RtFalse(
					owner,
					new org.o42a.lib.test.rt.RtFalse__SRC(this))
			.getScope().toField(),
			new org.o42a.lib.test.rt.RtVoid(
					owner,
					new org.o42a.lib.test.rt.RtVoid__SRC(this))
			.getScope().toField(),
			new org.o42a.lib.test.run.RunTests(
					owner,
					new org.o42a.lib.test.run.RunTests__SRC(this))
			.getScope().toField(),
		};
	}

}
