// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.macros;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link MacrosModule}.
 * 
 * File: macros.o42a
 */
public final class MacrosModule__SRC implements AnnotatedSources {

	private static final Class<? extends MacrosModule> MODULE_CLASS =
			MacrosModule.class;

	private static java.net.URL base() {
		try {

			final java.net.URL self = MODULE_CLASS.getResource(
					MODULE_CLASS.getSimpleName() + ".class");

			return new java.net.URL(self, "../../../..");
		} catch (java.net.MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private SingleURLSource sourceTree;

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				null,
				base(),
				"macros.o42a");
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[0];
	}

}
