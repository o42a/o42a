// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.string;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link StringValueTypeObject}.
 * 
 * File: root/string.o42a
 */
public final class StringValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public StringValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"string.o42a");
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[] {
			new org.o42a.intrinsic.string.StringChar(
					owner,
					new org.o42a.intrinsic.string.StringChar__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.string.StringLength(
					owner,
					new org.o42a.intrinsic.string.StringLength__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.string.SubString(
					owner,
					new org.o42a.intrinsic.string.SubString__SRC(this))
			.getScope().toField(),
		};
	}

}
