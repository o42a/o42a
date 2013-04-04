// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.string;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link Strings}.
 * 
 * Directory: root/strings/
 */
public final class Strings__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public Strings__SRC(AnnotatedSources parent) {
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
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.string.CompareStrings(
					owner,
					new org.o42a.root.string.CompareStrings__SRC(this))
			.getScope().toField(),
			new org.o42a.root.string.ConcatStrings(
					owner,
					new org.o42a.root.string.ConcatStrings__SRC(this))
			.getScope().toField(),
		};
	}

}
