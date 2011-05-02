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
package org.o42a.core.artifact.link;

import static org.o42a.core.def.Rescoper.wrapper;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.member.Member;


public abstract class ObjectWrap extends PlainObject {

	public ObjectWrap(Scope scope) {
		super(scope);
	}

	public ObjectWrap(LocationInfo location, Distributor enclosing) {
		super(location, enclosing);
	}

	protected ObjectWrap(Scope scope, Obj sample) {
		super(scope, sample);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {

		final Obj wrapped = getWrapped();
		final ObjectType type = type().useBy(dummyUser());

		for (Member inherited
				: type.getAncestor().typeObject(type()).getMembers()) {

			// find the member from ancestor in wrapped object
			final Member member = wrapped.member(inherited.getKey());

			assert member != null:
				"Member " + inherited + " is not known to " + wrapped;

			if (inherited.getDefinedIn().derivedFrom(member.getDefinedIn())) {
				// member is not overridden in wrapped object -
				// leave the default definition
				continue;
			}

			members.addMember(member.wrap(type(), inherited, this));
		}
	}

	@Override
	protected Definitions explicitDefinitions() {
		return wrapper(getScope(), getWrapped().getScope())
		.update(getWrapped().getDefinitions());
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();
		getWrapped().resolveAll();
	}

	@Override
	protected ObjectIR createIR(Generator generator) {
		return getWrapped().ir(generator);
	}

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return getWrapped().valueIR(objectIR.getGenerator());
	}

}
