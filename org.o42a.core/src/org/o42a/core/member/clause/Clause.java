/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.core.ContainerInfo;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.Name;


public abstract class Clause implements ContainerInfo {

	protected static final ReusedClause[] NOTHING_REUSED = new ReusedClause[0];

	public static boolean validateImplicitSubClauses(
			MemberClause[] subClauses) {

		final HashMap<MemberId, MemberClause> explicitClauses = new HashMap<>();

		return validateImplicitSubClauses(explicitClauses, subClauses);
	}

	private static boolean validateImplicitSubClauses(
			HashMap<MemberId, MemberClause> explicitClauses,
			MemberClause[] subClauses) {

		boolean result = true;

		for (MemberClause subClause : subClauses) {
			if (!validateClause(explicitClauses, subClause)) {
				result = false;
			}
		}

		return result;
	}

	private static boolean validateClause(
			HashMap<MemberId, MemberClause> explicitClauses,
			MemberClause clause) {

		final MemberId memberId =
				clause.getMemberKey().getMemberId().getLocalId();

		if (!memberId.isValid()) {
			return true;
		}
		if (clause.isImplicit()) {
			return validateImplicitSubClauses(
					explicitClauses,
					clause.clause().getSubClauses());
		}

		final MemberClause conflicting = explicitClauses.put(memberId, clause);

		if (conflicting == null) {
			return true;
		}

		explicitClauses.put(memberId, conflicting);
		clause.getContext().getLogger().ambiguousClause(
				clause.getLocation(),
				clause.getDisplayName());

		return false;
	}

	private final MemberClause member;
	private Clause enclosingClause;
	private Clause topClause;
	private Path pathInObject;
	private MemberClause[] implicitClauses;
	private boolean allResolved;

	public Clause(MemberClause member) {
		this.member = member;
	}

	@Override
	public final Location getLocation() {
		return this.member.getLocation();
	}

	public abstract Scope getEnclosingScope();

	public abstract ClauseContainer getClauseContainer();

	public final boolean isTopLevel() {
		return getEnclosingClause() == null;
	}

	public final Clause getEnclosingClause() {
		if (this.enclosingClause != null) {
			return this.enclosingClause;
		}

		final Scope enclosingScope = getEnclosingScope();
		final MemberKey enclosingKey = getKey().getEnclosingKey();

		if (enclosingKey == null) {
			// Return enclosing scope, if it's clause.
			return enclosingScope.getContainer().toClause();
		}

		final Member enclosingMember =
				enclosingScope.getContainer().member(enclosingKey);

		assert enclosingMember != null :
			"Member " + enclosingKey + " not found in " + enclosingScope;

		this.enclosingClause = enclosingMember.toClause().clause();

		assert this.enclosingClause != null :
			enclosingMember + " is not a clause";

		return this.enclosingClause;
	}

	public final ClauseKind getKind() {
		return toMember().getKind();
	}

	public abstract boolean requiresInstance();

	public final boolean hasContinuation() {
		if (getReusedClauses().length != 0) {
			return true;
		}
		return getClauseContainer().hasSubClauses();
	}

	public final boolean requiresContinuation() {
		return isImplicit() || getDeclaration().requiresContinuation();
	}

	public final boolean isTerminator() {
		return getDeclaration().isTerminator();
	}

	public final boolean isImplicit() {
		return toMember().isImplicit();
	}

	public abstract boolean isMandatory();

	public final ClauseDeclaration getDeclaration() {
		return this.member.getDeclaration();
	}

	public final MemberKey getKey() {
		return this.member.getMemberKey();
	}

	public final Name getName() {
		return getDeclaration().getName();
	}

	public final String getDisplayName() {
		return this.member.getDisplayName();
	}

	public abstract ClauseSubstitution getSubstitution();

	public final MemberClause toMember() {
		return this.member;
	}

	public abstract PlainClause toPlainClause();

	public abstract GroupClause toGroupClause();

	public final MemberClause[] getImplicitClauses() {
		if (this.implicitClauses != null) {
			return this.implicitClauses;
		}

		MemberClause[] implicitClauses = new MemberClause[0];

		for (MemberClause clause : getSubClauses()) {
			if (clause.isImplicit()) {
				implicitClauses = ArrayUtil.append(implicitClauses, clause);
			}
		}

		return this.implicitClauses = implicitClauses;
	}

	public abstract MemberClause[] getSubClauses();

	public abstract ReusedClause[] getReusedClauses();

	public final Obj getEnclosingObject() {
		return getTopClause().getEnclosingScope().toObject();
	}

	public final Clause getTopClause() {
		if (this.topClause != null) {
			return this.topClause;
		}

		final Clause enclosingClause = getEnclosingClause();

		if (enclosingClause != null) {
			return this.topClause = enclosingClause.getTopClause();
		}

		return this.topClause = this;
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
		if (toMember().isPropagated()) {
			return;
		}
		getContext().fullResolution().start();
		try {
			fullyResolve();
		} finally {
			getContext().fullResolution().end();
		}
	}

	@Override
	public String toString() {
		return this.member.toString();
	}

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
		getDeclaration().getClauseId().validateClause(this);
	}

	protected Path buildPathInObject() {

		final Member member = toMember();
		final Scope enclosingScope = getEnclosingScope();
		final Clause enclosingClause = enclosingScope.getContainer().toClause();

		if (enclosingClause == null) {
			assert enclosingScope.toObject() != null :
				this + " is not inside of object";
			return member.getMemberKey().toPath();
		}

		return enclosingClause.pathInObject().append(member.getMemberKey());
	}

}
