// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test.rt.parser;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link ParseString}.
 * 
 * File: test/parser/string.o42a
 */
public final class ParseString__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public ParseString__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"string.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[0];
	}

}
