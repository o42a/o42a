/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.object;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;
import static org.o42a.util.fn.Init.init;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.alias.MemberAlias;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.meta.ObjectMeta;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.fn.Init;


public final class Meta extends ObjectMeta {

	private final Obj object;
	private final Init<Nesting> nesting =
			init(() -> getObject().createNesting());
	private final Init<EscapeMode> escapeMode = init(this::detectEscapeMode);

	Meta(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Meta getParentMeta() {

		final Scope enclosingScope =
				getObject().getScope().getEnclosingScope();
		final Obj enclosingObject = enclosingScope.toObject();

		if (enclosingObject != null) {
			return enclosingObject.meta();
		}

		final Member enclosingMember = enclosingScope.toMember();

		if (enclosingMember != null) {
			return enclosingMember.getMemberOwner().meta();
		}

		return null;
	}

	@Override
	public boolean isUpdated() {
		getObject().resolveMembers(true);
		getObject().type().resolve(false);
		return super.isUpdated();
	}

	public final Nesting getNesting() {
		return this.nesting.get();
	}

	public final Obj findIn(Scope enclosing) {

		final Scope enclosingScope = getObject().getScope().getEnclosingScope();

		if (enclosingScope.is(enclosing)) {
			return getObject();
		}

		enclosing.assertDerivedFrom(enclosingScope);

		return getNesting().findObjectIn(enclosing);
	}

	public final EscapeMode getEscapeMode() {
		return this.escapeMode.get();
	}

	public final boolean is(Meta meta) {
		return this == meta;
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "Meta[" + this.object + ']';
	}

	private EscapeMode detectEscapeMode() {

		final TypeRef ancestor = getObject().type().getAncestor();

		if (ancestor != null && !ancestor.getRef().getPurity().isPure()) {
			return ESCAPE_POSSIBLE;
		}

		final EscapeMode defEscapeMode =
				getObject().value().getDefinitions().getEscapeMode();

		if (defEscapeMode.isEscapePossible()) {
			return defEscapeMode;
		}

		return membersEscapeMode();
	}

	private EscapeMode membersEscapeMode() {

		EscapeMode escapeMode = ESCAPE_IMPOSSIBLE;

		for (Member member : getObject().members().values()) {

			final MemberField field = member.toField();

			if (field != null) {
				escapeMode = escapeMode.combine(memberEscapeMode(member));
				if (escapeMode.isEscapePossible()) {
					break;
				}
			}
		}

		return escapeMode;
	}

	private EscapeMode memberEscapeMode(Member member) {

		final MemberField field = member.toField();

		if (field != null) {
			return field.field(dummyUser()).toObject().meta().getEscapeMode();
		}

		final MemberAlias alias = member.toAlias();

		if (alias != null) {
			if (alias.getRef().getPurity().isPure()) {
				return ESCAPE_IMPOSSIBLE;
			}
			return ESCAPE_POSSIBLE;
		}

		return ESCAPE_IMPOSSIBLE;
	}

}
