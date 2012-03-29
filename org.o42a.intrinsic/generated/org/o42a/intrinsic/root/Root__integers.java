// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * Directory: root/integers/
 */
public final class Root__integers implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public Root__integers(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"integers/");
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[] {
			new org.o42a.intrinsic.numeric.AddIntegers(
					owner,
					new org.o42a.intrinsic.numeric.AddIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.ParseInteger(
					owner,
					new org.o42a.intrinsic.numeric.ParseInteger__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.CompareIntegers(
					owner,
					new org.o42a.intrinsic.numeric.CompareIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.DivideIntegers(
					owner,
					new org.o42a.intrinsic.numeric.DivideIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.IntegersEqual(
					owner,
					new org.o42a.intrinsic.numeric.IntegersEqual__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.IntegerMinus(
					owner,
					new org.o42a.intrinsic.numeric.IntegerMinus__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.MultiplyIntegers(
					owner,
					new org.o42a.intrinsic.numeric.MultiplyIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.intrinsic.numeric.SubtractIntegers(
					owner,
					new org.o42a.intrinsic.numeric.SubtractIntegers__SRC(this))
			.getScope().toField(),
		};
	}

}
