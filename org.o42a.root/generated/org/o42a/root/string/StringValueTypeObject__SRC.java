// GENERATED FILE. DO NOT MODIFY.
package org.o42a.root.string;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


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
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.root.string.StringChar(
					owner,
					new org.o42a.root.string.StringChar__SRC(this))
			.getScope().toField(),
			new org.o42a.root.string.StringLength(
					owner,
					new org.o42a.root.string.StringLength__SRC(this))
			.getScope().toField(),
			new org.o42a.root.string.SubString(
					owner,
					new org.o42a.root.string.SubString__SRC(this))
			.getScope().toField(),
		};
	}

}
