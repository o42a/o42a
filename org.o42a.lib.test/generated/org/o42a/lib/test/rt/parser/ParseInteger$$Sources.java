// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test.rt.parser;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link ParseInteger}.
 * 
 * File: test/parser/integer.o42a
 */
public final class ParseInteger$$Sources implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public ParseInteger$$Sources(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"integer.o42a");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[0];
	}

}
