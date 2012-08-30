// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.collections;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link CollectionsModule}.
 * 
 * File: collections.o42a
 */
public final class CollectionsModule__SRC implements AnnotatedSources {

	private static final Class<? extends CollectionsModule> MODULE_CLASS =
			CollectionsModule.class;

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
				"collections.o42a");
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[0];
	}

}
