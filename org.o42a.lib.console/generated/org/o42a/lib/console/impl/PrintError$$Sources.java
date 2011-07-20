// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.console.impl;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link PrintError}.
 * 
 * File: console/print_error.o42a
 */
public final class PrintError$$Sources implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public PrintError$$Sources(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"print_error.o42a");
	}

	@Override
	public Field<?>[] fields(MemberOwner owner) {
		return new Field<?>[0];
	}

}
