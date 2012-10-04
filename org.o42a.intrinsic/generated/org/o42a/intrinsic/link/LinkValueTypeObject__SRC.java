// GENERATED FILE. DO NOT MODIFY.
package org.o42a.intrinsic.link;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.source.*;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;


/**
 * o42a sources for {@link LinkValueTypeObject}.
 * 
 * File: root/link.o42a
 */
public final class LinkValueTypeObject__SRC implements AnnotatedSources {

	private final AnnotatedSources parent;
	private URLSources sourceTree;

	public LinkValueTypeObject__SRC(AnnotatedSources parent) {
		this.parent = parent;
	}

	@Override
	public URLSources getSourceTree() {
		if (this.sourceTree != null) {
			return this.sourceTree;
		}

		this.sourceTree = new URLSources(
				this.parent.getSourceTree(),
				"link.o42a");

		this.sourceTree.add("interface__.o42a");

		return this.sourceTree;
	}

	@Override
	public Field[] fields(MemberOwner owner) {
		return new Field[] {
			new org.o42a.intrinsic.link.LinkCast(
					owner,
					new org.o42a.intrinsic.link.LinkCast__SRC(this))
			.getScope().toField(),
		};
	}

}
