package org.o42a.lib.test;

import java.util.Iterator;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


public final class TestModule$$Sources implements AnnotatedSources {

	private URLSourceTree sourceTree;

	private static java.net.URL base() {
		try {

			final java.net.URL self = TestModule$$Sources.class.getResource(
					TestModule$$Sources.class.getSimpleName() + ".class");

			return new java.net.URL(self, "../../../..");
		} catch (java.net.MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	public URLSourceTree getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(null, base(), "test.o42a");
	}

	@Override
	public Iterator<? extends Field<?>> fields(MemberOwner owner) {

		final java.util.ArrayList<Field<?>> fields = new java.util.ArrayList<Field<?>>(5);

		final org.o42a.lib.test.rt.RtString$$Sources sources0 = new org.o42a.lib.test.rt.RtString$$Sources();

		sources0.setParent(this);
		fields.add(new org.o42a.lib.test.rt.RtString(owner, sources0).getScope().toField());

		final org.o42a.lib.test.rt.RtInteger$$Sources sources1 = new org.o42a.lib.test.rt.RtInteger$$Sources();

		sources1.setParent(this);
		fields.add(new org.o42a.lib.test.rt.RtInteger(owner, sources1).getScope().toField());

		final org.o42a.lib.test.run.RunTests$$Sources sources2 = new org.o42a.lib.test.run.RunTests$$Sources();

		sources2.setParent(this);
		fields.add(new org.o42a.lib.test.run.RunTests(owner, sources2).getScope().toField());

		final org.o42a.lib.test.rt.RtFloat$$Sources sources3 = new org.o42a.lib.test.rt.RtFloat$$Sources();

		sources3.setParent(this);
		fields.add(new org.o42a.lib.test.rt.RtFloat(owner, sources3).getScope().toField());

		final org.o42a.lib.test.rt.parser.Parser$$Sources sources4 = new org.o42a.lib.test.rt.parser.Parser$$Sources();

		sources4.setParent(this);
		fields.add(new org.o42a.lib.test.rt.parser.Parser(owner, sources4).getScope().toField());

		return fields.iterator();
	}

}
