// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.array;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link CopyArrayElements}.
 * 
 * File: root/array/copy.o42a
 */
public final class CopyArrayElements__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public CopyArrayElements__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"copy.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[0];
	}

}
