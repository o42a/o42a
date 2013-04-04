// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.array;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link ArrayValueTypeObject}.
 * 
 * File: root/array.o42a
 */
public final class ArrayValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public ArrayValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"array.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.array.ArrayAsRow(
					owner,
					new org.o42a.root.array.ArrayAsRow__SRC(this))
			.getScope().toField(),
			new org.o42a.root.array.CopyArrayElements(
					owner,
					new org.o42a.root.array.CopyArrayElements__SRC(this))
			.getScope().toField(),
			new org.o42a.root.array.ArrayItem(
					owner,
					new org.o42a.root.array.ArrayItem__SRC(this))
			.getScope().toField(),
			new org.o42a.root.array.ArrayLength(
					owner,
					new org.o42a.root.array.ArrayLength__SRC(this))
			.getScope().toField(),
		};
	}

}
