/*
    Compiler Core
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
package org.o42a.core.member.clause;

import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberName.clauseName;
import static org.o42a.core.member.clause.ClauseId.byAdapterType;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.member.*;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public class ClauseDeclaration extends Placed implements Cloneable {

	public static ClauseDeclaration clauseDeclaration(
			LocationInfo location,
			Distributor distributor,
			Name name,
			ClauseId clauseId) {
		return new ClauseDeclaration(
				location,
				distributor,
				name,
				null,
				clauseId);
	}

	public static ClauseDeclaration clauseDeclaration(
			LocationInfo location,
			Distributor distributor,
			Name name,
			MemberId memberId) {
		return new ClauseDeclaration(
				location,
				distributor,
				name,
				memberId,
				null);
	}

	public static ClauseDeclaration anonymousClauseDeclaration(
			LocationInfo location,
			Distributor distributor) {
		return new ClauseDeclaration(location, distributor);
	}

	private Name name;
	private MemberId memberId;
	private MemberId groupId;
	private ClauseId clauseId;
	private ClauseKind kind;
	private boolean requiresContinuation;
	private boolean terminator;
	private boolean implicit;
	private boolean internal;

	private ClauseDeclaration(
			LocationInfo location,
			Distributor distributor,
			Name name,
			MemberId memberId,
			ClauseId clauseId) {
		super(location, distributor);
		assert memberId != null || clauseId != null :
			"Either member identifier or clause kind should be specified";
		this.name = name;
		this.memberId = memberId;
		this.clauseId = clauseId;
		this.kind = ClauseKind.EXPRESSION;
	}

	private ClauseDeclaration(
			LocationInfo location,
			Distributor distributor,
			ClauseDeclaration prototype) {
		super(location, distributor);
		this.name = prototype.name;
		this.memberId = prototype.memberId;
		this.clauseId = prototype.clauseId;
		this.kind = prototype.kind;
		this.requiresContinuation = prototype.requiresContinuation;
	}

	private ClauseDeclaration(
			LocationInfo location,
			Distributor distributor) {
		super(location, distributor);
		this.name = null;
		this.memberId = null;
		this.clauseId = ClauseId.NAME;
		this.kind = ClauseKind.EXPRESSION;
	}

	public final boolean isAnonymous() {
		return this.memberId == null && this.clauseId == ClauseId.NAME;
	}

	public final Name getName() {
		return this.name;
	}

	public final MemberId getMemberId() {
		if (this.memberId != null) {
			return this.memberId;
		}
		if (this.clauseId == ClauseId.NAME) {
			return null;
		}

		final StaticTypeRef adapterType =
				this.clauseId.adapterType(this, distribute());

		if (this.groupId == null) {
			return this.memberId = adapterId(adapterType);
		}

		return this.memberId = this.groupId.append(adapterId(adapterType));
	}

	public final ClauseDeclaration setName(Name name) {

		final ClauseDeclaration clone = clone();

		clone.clauseId = ClauseId.NAME;
		clone.name = name;
		if (this.groupId != null) {
			clone.memberId = this.groupId.append(clauseName(name));
		} else {
			clone.memberId = clauseName(name);
		}

		return clone;
	}

	public final ClauseId getClauseId() {
		if (this.clauseId != null) {
			return this.clauseId;
		}

		if (this.memberId != null) {

			final MemberName name = this.memberId.getMemberName();

			if (name != null) {
				return this.clauseId = ClauseId.NAME;
			}
		}

		return this.clauseId = byAdapterType(getAdapterType());
	}

	public final String getDisplayName() {
		return getClauseId().toString(this.memberId, this.name);
	}

	public final StaticTypeRef getAdapterType() {

		final MemberId memberId = getMemberId();

		if (memberId == null) {
			return null;
		}

		final AdapterId adapterId = memberId.getAdapterId();

		if (adapterId == null) {
			return null;
		}

		return adapterId.adapterType(getScope());
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
		if (this.memberId != null) {
			clone.memberId = groupId.append(this.memberId);
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
