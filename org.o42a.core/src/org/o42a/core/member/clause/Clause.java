/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member.clause;

import java.util.HashMap;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;
import org.o42a.util.log.Loggable;


public abstract class Clause implements PlaceInfo {

	protected static final ReusedClause[] NOTHING_REUSED = new ReusedClause[0];

	public static boolean validateImplicitSubClauses(Clause[] subClauses) {

		boolean result = true;
		final HashMap<MemberId, Clause> implicitClauses =
				new HashMap<MemberId, Clause>();

		for (Clause subClause : subClauses) {
			if (!validateImplicitSubClauses(implicitClauses, subClause)) {
				result = false;
			}
		}

		return result;
	}

	private static boolean validateImplicitSubClauses(
			HashMap<MemberId, Clause> implicitClauses,
			Clause clause) {

		boolean result = true;

		for (Clause subClause : clause.getSubClauses()) {
			if (subClause.isImplicit()) {
				validateImplicitSubClauses(implicitClauses, subClause);
				continue;
			}

			final MemberId memberId = subClause.getKey().getMemberId();

			if (!memberId.isValid()) {
				continue;
			}

			final Clause conflicting = implicitClauses.put(memberId, subClause);

			if (conflicting == null) {
				continue;
			}

			result = false;
			implicitClauses.put(memberId, conflicting);
			clause.getContext().getLogger().ambiguousClause(
					subClause,
					subClause.getDisplayName());
		}

		return result;
	}

	private final MemberClause member;
	private final ClauseDeclaration declaration;
	private Clause enclosingClause;
	private Obj enclosingObject;
	private Path pathInObject;
	private Clause[] implicitClauses;
	private boolean allResolved;

	public Clause(MemberClause member) {
		this.member = member;
		this.declaration = member.getDeclaration();
	}

	protected Clause(
			MemberOwner owner,
			Clause overridden,
			Clause wrapped,
			OverrideMode mode) {

		final OverriddenMemberClause member = new OverriddenMemberClause(
				owner,
				this,
				overridden.toMember(),
				wrapped != null ? wrapped.toMember() : null,
				mode.propagation(overridden.toMember()));

		this.member = member;
		this.declaration = member.getDeclaration();
	}

	@Override
	public final CompilerContext getContext() {
		return this.member.getContext();
	}

	@Override
	public Loggable getLoggable() {
		return this.member.getLoggable();
	}

	@Override
	public final ScopePlace getPlace() {
		return this.member.getPlace();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	public abstract Scope getEnclosingScope();

	public abstract ClauseContainer getClauseContainer();

	public final Clause getEnclosingClause() {
		if (this.enclosingClause != null) {
			return this.enclosingClause;
		}

		final Scope enclosingScope = getEnclosingScope();
		final MemberKey enclosingKey = getKey().getEnclosingKey();

		if (enclosingKey != null) {

			final Member enclosingMember =
					enclosingScope.getContainer().member(enclosingKey);

			assert enclosingMember != null :
				"Member " + enclosingKey + " not found in " + enclosingScope;

			this.enclosingClause = enclosingMember.toClause();

			assert this.enclosingClause != null :
				enclosingMember + " is not a clause";

			return this.enclosingClause;
		}

		// Return enclosing scope, if it's clause.
		return enclosingScope.getContainer().toClause();
	}

	public final ClauseKind getKind() {
		return getDeclaration().getKind();
	}

	public abstract boolean requiresInstance();

	public final boolean hasContinuation() {
		return getClauseContainer().hasSubClauses();
	}

	public final boolean requiresContinuation() {
		return isImplicit() || getDeclaration().requiresContinuation();
	}

	public final boolean isImplicit() {
		return getDeclaration().isImplicit();
	}

	public abstract boolean isMandatory();

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	public final MemberKey getKey() {
		return this.member.getKey();
	}

	public final String getName() {
		return getDeclaration().getName();
	}

	public final String getDisplayName() {
		return this.member.getDisplayName();
	}

	public final MemberClause toMember() {
		return this.member;
	}

	public abstract PlainClause toPlainClause();

	public abstract GroupClause toGroupClause();

	public final Clause[] getImplicitClauses() {
		if (this.implicitClauses != null) {
			return this.implicitClauses;
		}

		Clause[] implicitClauses = new Clause[0];

		for (Clause clause : getSubClauses()) {
			if (clause.isImplicit()) {
				implicitClauses = ArrayUtil.append(implicitClauses, clause);
			}
		}

		return this.implicitClauses = implicitClauses;
	}

	public abstract Clause[] getSubClauses();

	public abstract ReusedClause[] getReusedClauses();

	public final Obj getEnclosingObject() {
		if (this.enclosingObject != null) {
			return this.enclosingObject;
		}

		final Scope enclosingScope = getEnclosingScope();
		final Clause clause = enclosingScope.getContainer().toClause();

		if (clause != null) {
			return this.enclosingObject = clause.getEnclosingObject();
		}

		this.enclosingObject = enclosingScope.toObject();

		assert this.enclosingObject != null :
			"Enclosing object not found: " + this;

		return this.enclosingObject;
	}

	public final Path pathInObject() {
		if (this.pathInObject != null) {
			return this.pathInObject;
		}
		return this.pathInObject = buildPathInObject();
	}

	public abstract void define(Reproducer reproducer);

	public final void resolveAll() {
		if (this.allResolved) {
			return;
		}
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			fullyResolve();
		} finally {
			getContext().fullResolution().end();
		}
	}

	@Override
	public Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		return this.member.toString();
	}

	protected abstract Clause propagate(MemberOwner owner);

	protected abstract void fullyResolve();

	protected void validate() {
		getReusedClauses();
		if (!isImplicit()) {
			validateImplicitSubClauses(getSubClauses());
		}
		if (requiresContinuation() && !hasContinuation()) {
			getLogger().error(
					"missing_clause_continuation",
					this,
					"Required clause continuation is missing");
		}
	}

	protected Path buildPathInObject() {

		final Member member = toMember();
		final Scope enclosingScope = getEnclosingScope();
		final Clause enclosingClause = enclosingScope.getContainer().toClause();

		if (enclosingClause == null) {
			assert enclosingScope.toObject() != null :
				this + " is not inside of object";
			return member.getKey().toPath();
		}

		return enclosingClause.pathInObject().append(member.getKey());
	}

}
