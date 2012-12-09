/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.imperative;

import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.DefinerEnv;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueRequest;


final class ImperativeDefinerCommandEnv extends CommandEnv {

	private final ImperativeDefiner definer;
	private final DefinerEnv initialEnv;
	private ValueRequest valueRequest;

	ImperativeDefinerCommandEnv(ImperativeDefiner definer, DefinerEnv env) {
		super(null);
		this.definer = definer;
		this.initialEnv = env;
	}

	@Override
	public ValueRequest getValueRequest() {
		if (this.valueRequest != null) {
			return this.valueRequest;
		}
		return this.valueRequest = buildValueRequest();
	}

	private ValueRequest buildValueRequest() {

		final ValueRequest initialRequest = this.initialEnv.getValueRequest();
		final LocalScope localScope = this.definer.getBlock().getScope();
		final TypeParameters<?> initialParameters =
				initialRequest.getExpectedParameters();

		if (initialParameters == null) {
			return initialRequest;
		}

		final TypeParameters<?> typeParameters =
				initialParameters.prefixWith(
						localScope.pathTo(this.definer.getScope())
						.bind(this.definer)
						.toPrefix(localScope));
		final ValueRequest valueRequest = new ValueRequest(
				typeParameters,
				initialRequest.getLogger());

		if (valueRequest.isTransformAllowed()) {
			return valueRequest;
		}

		return valueRequest.dontTransofm();
	}

}
