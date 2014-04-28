// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.console;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


/**
 * o42a sources for {@link ConsoleModule}.
 * 
 * File: console.o42a
 */
public final class ConsoleModule__SRC implements AnnotatedSources {

	private static final Class<? extends ConsoleModule> MODULE_CLASS =
			ConsoleModule.class;

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
				"console.o42a");
	}

	@Override
	public Field[] fields(Obj owner) {
		return new Field[] {
			new org.o42a.lib.console.impl.Print(
					owner,
					new org.o42a.lib.console.impl.Print__SRC(this))
			.getScope().toField(),
			new org.o42a.lib.console.impl.PrintError(
					owner,
					new org.o42a.lib.console.impl.PrintError__SRC(this))
			.getScope().toField(),
		};
	}

}
