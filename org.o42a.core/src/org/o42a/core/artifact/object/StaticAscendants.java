/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.object;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


public class StaticAscendants extends Ascendants {

	public StaticAscendants(Ascendants ascendants) {
		super(ascendants);
	}

	public StaticAscendants(Obj object) {
		super(object);
	}

	public StaticAscendants(Scope scope) {
		super(scope);
	}

	@Override
	public StaticAscendants setAncestor(TypeRef explicitAncestor) {
		return (StaticAscendants) super.setAncestor(
				explicitAncestor.toStatic());
	}

	@Override
	public StaticAscendants addExplicitSample(StaticTypeRef explicitAscendant) {
		return (StaticAscendants) super.addExplicitSample(
				explicitAscendant.toStatic());
	}

	@Override
	public StaticAscendants addImplicitSample(StaticTypeRef implicitAscendant) {
		return (StaticAscendants) super.addImplicitSample(
				implicitAscendant.toStatic());
	}

	@Override
	public StaticAscendants addMemberOverride(Member overriddenMember) {
		return (StaticAscendants) super.addMemberOverride(overriddenMember);
	}

}
