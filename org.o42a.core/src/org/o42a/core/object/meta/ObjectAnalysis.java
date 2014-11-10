/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.object.meta;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;
import static org.o42a.core.object.meta.EscapeDetectionMethod.ALWAYS_ESCAPE;
import static org.o42a.core.object.meta.EscapeDetectionMethod.ANCESTOR_ESCAPE;
import static org.o42a.core.object.meta.EscapeDetectionMethod.OBJECT_ESCAPE;
import static org.o42a.util.Misc.coalesce;
import static org.o42a.util.fn.Init.init;

import java.util.function.Function;

import org.o42a.core.member.Member;
import org.o42a.core.member.alias.MemberAlias;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.fn.Init;


public final class ObjectAnalysis {

	private static final Function<Obj, EscapeMode> OWN_ESCAPE_MODE =
			obj -> obj.analysis().ownEscapeMode();

	private final Obj object;

	private final Init<EscapeDetectionMethod> escapeDetectionMethod =
			init(this::chooseEscapeDetectionMethod);
	private final Init<EscapeMode> ownEscapeMode =
			init(this::detectOwnEscapeMode);
	private final Init<EscapeMode> overridersEscapeMode =
			init(() -> overridersEscapeMode(OWN_ESCAPE_MODE));
	private final Init<EscapeMode> derivativesEscapeMode =
			init(() -> derivativesEscapeMode(OWN_ESCAPE_MODE));

	ObjectAnalysis(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	/**
	 * Detects an escape mode of object ancestor expression.
	 *
	 * @return escape mode of ancestor access operation.
	 */
	public final EscapeMode ancestorEscapeMode() {
		if (!escapeDetectionMethod().alwaysEscapes()) {
			return ESCAPE_IMPOSSIBLE;
		}
		if (!escapeDetectionMethodByAncestor().alwaysEscapes()) {
			return ESCAPE_IMPOSSIBLE;
		}
		return ESCAPE_POSSIBLE;
	}

	/**
	 * An escape mode of the object itself.
	 *
	 * @return escape mode depending on ancestor purity, value definition
	 * escape mode, and members escape modes.
	 */
	public final EscapeMode ownEscapeMode() {
		return this.ownEscapeMode.get();
	}

	public final EscapeMode overridersEscapeMode() {
		return this.overridersEscapeMode.get();
	}

	public EscapeMode overridersEscapeMode(Function<Obj, EscapeMode> f) {
		return escapeDetectionMethod().overridersEscapeMode(
				getObject(),
				coalesce(f, OWN_ESCAPE_MODE));
	}

	public final EscapeMode derivativesEscapeMode() {
		return this.derivativesEscapeMode.get();
	}

	public EscapeMode derivativesEscapeMode(Function<Obj, EscapeMode> f) {
		return escapeDetectionMethod().derivativesEscapeMode(
				getObject(),
				coalesce(f, OWN_ESCAPE_MODE));
	}

	private final EscapeDetectionMethod escapeDetectionMethod() {
		return this.escapeDetectionMethod.get();
	}

	private EscapeDetectionMethod chooseEscapeDetectionMethod() {
		if (!getObject().value().getStatefulness().isStateless()) {
			// Stateful and eager objects always escape.
			return ALWAYS_ESCAPE;
		}
		return escapeDetectionMethodByAncestor();
	}

	private EscapeDetectionMethod escapeDetectionMethodByAncestor() {

		final TypeRef ancestor = getObject().type().getAncestor();

		if (ancestor == null) {
			// Void.
			return OBJECT_ESCAPE;
		}
		if (!ancestor.getRef()
				.purity(getObject().getScope().getEnclosingScope())
				.isPure()) {
			// Ancestor isn't pure.
			// Can not detect escape mode.
			return ALWAYS_ESCAPE;
		}
		if (ancestor.isStatic()) {
			// Static ancestor.
			// The value definition can be overridden by derivatives only.
			return OBJECT_ESCAPE;
		}
		if (getObject().value().getDefinitions().areInherited()) {
			// The value definition is inherited from relative ancestor.
			// So, it can be overridden by another ancestor without
			// overriding the object itself.
			return ANCESTOR_ESCAPE;
		}

		// The object has its own value definition.
		// It can be overridden by derivatives only.
		return OBJECT_ESCAPE;
	}

	private EscapeMode detectOwnEscapeMode() {
		if (escapeDetectionMethod().alwaysEscapes()) {
			return ESCAPE_POSSIBLE;
		}

		final EscapeMode valueEscapeMode =
				getObject()
				.type()
				.getValueType()
				.valueEscapeMode()
				.valueEscapeMode(getObject());

		if (valueEscapeMode.isEscapePossible()) {
			return valueEscapeMode;
		}

		return membersEscapeMode();
	}

	private EscapeMode membersEscapeMode() {

		EscapeMode escapeMode = ESCAPE_IMPOSSIBLE;

		for (Member member : getObject().getMembers()) {

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
			return field.field(dummyUser())
					.toObject()
					.analysis()
					.ownEscapeMode();
		}

		final MemberAlias alias = member.toAlias();

		if (alias != null) {
			if (alias.getRef().purity(getObject().getScope()).isPure()) {
				return ESCAPE_IMPOSSIBLE;
			}
			return ESCAPE_POSSIBLE;
		}

		return ESCAPE_IMPOSSIBLE;
	}

}
