// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test.rt;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


public final class RtString$$Sources implements AnnotatedSources {

	private final AnnotatedSources parent;
	private URLSourceTree sourceTree;

	public RtString$$Sources(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public URLSourceTree getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"rt-string.o42a");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[0];
	}

}
