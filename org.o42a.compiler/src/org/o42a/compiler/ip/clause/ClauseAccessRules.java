/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.clause;

import static org.o42a.core.member.AccessSource.FROM_CLAUSE_REUSE;
import static org.o42a.util.CheckResult.CHECK_ERROR;

import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.Container;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.CheckResult;


public final class ClauseAccessRules extends AccessRules {

	public static AccessRules clauseAccessRules(
			ClauseDeclaration declaration,
			AccessRules accessRules) {
		if (declaration.getContainer().toClause() != null) {
			// Not a top-level access rules.
			return accessRules;
		}
		if (declaration.getKind() != ClauseKind.EXPRESSION) {
			// Access rules are only special if top-level clause
			// is an expression.
			return accessRules;
		}
		return new ClauseAccessRules(declaration, accessRules);
	}

	private final AccessRules wrappedRules;
	private final ClauseDeclaration topLevelClause;

	private ClauseAccessRules(
			ClauseDeclaration topLevelClause,
			AccessRules wrappedRules) {
		super(wrappedRules.getSource());
		this.wrappedRules = wrappedRules;
		this.topLevelClause = topLevelClause;
	}

	@Override
	public Ref selfRef(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor) {
		if (isProhibitedObjectAccess(distributor.getContainer())) {
			prohibitObjectReuse(location);
			return null;
		}
		return defaultSelfRef(location, distributor);
	}

	@Override
	public CheckResult checkContainerAccessibility(
			LocationInfo location,
			Container from,
			Container to) {
		if (isProhibitedObjectAccess(to)) {
			prohibitObjectReuse(location);
			return CHECK_ERROR;
		}
		return this.wrappedRules.checkContainerAccessibility(
				location,
				from,
				to);
	}

	@Override
	public boolean containerIsVisible(Container by, Container what) {
		if (isProhibitedObjectAccess(what)) {
			return false;
		}
		return this.wrappedRules.containerIsVisible(by, what);
	}

	@Override
	public AccessRules typeRules() {

		final AccessRules typeRules = this.wrappedRules.typeRules();

		if (this.wrappedRules == typeRules) {
			return this;
		}

		return new ClauseAccessRules(this.topLevelClause, typeRules);
	}

	@Override
	public AccessRules declarationRules() {

		final AccessRules declarationRules =
				this.wrappedRules.declarationRules();

		if (this.wrappedRules == declarationRules) {
			return this;
		}

		return new ClauseAccessRules(this.topLevelClause, declarationRules);
	}

	@Override
	public AccessRules contentRules() {

		final AccessRules contentRules =
				this.wrappedRules.contentRules();

		if (this.wrappedRules == contentRules) {
			return this;
		}

		return new ClauseAccessRules(this.topLevelClause, contentRules);
	}

	@Override
	public AccessRules clauseReuseRules() {

		final AccessRules clauseReuseRules =
				this.wrappedRules.clauseReuseRules();

		if (this.wrappedRules == clauseReuseRules) {
			return this;
		}

		return new ClauseAccessRules(this.topLevelClause, clauseReuseRules);
	}

	@Override
	public String toString() {
		if (this.wrappedRules == null) {
			return super.toString();
		}
		return "FromClause[" + this.wrappedRules + ']';
	}

	private boolean isProhibitedObjectAccess(Container target) {
		if (getSource() != FROM_CLAUSE_REUSE) {
			// Access to object may be prohibited only when it is reused
			// as clause.
			return false;
		}
		return target.getScope().is(this.topLevelClause.getScope());
	}

	private void prohibitObjectReuse(LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_clause_object_reused",
				location,
				"Enclosing object can not be reused when top-level clause"
				+ " is an expression");
	}

}
