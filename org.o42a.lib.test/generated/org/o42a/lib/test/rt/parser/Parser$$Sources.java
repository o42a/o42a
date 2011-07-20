// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test.rt.parser;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


public final class Parser$$Sources implements AnnotatedSources {

	private final AnnotatedSources parent;
	private URLSourceTree sourceTree;

	public Parser$$Sources(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public URLSourceTree getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"parser/");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[0];
	}

}
