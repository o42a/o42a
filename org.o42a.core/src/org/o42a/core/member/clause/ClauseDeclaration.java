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

import static org.o42a.core.member.MemberIdKind.CLAUSE_NAME;

import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public class ClauseDeclaration extends Contained implements Cloneable {

	public static ClauseDeclaration clauseDeclaration(
			LocationInfo location,
			Distributor distributor,
			Name name,
			ClauseId clauseId) {
		return new ClauseDeclaration(
				location,
				distributor,
				name,
				clauseId);
	}

	public static ClauseDeclaration anonymousClauseDeclaration(
			LocationInfo location,
			Distributor distributor) {
		return new ClauseDeclaration(location, distributor);
	}

	private Name name;
	private ClauseId clauseId;
	private MemberId groupId;
	private ClauseKind kind;
	private boolean requiresContinuation;
	private boolean terminator;
	private boolean implicit;
	private boolean internal;

	private ClauseDeclaration(
			LocationInfo location,
			Distributor distributor,
			Name name,
			ClauseId clauseId) {
		super(location, distributor);
		assert clauseId != null :
			"Clause identifier not specified";
		this.name = name;
		this.clauseId = clauseId;
		this.kind = ClauseKind.EXPRESSION;
	}

	private ClauseDeclaration(
			LocationInfo location,
			Distributor distributor,
			ClauseDeclaration prototype) {
		super(location, distributor);
		this.name = prototype.name;
		this.clauseId = prototype.getClauseId();
		this.groupId = prototype.groupId;
		this.kind = prototype.kind;
		this.requiresContinuation = prototype.requiresContinuation;
		this.terminator = prototype.terminator;
		this.implicit = prototype.implicit;
		this.internal = prototype.internal;
	}

	private ClauseDeclaration(LocationInfo location, Distributor distributor) {
		super(location, distributor);
		this.name = null;
		this.clauseId = ClauseId.NAME;
		this.kind = ClauseKind.EXPRESSION;
	}

	public final boolean isAnonymous() {
		return getClauseId().isName() && getName() == null;
	}

	public final Name getName() {
		return this.name;
	}

	public final MemberId getMemberId() {

		final MemberId localId;
		final ClauseId clauseId = getClauseId();

		if (clauseId.isName()) {
			localId = CLAUSE_NAME.memberName(getName());
		} else {
			localId = clauseId.getMemberId();
		}

		if (this.groupId == null) {
			return localId;
		}

		return this.groupId.append(localId);
	}

	public final ClauseDeclaration setName(Name name) {

		final ClauseDeclaration clone = clone();

		clone.clauseId = ClauseId.NAME;
		clone.name = name;

		return clone;
	}

	public final ClauseId getClauseId() {
		return this.clauseId;
	}

	public final String getDisplayName() {
		return getClauseId().toString(getMemberId(), this.name);
	}

	public final ClauseKind getKind() {
		return this.kind;
	}

	public final ClauseDeclaration setKind(ClauseKind kind) {

		final ClauseDeclaration clone = clone();

		clone.kind = kind;

		return clone;
	}

	public final boolean requiresContinuation() {
		return this.requiresContinuation;
	}

	public final ClauseDeclaration requireContinuation() {

		final ClauseDeclaration clone = clone();

		clone.requiresContinuation = true;

		return clone;
	}

	public final boolean isTerminator() {
		return this.terminator;
	}

	public final ClauseDeclaration terminator() {

		final ClauseDeclaration clone = clone();

		clone.terminator = true;

		return clone;
	}

	public final boolean isImplicit() {
		return this.implicit;
	}

	public final ClauseDeclaration implicit() {

		final ClauseDeclaration clone = clone();

		clone.implicit = true;

		return clone;
	}

	public final boolean isInternal() {
		return this.internal;
	}

	public final ClauseDeclaration internal() {

		final ClauseDeclaration clone = clone();

		clone.internal = true;

		return clone;
	}

	public ClauseDeclaration inGroup(MemberId groupId) {

		final ClauseDeclaration clone = clone();

		if (clone.groupId != null) {
			clone.groupId = groupId.append(clone.groupId);
		} else {
			clone.groupId = groupId;
		}

		return clone;
	}

	@Override
	public String toString() {
		return "Clause " + getDisplayName();
	}

	@Override
	protected ClauseDeclaration clone() {
		try {
			return (ClauseDeclaration) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	ClauseDeclaration overrideBy(Member overrider) {
		return new ClauseDeclaration(
				overrider,
				distributeIn(overrider.getContainer()),
				this);
	}


}
