// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.array;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link ConstantArrayValueTypeObject}.
 * 
 * File: root/constant_array.o42a
 */
public final class ConstantArrayValueTypeObject$$Sources implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public ConstantArrayValueTypeObject$$Sources(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"constant_array.o42a");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[] {
			new org.o42a.intrinsic.array.ConstantArrayLength(
					owner,
					new org.o42a.intrinsic.array.ConstantArrayLength$$Sources(this))
			.getScope().toField(),
		};
	}

}
