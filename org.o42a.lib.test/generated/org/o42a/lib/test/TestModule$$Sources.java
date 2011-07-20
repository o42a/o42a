// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


public final class TestModule$$Sources implements AnnotatedSources {

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

	private URLSourceTree sourceTree;

	@Override
	public URLSourceTree getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				null,
				base(),
				"test.o42a");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[] {
			new org.o42a.lib.test.rt.parser.Parser(
					owner,
					new org.o42a.lib.test.rt.parser.Parser$$Sources(this))
			.getScope().toField(),
			new org.o42a.lib.test.rt.RtFloat(
					owner,
					new org.o42a.lib.test.rt.RtFloat$$Sources(this))
			.getScope().toField(),
			new org.o42a.lib.test.rt.RtInteger(
					owner,
					new org.o42a.lib.test.rt.RtInteger$$Sources(this))
			.getScope().toField(),
			new org.o42a.lib.test.rt.RtString(
					owner,
					new org.o42a.lib.test.rt.RtString$$Sources(this))
			.getScope().toField(),
			new org.o42a.lib.test.run.RunTests(
					owner,
					new org.o42a.lib.test.run.RunTests$$Sources(this))
			.getScope().toField(),
		};
	}

}
