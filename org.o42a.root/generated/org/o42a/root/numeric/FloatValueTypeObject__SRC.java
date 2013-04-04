// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.numeric;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link FloatValueTypeObject}.
 * 
 * File: root/float__.o42a
 */
public final class FloatValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public FloatValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"float__.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.numeric.FloatToString(
					owner,
					new org.o42a.root.numeric.FloatToString__SRC(this))
			.getScope().toField(),
		};
	}

}
