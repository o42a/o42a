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

import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.source.Intrinsics;


final class FlowIntrinsics {

	private static final MemberName INPUT_ID =
			fieldName(CASE_INSENSITIVE.canonicalName("input"));
	private static final MemberName OUTPUT_ID =
			fieldName(CASE_INSENSITIVE.canonicalName("output"));

	private static FlowIntrinsics instance;

	static FlowIntrinsics flowIntrinsics(Intrinsics intrinsics) {

		final FlowIntrinsics cached = instance;

		if (cached != null && cached.intrinsics == intrinsics) {
			return cached;
		}

		return instance = new FlowIntrinsics(intrinsics);
	}

	private final Intrinsics intrinsics;
	private MemberKey inputKey;
	private MemberKey outputKey;

	private FlowIntrinsics(Intrinsics intrinsics) {
		this.intrinsics = intrinsics;
	}

	public final MemberKey inputKey() {
		if (this.inputKey != null) {
			return this.inputKey;
		}
		return this.inputKey =
				INPUT_ID.key(this.intrinsics.getFlow().getScope());
	}

	public final MemberKey outputKey() {
		if (this.outputKey != null) {
			return this.outputKey;
		}
		return this.outputKey =
				OUTPUT_ID.key(this.intrinsics.getFlow().getScope());
	}

}
