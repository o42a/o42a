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
package org.o42a.core.member.field.decl;

import org.o42a.core.member.Member;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.value.ObjectTypeParameters;


final class ObjectDefinerImpl implements ObjectDefiner {

	private final DeclaredField field;
	private final Ascendants implicitAscendants;
	private Ascendants ascendants;

	ObjectDefinerImpl(DeclaredField field, Ascendants implicitAscendants) {
		this.field = field;
		this.implicitAscendants = this.ascendants = implicitAscendants;
	}

	@Override
	public final DeclaredField getField() {
		return this.field;
	}

	@Override
	public final Ascendants getImplicitAscendants() {
		return this.implicitAscendants;
	}

	@Override
	public void makeStateful() {
		this.field.makeStateful();
	}

	public final Ascendants getAscendants() {
		return this.ascendants;
	}

	@Override
	public ObjectDefiner setAncestor(TypeRef ancestor) {
		this.ascendants = this.ascendants.setAncestor(ancestor);
		return this;
	}

	@Override
	public ObjectDefiner setParameters(ObjectTypeParameters typeParameters) {
		this.ascendants = this.ascendants.setParameters(typeParameters);
		return this;
	}

	@Override
	public ObjectDefiner addExplicitSample(StaticTypeRef explicitAscendant) {
		this.ascendants = this.ascendants.addExplicitSample(explicitAscendant);
		return this;
	}

	@Override
	public ObjectDefiner addImplicitSample(
			StaticTypeRef implicitAscendant,
			TypeRef overriddenAncestor) {
		this.ascendants = this.ascendants.addImplicitSample(
				implicitAscendant,
				overriddenAncestor);
		return this;
	}

	@Override
	public ObjectDefiner addMemberOverride(Member overriddenMember) {
		this.ascendants = this.ascendants.addMemberOverride(overriddenMember);
		return this;
	}

	@Override
	public void define(BlockBuilder definitions) {
		getField().addDefinitions(definitions);
	}

	@Override
	public String toString() {
		if (this.field == null) {
			return super.toString();
		}
		return "ObjectDefiner[" + this.field + ']';
	}

}
