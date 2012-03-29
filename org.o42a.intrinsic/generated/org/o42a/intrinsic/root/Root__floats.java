// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * Directory: root/floats/
 */
public final class Root__floats implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public Root__floats(AnnotatedSources parent) {
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
	public Field[] fields(MemberOwner owner) {
		return new Field[] {
			new org.o42a.intrinsic.numeric.AddFloats(
					owner,
					new org.o42a.intrinsic.numeric.AddFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.ParseFloat(
					owner,
					new org.o42a.intrinsic.numeric.ParseFloat__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.CompareFloats(
					owner,
					new org.o42a.intrinsic.numeric.CompareFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.DivideFloats(
					owner,
					new org.o42a.intrinsic.numeric.DivideFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.FloatsEqual(
					owner,
					new org.o42a.intrinsic.numeric.FloatsEqual__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.FloatMinus(
					owner,
					new org.o42a.intrinsic.numeric.FloatMinus__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.MultiplyFloats(
					owner,
					new org.o42a.intrinsic.numeric.MultiplyFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.SubtractFloats(
					owner,
					new org.o42a.intrinsic.numeric.SubtractFloats__SRC(this))
			.getScope().toField(),
		};
	}

}
