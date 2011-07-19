package org.o42a.lib.test.run;

import java.util.Iterator;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


public final class RunTests$$Sources implements AnnotatedSources {

	private URLSourceTree sourceTree;

	private AnnotatedSources parent;

	public final void setParent(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public URLSourceTree getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(this.parent.getSourceTree(), "run_tests.o42a");
	}

	@Override
	public Iterator<? extends Field<?>> fields(MemberOwner owner) {
		return java.util.Collections.<Field<?>>emptyList().iterator();
	}

}
