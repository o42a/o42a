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

	private URLSources sourceTree;

	@Override
	public URLSources getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}

		this.sourceTree = new URLSources(
				null,
				base(),
				"collections.o42a");

		this.sourceTree.add("collection.o42a");
		this.sourceTree.add("iterator.o42a");
		this.sourceTree.add("list.o42a");

		return this.sourceTree;
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[0];
	}

}
