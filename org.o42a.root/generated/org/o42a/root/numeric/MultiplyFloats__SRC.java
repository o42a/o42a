// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.numeric;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link MultiplyFloats}.
 * 
 * File: root/floats/multiply.o42a
 */
public final class MultiplyFloats__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public MultiplyFloats__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"multiply.o42a");
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[0];
	}

}