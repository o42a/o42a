// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.link;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link VariableValueTypeObject}.
 * 
 * File: root/variable.o42a
 */
public final class VariableValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public VariableValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"variable.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.link.VariableCast(
					owner,
					new org.o42a.root.link.VariableCast__SRC(this))
			.getScope().toField(),
		};
	}

}
