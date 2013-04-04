// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


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
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.numeric.AddIntegers(
					owner,
					new org.o42a.root.numeric.AddIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.ParseInteger(
					owner,
					new org.o42a.root.numeric.ParseInteger__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.CompareIntegers(
					owner,
					new org.o42a.root.numeric.CompareIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.DivideIntegers(
					owner,
					new org.o42a.root.numeric.DivideIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.IntegersEqual(
					owner,
					new org.o42a.root.numeric.IntegersEqual__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.IntegerMinus(
					owner,
					new org.o42a.root.numeric.IntegerMinus__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.MultiplyIntegers(
					owner,
					new org.o42a.root.numeric.MultiplyIntegers__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.SubtractIntegers(
					owner,
					new org.o42a.root.numeric.SubtractIntegers__SRC(this))
			.getScope().toField(),
		};
	}

}
