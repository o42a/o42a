/*
    Intrinsics
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
package org.o42a.intrinsic.numeric;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.common.adapter.FloatByString;
import org.o42a.common.intrinsic.IntrinsicType;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.value.ValueType;


public class FloatObject extends IntrinsicType {

	private static FieldDeclaration declaration(
			Container enclosingContainer) {

		final CompilerContext context;

		try {
			context = enclosingContainer.getContext().contextFor(
					"floats/float.o42a");
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}

		final Location location = new Location(context, context.getSource());
		final Distributor distributor =
			declarativeDistributor(enclosingContainer);

		return fieldDeclaration(
				location,
				distributor,
				memberName("float")).prototype();
	}

	public FloatObject(Container enclosingContainer) {
		super(declaration(enclosingContainer), ValueType.FLOAT);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				absolutePath(getContext(), "number")
				.target(
						this,
						distributeIn(getScope().getEnclosingContainer()))
				.toTypeRef());
	}

	@Override
	protected void postResolve() {
		super.postResolve();
		includeSource();
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		super.declareMembers(members);

		final FloatByString byString = new FloatByString(this);

		members.addMember(byString.toMember());
	}

}
