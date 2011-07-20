// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.string;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link Strings}.
 * 
 * Directory: root/strings/
 */
public final class Strings$$Sources implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public Strings$$Sources(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"strings/");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[] {
			new org.o42a.intrinsic.string.CompareStrings(
					owner,
					new org.o42a.intrinsic.string.CompareStrings$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.string.ConcatStrings(
					owner,
					new org.o42a.intrinsic.string.ConcatStrings$$Sources(this))
			.getScope().toField(),
		};
	}

}
