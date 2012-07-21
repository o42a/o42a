/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;


public abstract class ParentMetaDep extends MetaDep {

	private final MetaDep nested;
	private final Nesting nesting;

	public ParentMetaDep(MetaDep nested) {
		super(nested.getDeclaredIn().getParentMeta(), nested.getKey());
		this.nested = nested;
		this.nesting = nesting();
	}

	@Override
	public final MetaDep nestedDep() {
		return this.nested;
	}

	@Override
	public Meta nestedMeta(Meta meta) {
		meta.getObject().assertDerivedFrom(getDeclaredIn().getObject());
		return this.nesting.findObjectIn(meta.getObject().getScope()).meta();
	}

	@Override
	protected boolean triggered(Meta meta) {
		return nestedDep().triggered(nestedMeta(meta));
	}

	@Override
	protected boolean updateMeta(Meta meta) {
		return nestedDep().updateMeta(nestedMeta(meta));
	}

	private final Nesting nesting() {

		final Meta nestedMeta = nestedDep().getDeclaredIn();
		final Scope enclosingScope =
				nestedMeta.getObject()
				.getScope()
				.getEnclosingScope();
		final Obj enclosingObject = enclosingScope.toObject();

		if (enclosingObject != null) {
			assert enclosingObject.meta().is(getDeclaredIn()) :
				"Wrong enclosing object: " + enclosingObject
				+ ", but expected " + getDeclaredIn().getObject();
			return nestedMeta.getNesting();
		}

		return new MemberObjectNesting(
				enclosingScope.toMember().getMemberKey(),
				nestedMeta.getNesting());
	}

	private static final class MemberObjectNesting implements Nesting {

		private final MemberKey memberKey;
		private final Nesting nesting;

		MemberObjectNesting(MemberKey memberKey, Nesting nesting) {
			this.memberKey = memberKey;
			this.nesting = nesting;
		}

		@Override
		public Obj findObjectIn(Scope enclosing) {
			return this.nesting.findObjectIn(
					enclosing.getContainer()
					.member(this.memberKey)
					.substance(dummyUser())
					.getScope());
		}

		@Override
		public String toString() {
			if (this.nesting == null) {
				return super.toString();
			}
			return "Nesting[" + this.memberKey + '/' + this.nesting + ']';
		}

	}

}
