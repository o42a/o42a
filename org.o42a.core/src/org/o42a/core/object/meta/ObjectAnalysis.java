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

import static org.o42a.analysis.escape.EscapeInit.escapeInit;
import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.meta.DetectEscapeFlag.OWN_ESCAPE_MODE;
import static org.o42a.core.object.meta.EscapeDetectionMethod.ALWAYS_ESCAPE;
import static org.o42a.core.object.meta.EscapeDetectionMethod.ANCESTOR_ESCAPE;
import static org.o42a.core.object.meta.EscapeDetectionMethod.OBJECT_ESCAPE;
import static org.o42a.util.fn.Init.init;

import org.o42a.analysis.escape.*;
import org.o42a.core.member.Member;
import org.o42a.core.member.alias.MemberAlias;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.fn.Init;


public final class ObjectAnalysis {

	private final Obj object;

	private final Init<EscapeDetectionMethod> escapeDetectionMethod =
			init(this::chooseEscapeDetectionMethod);
	private final EscapeInit ownEscapeMode =
			escapeInit(this::detectOwnEscapeMode);
	private final EscapeInit overridersEscapeMode =
			escapeInit(a -> overridersEscapeFlag(a, OWN_ESCAPE_MODE));
	private final EscapeInit derivativesEscapeMode =
			escapeInit(a -> derivativesEscapeFlag(a, OWN_ESCAPE_MODE));

	ObjectAnalysis(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	/**
	 * Detects an escape mode of object ancestor expression.
	 *
	 * @param analyzer escape mode analyzer.
	 *
	 * @return escape mode of ancestor access operation.
	 */
	public final EscapeMode ancestorEscapeMode(EscapeAnalyzer analyzer) {
		return ancestorEscapeFlag(analyzer).getEscapeMode();
	}

	public final EscapeFlag ancestorEscapeFlag(EscapeAnalyzer analyzer) {
		if (!escapeDetectionMethod().alwaysEscapes()) {
			return analyzer.escapeImpossible();
		}
		if (!escapeDetectionMethodByAncestor().alwaysEscapes()) {
			return analyzer.escapeImpossible();
		}
		return analyzer.escapePossible();
	}

	/**
	 * Detect an escape mode of object value definition.
	 *
	 * @param analyzer escape mode analyzer.
	 *
	 * @return escape mode of value definition statements.
	 */
	public final EscapeMode valueEscapeMode(EscapeAnalyzer analyzer) {
		return valueEscapeFlag(analyzer).getEscapeMode();
	}

	public final EscapeFlag valueEscapeFlag(EscapeAnalyzer analyzer) {
		if (escapeDetectionMethod().alwaysEscapes()) {
			return analyzer.escapePossible();
		}
		return getObject()
				.type()
				.getValueType()
				.valueEscapeMode()
				.valueEscapeFlag(analyzer, getObject());
	}

	/**
	 * An escape mode of the object itself.
	 *
	 * @param analyzer escape mode analyzer.
	 *
	 * @return escape mode depending on ancestor purity, value definition
	 * escape mode, and members escape modes.
	 */
	public final EscapeMode ownEscapeMode(EscapeAnalyzer analyzer) {
		return this.ownEscapeMode.escapeMode(analyzer);
	}

	public final EscapeFlag ownEscapeFlag(EscapeAnalyzer analyzer) {
		return this.ownEscapeMode.escapeFlag(analyzer);
	}

	public final EscapeMode overridersEscapeMode(EscapeAnalyzer analyzer) {
		return this.overridersEscapeMode.escapeMode(analyzer);
	}

	public final EscapeFlag overridersEscapeFlag(EscapeAnalyzer analyzer) {
		return this.overridersEscapeMode.escapeFlag(analyzer);
	}

	public final EscapeFlag overridersEscapeFlag(
			EscapeAnalyzer analyzer,
			DetectEscapeFlag detect) {
		return escapeDetectionMethod(detect).overridersEscapeFlag(
				analyzer,
				getObject(),
				detect);
	}

	public final EscapeMode derivativesEscapeMode(EscapeAnalyzer analyzer) {
		return this.derivativesEscapeMode.escapeMode(analyzer);
	}

	public final EscapeFlag derivativesEscapeFlag(EscapeAnalyzer analyzer) {
		return this.derivativesEscapeMode.escapeFlag(analyzer);
	}

	public EscapeFlag derivativesEscapeFlag(
			EscapeAnalyzer analyzer,
			DetectEscapeFlag detect) {
		return escapeDetectionMethod(detect).derivativesEscapeFlag(
				analyzer,
				getObject(),
				detect);
	}

	private final EscapeDetectionMethod escapeDetectionMethod() {
		return this.escapeDetectionMethod.get();
	}

	private final EscapeDetectionMethod escapeDetectionMethod(
			DetectEscapeFlag detect) {

		final EscapeDetectionMethod method = escapeDetectionMethod();

		if (!detect.objectDefinitionsIgnored()) {
			return method;
		}

		return method.ignoreObjectDefinitions();
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

	private EscapeFlag detectOwnEscapeMode(EscapeAnalyzer analyzer) {
		if (this.ownEscapeMode.check(this::valueEscapeFlag)) {
			return this.ownEscapeMode.lastFlag();
		}
		if (checkMembersEscape()) {
			return this.ownEscapeMode.lastFlag();
		}
		return analyzer.escapeImpossible();
	}

	private boolean checkMembersEscape() {
		for (Member member : getObject().getMembers()) {
			if (checkMemberEscape(member)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkMemberEscape(Member member) {

		final MemberField field = member.toField();

		if (field != null) {

			final Obj object = field.field(dummyUser()).toObject();

			if (field.isPrototype()) {
				// Prototype values do not affect parent escape mode.
				return this.ownEscapeMode.check(
						object.analysis()::ancestorEscapeFlag);
			}

			return this.ownEscapeMode.check(object.analysis()::ownEscapeFlag);
		}

		final MemberAlias alias = member.toAlias();

		if (alias != null) {
			return this.ownEscapeMode.check(
					a -> alias.getRef().purity(getObject().getScope()).isPure()
					? a.escapeImpossible()
					: a.escapePossible());
		}

		return false;
	}

}
