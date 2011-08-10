// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * Directory: root/floats/
 */
public final class Root$$floats implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public Root$$floats(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"floats/");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[] {
			new org.o42a.intrinsic.numeric.AddFloats(
					owner,
					new org.o42a.intrinsic.numeric.AddFloats$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.ParseFloat(
					owner,
					new org.o42a.intrinsic.numeric.ParseFloat$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.CompareFloats(
					owner,
					new org.o42a.intrinsic.numeric.CompareFloats$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.DivideFloats(
					owner,
					new org.o42a.intrinsic.numeric.DivideFloats$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.FloatsEqual(
					owner,
					new org.o42a.intrinsic.numeric.FloatsEqual$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.FloatMinus(
					owner,
					new org.o42a.intrinsic.numeric.FloatMinus$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.MultiplyFloats(
					owner,
					new org.o42a.intrinsic.numeric.MultiplyFloats$$Sources(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.SubtractFloats(
					owner,
					new org.o42a.intrinsic.numeric.SubtractFloats$$Sources(this))
			.getScope().toField(),
				};
	}

}
