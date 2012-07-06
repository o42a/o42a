/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.compiler.ip.Interpreter.CLAUSE_DECL_IP;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.path.Path.FALSE_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.VOID_PATH;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.PlainClause;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.common.PlacedPathFragment;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.LocationInfo;


public class MemberById extends PlacedPathFragment {

	private static final MemberName VOID_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("void"));
	private static final MemberName FALSE_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("false"));

	public static boolean prototypeExpressionClause(Container container) {

		final Clause clause = container.toClause();

		if (clause == null) {
			return false;
		}

		final PlainClause plainClause = clause.toPlainClause();

		if (plainClause == null || plainClause.isAssignment()) {
			return false;
		}

		final Container parentContainer = container.getParentContainer();

		if (parentContainer.toClause() != null) {
			return false;
		}

		return parentContainer.toObject().isPrototype();
	}

	private final Interpreter ip;
	private final StaticTypeRef declaredIn;
	private final MemberId memberId;

	public MemberById(
			Interpreter ip,
			LocationInfo location,
			Distributor distributor,
			MemberId memberId,
			StaticTypeRef declaredIn) {
		super(location, distributor);
		this.ip = ip;
		this.memberId = memberId;
		this.declaredIn = declaredIn;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Obj declaredIn;

		if (this.declaredIn != null) {
			declaredIn = this.declaredIn.getType();
		} else {
			declaredIn = null;
		}

		return path(getContainer(), declaredIn, false);
	}

	@Override
	public String toString() {
		if (this.memberId == null) {
			return super.toString();
		}
		return this.memberId.toString();
	}

	private Path path(
			Container container,
			Obj declaredIn,
			boolean excludeContainer) {
		if (!excludeContainer) {

			final Path memberOfContainer =
					memberOfContainer(container, declaredIn);

			if (memberOfContainer != null) {
				return memberOfContainer;
			}
		}

		final Container enclosing = container.getEnclosingContainer();

		if (enclosing == null) {
			if (declaredIn == null) {
				if (this.memberId.equals(VOID_MEMBER)) {
					return VOID_PATH;
				}
				if (this.memberId.equals(FALSE_MEMBER)) {
					return FALSE_PATH;
				}
			}

			getLogger().unresolved(this, this.memberId);

			return null;
		}

		// Top-level expression clause
		// shouldn't have access to enclosing prototype.
		final boolean excludeEnclosingContainer =
				this.ip != CLAUSE_DECL_IP
				&& prototypeExpressionClause(container);

		final Path found =
				path(enclosing, declaredIn, excludeEnclosingContainer);

		if (found == null) {
			return null;
		}
		if (found.isAbsolute()) {
			return found;
		}

		final Scope enclosingScope = enclosing.getScope();

		if (enclosingScope.is(container.getScope())) {
			return found;
		}

		final PathResolution pathResolution =
				found.bind(this, enclosingScope).resolve(
						pathResolver(enclosingScope, dummyUser()));

		if (!pathResolution.isResolved()) {
			return null;
		}
		if (pathResolution.getResult().getScope().is(container.getScope())) {
			return SELF_PATH;
		}

		final Path enclosingScopePath =
				container.getScope().getEnclosingScopePath();

		assert enclosingScopePath != null :
			found + " should be an absolute path";

		return enclosingScopePath.append(found);
	}

	private Path memberOfContainer(Container container, Obj declaredIn) {

		final Accessor accessor;

		if (getScope().is(container.getScope())) {
			accessor = Accessor.OWNER;
		} else if (container.getContext().declarationsVisibleFrom(
				getContext())) {
			accessor = Accessor.DECLARATION;
		} else {
			accessor = Accessor.ENCLOSED;
		}

		return container.findMember(this, accessor, this.memberId, declaredIn);
	}


}
