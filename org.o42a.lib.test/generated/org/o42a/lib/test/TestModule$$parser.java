// GENERATED FILE. DO NOT MODIFY.
package org.o42a.lib.test;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * Directory: test/parser/
 */
public final class TestModule$$parser implements AnnotatedSources {

	private final AnnotatedSources parent;
	private SingleURLSource sourceTree;

	public TestModule$$parser(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public SingleURLSource getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}
		return this.sourceTree = new SingleURLSource(
				this.parent.getSourceTree(),
				"parser/");
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[] {
			new org.o42a.lib.test.rt.parser.ParseFloat(
					owner,
					new org.o42a.lib.test.rt.parser.ParseFloat$$Sources(this))
			.getScope().toField(),
			new org.o42a.lib.test.rt.parser.ParseInteger(
					owner,
					new org.o42a.lib.test.rt.parser.ParseInteger$$Sources(this))
			.getScope().toField(),
			new org.o42a.lib.test.rt.parser.ParseString(
					owner,
					new org.o42a.lib.test.rt.parser.ParseString$$Sources(this))
			.getScope().toField(),
		};
	}

}
