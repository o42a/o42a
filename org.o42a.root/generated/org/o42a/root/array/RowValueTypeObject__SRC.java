// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.array;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link RowValueTypeObject}.
 * 
 * File: root/row.o42a
 */
public final class RowValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public RowValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"row.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.array.CopyRowElements(
					owner,
					new org.o42a.root.array.CopyRowElements__SRC(this))
			.getScope().toField(),
			new org.o42a.root.array.RowItem(
					owner,
					new org.o42a.root.array.RowItem__SRC(this))
			.getScope().toField(),
			new org.o42a.root.array.RowLength(
					owner,
					new org.o42a.root.array.RowLength__SRC(this))
			.getScope().toField(),
		};
	}

}
