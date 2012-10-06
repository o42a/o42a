// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.array;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link RowValueTypeObject}.
 * 
 * File: root/row.o42a
 */
public final class RowValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private URLSources sourceTree;

	public RowValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public URLSources getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}

		this.sourceTree = new URLSources(
				this.parent.getSourceTree(),
				"row.o42a");

		this.sourceTree.add("item_type.o42a");

		return this.sourceTree;
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[] {
			new org.o42a.intrinsic.array.RowLength(
					owner,
					new org.o42a.intrinsic.array.RowLength__SRC(this))
			.getScope().toField(),
		};
	}

}
