// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.array;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link ArrayAsRow}.
 * 
 * File: root/array/as_row.o42a
 */
public final class ArrayAsRow__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public ArrayAsRow__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"as_row.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[0];
	}

}
