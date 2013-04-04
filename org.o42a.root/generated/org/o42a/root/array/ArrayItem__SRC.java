// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.array;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link ArrayItem}.
 * 
 * File: root/array/item.o42a
 */
public final class ArrayItem__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public ArrayItem__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"item.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.array.SetArrayItem(
					owner,
					new org.o42a.root.array.SetArrayItem__SRC(this))
			.getScope().toField(),
		};
	}

}
