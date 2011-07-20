// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test.rt;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link RtFalse}.
 * 
 * Source: test/rt-false
 */
public final class RtFalse$$Sources implements AnnotatedSources {

	private final AnnotatedSources parent;
	private EmptyURLSource sourceTree;

	public RtFalse$$Sources(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public EmptyURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new EmptyURLSource(
				this.parent.getSourceTree(),
				"rt-false");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[0];
	}

}
