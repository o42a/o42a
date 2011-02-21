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

import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.clause.ClauseId.byAdapterType;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Placed;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.type.StaticTypeRef;


public class ClauseDeclaration extends Placed implements Cloneable {

	public static ClauseDeclaration clauseDeclaration(
			LocationSpec location,
			Distributor distributor,
			String name,
			ClauseId clauseId) {
		return new ClauseDeclaration(
				location,
				distributor,
				name,
				null,
				clauseId);
	}

	public static ClauseDeclaration clauseDeclaration(
			LocationSpec location,
			Distributor distributor,
			String name,
			MemberId memberId) {
		return new ClauseDeclaration(
				location,
				distributor,
				name,
				memberId,
				null);
	}

	public static ClauseDeclaration anonymousClauseDeclaration(
			LocationSpec location,
			Distributor distributor) {
		return new ClauseDeclaration(location, distributor);
	}

	private String name;
	private MemberId memberId;
	private MemberId groupId;
	private ClauseId clauseId;
	private ClauseKind kind;
	private boolean terminator;
	private boolean implicit;

	private ClauseDeclaration(
			LocationSpec location,
			Distributor distributor,
			String name,
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
			LocationSpec location,
			Distributor distributor,
			ClauseDeclaration prototype) {
		super(location, distributor);
		this.name = prototype.name;
		this.memberId = prototype.memberId;
		this.clauseId = prototype.clauseId;
		this.kind = prototype.kind;
		this.terminator = prototype.terminator;
	}

	private ClauseDeclaration(
			LocationSpec location,
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

	public final String getName() {
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

	public final ClauseDeclaration setName(String name) {

		final ClauseDeclaration clone = clone();

		clone.clauseId = ClauseId.NAME;
		clone.name = name;
		if (this.groupId != null) {
			clone.memberId = this.groupId.append(memberName(name));
		} else {
			clone.memberId = memberName(name);
		}

		return clone;
	}

	public final ClauseId getClauseId() {
		if (this.clauseId != null) {
			return this.clauseId;
		}
		return this.clauseId = byAdapterType(getAdapterType());
	}

	public final String getDisplayName() {

		final StringBuilder out = new StringBuilder();

		switch (getClauseId()) {
		case NAME:
			if (this.memberId == null) {
				out.append("<anonymous>");
				break;
			}
			out.append(this.memberId);
			break;
		case ARGUMENT:
			if (this.name == null) {
				out.append("[]");
				break;
			}
			out.append('[').append(this.name).append(']');
			break;
		case IMPERATIVE:
			if (this.name == null) {
				out.append("{}");
				break;
			}
			out.append('{').append(this.name).append('}');
			break;
		case STRING:
			if (this.name == null) {
				out.append("''");
				break;
			}
			out.append('\'').append(this.name).append('\'');
			break;
		}

		return out.toString();
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
