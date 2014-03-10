/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.value.flow;

import static org.o42a.core.value.flow.FlowIntrinsics.flowIntrinsics;

import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


public final class Flow {

	public static Flow FLOW = new Flow();

	public static final MemberKey inputKey(Intrinsics intrinsics) {
		return flowIntrinsics(intrinsics).inputKey();
	}

	public static final MemberKey outputKey(Intrinsics intrinsics) {
		return flowIntrinsics(intrinsics).outputKey();
	}

	public static TypeRef inputRef(TypeParameters<?> parameters) {

		final TypeParameters<Flow> flowParameters =
				ValueType.FLOW.cast(parameters);
		final MemberKey inputKey =
				inputKey(parameters.getContext().getIntrinsics());

		return flowParameters.typeRef(inputKey);
	}

	public static TypeRef outputRef(TypeParameters<?> parameters) {

		final TypeParameters<Flow> flowParameters =
				ValueType.FLOW.cast(parameters);
		final MemberKey inputKey =
				outputKey(parameters.getContext().getIntrinsics());

		return flowParameters.typeRef(inputKey);
	}

	private Flow() {
	}

	@Override
	public String toString() {
		return "FLOW";
	}

}
