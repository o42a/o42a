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

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogInfo;


public class MemberFragment extends PathFragment {

	static void unresolvedTypeParameter(
			CompilerLogger logger,
			LogInfo location,
			MemberKey parameterKey) {
		logger.error(
				"unresolved_type_parameter",
				location,
				"Unknown type parameter: %s",
				parameterKey);
	}

	private final MemberKey memberKey;

	public MemberFragment(MemberKey memberKey) {
		this.memberKey = memberKey;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Member member = start.getContainer().member(this.memberKey);

		if (!member.isTypeParameter()) {
			return new MemberStep(this.memberKey).toPath();
		}

		final Obj object = start.toObject();

		assert object != null :
			"Not object: " + start;

		final TypeRef typeRef =
				object.type().getParameters().typeRef(this.memberKey);

		if (typeRef == null) {
			unresolvedTypeParameter(
					expander.getLogger(),
					expander.getPath().getLocation(),
					this.memberKey);
			return null;
		}

		final TypeParameterConstructor typeParameter =
				new TypeParameterConstructor(
						expander.getPath(),
						start.distribute(),
						this.memberKey);

		return typeParameter.toPath();
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return defaultInterface(ref);
	}

	@Override
	public String toString() {
		if (this.memberKey == null) {
			return super.toString();
		}
		return this.memberKey.toString();
	}

}
