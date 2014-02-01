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
package org.o42a.core.object.type.impl;


import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.AscendantsBuilder;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;


public final class ExplicitSample extends Sample {

	private final StaticTypeRef explicitAscendant;

	public ExplicitSample(
			StaticTypeRef explicitAscendant,
			Ascendants ascendants) {
		super(explicitAscendant, ascendants);
		this.explicitAscendant = explicitAscendant;
		assertSameScope(explicitAscendant);
	}

	@Override
	public Obj getObject() {
		return this.explicitAscendant.getType();
	}

	@Override
	public TypeRef getAncestor() {
		return this.explicitAscendant.getAncestor();
	}

	@Override
	public TypeRef overriddenAncestor() {
		return this.explicitAscendant.getAncestor();
	}

	@Override
	public StaticTypeRef getTypeRef() {
		return this.explicitAscendant;
	}

	@Override
	public boolean isExplicit() {
		return true;
	}

	@Override
	public Member getOverriddenMember() {
		return null;
	}

	@Override
	public StaticTypeRef getExplicitAscendant() {
		return this.explicitAscendant;
	}

	@Override
	public void reproduce(
			Reproducer reproducer,
			AscendantsBuilder<?> ascendants) {

		final StaticTypeRef explicitAscendant =
				this.explicitAscendant.reproduce(reproducer);

		if (explicitAscendant != null) {
			ascendants.addExplicitSample(explicitAscendant);
		}
	}

	@Override
	public String toString() {
		return "ExplicitSample[" + this.explicitAscendant + ']';
	}

}
