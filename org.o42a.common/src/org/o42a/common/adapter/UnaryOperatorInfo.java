/*
    Intrinsics
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
package org.o42a.common.adapter;

import static org.o42a.core.ref.path.PathBuilder.pathBuilder;

import java.util.HashMap;

import org.o42a.core.ref.path.PathBuilder;


public enum UnaryOperatorInfo {

	PLUS("+", pathBuilder("operators", "plus")),
	MINUS("-", pathBuilder("operators", "minus"));

	public static UnaryOperatorInfo bySign(String operator) {

		final UnaryOperatorInfo result = Registry.operators.get(operator);

		assert result != null :
			"Unsupported unary operator: " + operator;

		return result;
	}

	private final String sign;
	private final PathBuilder path;

	UnaryOperatorInfo(String sign, PathBuilder path) {
		this.sign = sign;
		this.path = path;
		Registry.operators.put(sign, this);
	}

	public final String getSign() {
		return this.sign;
	}

	public final PathBuilder getPath() {
		return this.path;
	}

	private static final class Registry {

		private static HashMap<String, UnaryOperatorInfo> operators =
			new HashMap<String, UnaryOperatorInfo>(2);

	}

}
