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
package org.o42a.core.member;

import java.util.ArrayList;
import java.util.Iterator;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.type.TypeRef;


public abstract class Member extends Placed {

	private static final Member[] NOTHING_OVERRIDDEN = new Member[0];

	private Member lastDefinition;
	private Member[] overridden;

	public Member(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public abstract MemberId getId();

	public abstract MemberKey getKey();

	public final String getDisplayName() {
		return getId().toString();
	}

	public abstract Field<?> toField();

	public abstract LocalScope toLocal();

	public abstract Clause toClause();

	public abstract Container getSubstance();

	public abstract Visibility getVisibility();

	public abstract boolean isOverride();

	public final boolean isPropagated() {
		return getPropagatedFrom() != null;
	}

	public abstract boolean isAbstract();

	/**
	 * The scope this members's definition were assigned in.
	 *
	 * @return the {@link #getLastDefinition() last definition} scope.
	 */
	public final Scope getDefinedIn() {
		return getLastDefinition().getScope();
	}

	public abstract Member getPropagatedFrom();

	/**
	 * The last definition of this member.
	 *
	 * @return the last member's explicit definition or implicit definition
	 * with multiple inheritance.
	 */
	public Member getLastDefinition() {
		if (this.lastDefinition != null) {
			return this.lastDefinition;
		}
		if (!isPropagated()) {
			return this.lastDefinition = this;
		}

		final Member[] overridden = getOverridden();

		if (overridden.length != 1) {
			return this.lastDefinition = this;
		}

		return this.lastDefinition = overridden[0].getLastDefinition();
	}

	public Member[] getOverridden() {
		if (this.overridden != null) {
			return this.overridden;
		}
		return this.overridden = overriddenMembers();
	}

	public final boolean definedAfter(Member other) {
		return getScope().derivedFrom(other.getDefinedIn());
	}

	public abstract Member propagateTo(Scope scope);

	public abstract void resolveAll();

	public abstract Member wrap(Member inherited, Container container);

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Scope enclosingScope = getScope();

		if (enclosingScope != getContext().getRoot().getScope()) {
			out.append(enclosingScope).append(':');
		} else {
			out.append("$$");
		}
		out.append(getDisplayName());

		return out.toString();
	}

	protected abstract void merge(Member member);

	private Member[] overriddenMembers() {
		if (!isOverride()) {
			return NOTHING_OVERRIDDEN;
		}

		final Obj container = getContainer().toObject();
		final Sample[] containerSamples = container.getSamples();
		final ArrayList<Member> overridden;
		final TypeRef containerAncestor = container.getAncestor();

		if (containerAncestor != null) {

			final Member ancestorMember =
				containerAncestor.getType().member(getKey());

			if (ancestorMember != null) {
				overridden = new ArrayList<Member>(containerSamples.length + 1);
				overridden.add(ancestorMember);
			} else {
				overridden = new ArrayList<Member>(containerSamples.length);
			}
		} else {
			overridden = new ArrayList<Member>(containerSamples.length);
		}

		for (Sample containerSample : containerSamples) {

			final Member sampleMember =
				containerSample.getType().member(getKey());

			if (sampleMember == null) {
				continue;
			}
			addMember(overridden, sampleMember);
		}

		return overridden.toArray(new Member[overridden.size()]);
	}

	private void addMember(ArrayList<Member> members, Member member) {

		final Scope definedIn = member.getDefinedIn();
		final Iterator<Member> i = members.iterator();

		while (i.hasNext()) {

			final Member m = i.next();

			if (m.getDefinedIn().derivedFrom(definedIn)) {
				return;
			}
			if (definedIn.derivedFrom(m.getDefinedIn())) {
				i.remove();
				while (i.hasNext()) {

					final Member f2 = i.next();

					if (definedIn.derivedFrom(f2.getDefinedIn())) {
						i.remove();
					}
				}

				break;
			}
		}

		members.add(member);
	}

}
