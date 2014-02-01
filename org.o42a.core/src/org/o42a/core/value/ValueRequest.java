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
package org.o42a.core.value;

import org.o42a.core.source.CompilerLogger;


public final class ValueRequest {

	public static ValueRequest noValueRequest(CompilerLogger logger) {
		return new ValueRequest(null, logger, false, false);
	}

	private final TypeParameters<?> expectedParameters;
	private final CompilerLogger logger;
	private final boolean linkByValueAllowed;
	private final boolean linkToLinkAllowed;

	public ValueRequest(
			TypeParameters<?> expectedParameters,
			CompilerLogger logger) {
		assert expectedParameters != null :
			"Expected type parameters not specified";
		this.expectedParameters = expectedParameters;
		this.linkByValueAllowed = true;
		this.linkToLinkAllowed = true;
		this.logger = logger;
	}

	private ValueRequest(
			TypeParameters<?> expectedParameters,
			CompilerLogger logger,
			boolean linkByValueAllowed,
			boolean linkToLinkAllowed) {
		this.expectedParameters = expectedParameters;
		this.logger = logger;
		this.linkByValueAllowed = linkByValueAllowed;
		this.linkToLinkAllowed = linkToLinkAllowed;
	}

	public final boolean isValueExpected() {
		return getExpectedParameters() != null;
	}

	public final ValueType<?> getExpectedType() {
		return getExpectedParameters().getValueType();
	}

	public final TypeParameters<?> getExpectedParameters() {
		return this.expectedParameters;
	}

	public final boolean isLinkByValueAllowed() {
		return this.linkByValueAllowed;
	}

	public final boolean isLinkToLinkAllowed() {
		return this.linkToLinkAllowed;
	}

	public final CompilerLogger getLogger() {
		return this.logger;
	}

	public final ValueRequest noLinkByValue() {
		if (!isLinkByValueAllowed()) {
			return this;
		}
		return new ValueRequest(
				getExpectedParameters(),
				getLogger(),
				false,
				isLinkToLinkAllowed());
	}

	public final ValueRequest noLinkToLink() {
		if (!isLinkToLinkAllowed()) {
			return this;
		}
		return new ValueRequest(
				getExpectedParameters(),
				getLogger(),
				isLinkByValueAllowed(),
				false);
	}

	public final ValueRequest setLogger(CompilerLogger logger) {
		return new ValueRequest(
				getExpectedParameters(),
				logger,
				isLinkByValueAllowed(),
				isLinkToLinkAllowed());
	}

	@Override
	public String toString() {
		if (this.expectedParameters == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("ValueRequest[");
		out.append(this.expectedParameters);
		if (this.linkByValueAllowed) {
			out.append(", allow transform]");
		} else {
			out.append(", don't transform]");
		}

		return out.toString();
	}
}
