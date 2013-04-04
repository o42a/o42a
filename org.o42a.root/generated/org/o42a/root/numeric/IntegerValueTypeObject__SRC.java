// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.numeric;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link IntegerValueTypeObject}.
 * 
 * File: root/integer.o42a
 */
public final class IntegerValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public IntegerValueTypeObject__SRC(AnnotatedSources parent) {
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
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.numeric.IntegerToFloat(
					owner,
					new org.o42a.root.numeric.IntegerToFloat__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.IntegerToString(
					owner,
					new org.o42a.root.numeric.IntegerToString__SRC(this))
			.getScope().toField(),
		};
	}

}
