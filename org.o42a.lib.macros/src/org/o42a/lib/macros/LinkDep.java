/*
    Standard Macros
    Copyright (C) 2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.lib.macros;

import static org.o42a.common.macro.Macros.expandMacro;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.object.macro.MacroConsumer.DEFAULT_CONSUMER;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.common.macro.RefDep;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Meta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.MetaKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;


final class LinkDep extends RefDep<LinkMetaDep> implements MetaKey {

	private static final MemberName LINK_NAME =
			fieldName(CASE_SENSITIVE.canonicalName("link"));
	private static final LinkDep INSTANCE = new LinkDep();

	static Ref linkRef(LinkInterface macro) {

		final Ref linkRef =
				expandMacro(LINK_NAME.key(macro.getScope()).toPath())
				.bind(macro, macro.getScope())
				.target(macro.distribute());
		final LinkMetaDep dep = INSTANCE.buildDep(
				linkRef.consume(DEFAULT_CONSUMER),
				null);

		dep.register();

		return dep.getLinkRef();
	}

	private LinkDep() {
	}

	@Override
	public LinkMetaDep newDep(Meta meta, Ref ref, PathTemplate template) {
		return new LinkMetaDep(meta, this, ref);
	}

	@Override
	public void setParentDep(LinkMetaDep dep, MetaDep parentDep) {
		dep.setParentDep(parentDep);
	}

	@Override
	public void invalidRef(Ref ref) {
		ref.getLogger().error(
				"invalid_link_meta_dep",
				ref,
				"Invalid link meta-reference");
	}

}
