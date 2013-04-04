// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


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
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.numeric.AddFloats(
					owner,
					new org.o42a.root.numeric.AddFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.ParseFloat(
					owner,
					new org.o42a.root.numeric.ParseFloat__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.CompareFloats(
					owner,
					new org.o42a.root.numeric.CompareFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.DivideFloats(
					owner,
					new org.o42a.root.numeric.DivideFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.FloatsEqual(
					owner,
					new org.o42a.root.numeric.FloatsEqual__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.FloatMinus(
					owner,
					new org.o42a.root.numeric.FloatMinus__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.MultiplyFloats(
					owner,
					new org.o42a.root.numeric.MultiplyFloats__SRC(this))
			.getScope().toField(),
			new org.o42a.root.numeric.SubtractFloats(
					owner,
					new org.o42a.root.numeric.SubtractFloats__SRC(this))
			.getScope().toField(),
		};
	}

}
