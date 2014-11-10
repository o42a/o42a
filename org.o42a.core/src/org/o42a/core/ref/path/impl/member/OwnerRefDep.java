/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl.member;

import static org.o42a.core.ref.path.impl.member.MemberFragment.unresolvedTypeParameter;
import static org.o42a.core.value.macro.MacroConsumer.DEFAULT_CONSUMER;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.ObjectMeta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.value.macro.RefDep;


final class OwnerRefDep implements RefDep<ObjectMetaDep> {

	static Ref ownerRef(TypeParameterMacro macro) {

		final TypeParameterObject object = macro.getObject();
		final Scope scope = object.getScope();
		final Ref ownerRef =
				scope.getEnclosingScopePath()
				.bind(object, scope)
				.target(scope.distribute());

		final Ref ref = ownerRef.consume(DEFAULT_CONSUMER);
		final ObjectMetaDep dep =
				new OwnerRefDep(object.getParameterKey()).buildDep(ref, null);

		if (dep != null) {
			dep.register();
		}

		return ref;
	}

	private MemberKey parameterKey;

	private OwnerRefDep(MemberKey parameterKey) {
		this.parameterKey = parameterKey;
	}

	@Override
	public ObjectMetaDep newDep(ObjectMeta meta, Ref ref, PathTemplate template) {
		return new ObjectMetaDep(meta, ref);
	}

	@Override
	public void setParentDep(ObjectMetaDep dep, MetaDep parentDep) {
		dep.setParentDep(parentDep);
	}

	@Override
	public void invalidRef(Ref ref) {
		unresolvedTypeParameter(
				ref.getLogger(),
				ref.getLocation(),
				this.parameterKey);
	}

}
