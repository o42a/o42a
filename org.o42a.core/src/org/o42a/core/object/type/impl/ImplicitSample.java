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


public final class ImplicitSample extends Sample {

	private final StaticTypeRef implicitAscendant;
	private final TypeRef overriddenAncestor;
	private TypeRef ancestor;

	public ImplicitSample(
			Ascendants ascendants,
			StaticTypeRef implicitAscendant,
			TypeRef overriddenAncestor) {
		super(implicitAscendant, ascendants);
		this.implicitAscendant = implicitAscendant;
		this.overriddenAncestor = overriddenAncestor;
		assertSameScope(implicitAscendant);
		assertSameScope(overriddenAncestor);
	}

	@Override
	public Obj getObject() {
		return this.implicitAscendant.getType();
	}

	@Override
	public TypeRef getAncestor() {
		if (this.ancestor != null) {
			return this.ancestor;
		}
		return this.ancestor = this.implicitAscendant.getAncestor();
	}

	@Override
	public TypeRef overriddenAncestor() {
		return this.overriddenAncestor;
	}

	@Override
	public StaticTypeRef getTypeRef() {
		return this.implicitAscendant;
	}

	@Override
	public Member getOverriddenMember() {
		return null;
	}

	@Override
	public StaticTypeRef getExplicitAscendant() {
		return null;
	}

	@Override
	public void reproduce(
			Reproducer reproducer,
			AscendantsBuilder<?> ascendants) {
		// Will be done automatically.
	}

	@Override
	public String toString() {
		return "ImplicitSample[" + this.implicitAscendant + ']';
	}

}
