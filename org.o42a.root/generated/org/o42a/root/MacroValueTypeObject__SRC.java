// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link MacroValueTypeObject}.
 * 
 * File: root/macro.o42a
 */
public final class MacroValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public MacroValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"macro.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[0];
	}

}
