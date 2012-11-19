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
import static org.o42a.core.value.macro.MacroConsumer.DEFAULT_CONSUMER;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.core.member.MemberName;
import org.o42a.core.object.Meta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.value.macro.RefDep;


final class LinkSubjectDep extends RefDep<SubjectMetaDep> {

	private static final MemberName LINK_NAME =
			fieldName(CASE_SENSITIVE.canonicalName("link"));
	private static final LinkSubjectDep INSTANCE = new LinkSubjectDep();

	static Ref linkRef(LinkInterface macro) {

		final Ref linkRef =
				expandMacro(LINK_NAME.key(macro.getScope()).toPath())
				.bind(macro, macro.getScope())
				.target(macro.distribute());
		final Ref ref = linkRef.consume(DEFAULT_CONSUMER);
		final SubjectMetaDep dep = INSTANCE.buildDep(ref, null);

		if (dep != null) {
			dep.register();
		}

		return ref;
	}

	private LinkSubjectDep() {
	}

	@Override
	public SubjectMetaDep newDep(Meta meta, Ref ref, PathTemplate template) {
		return new SubjectMetaDep(meta, ref);
	}

	@Override
	public void setParentDep(SubjectMetaDep dep, MetaDep parentDep) {
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
