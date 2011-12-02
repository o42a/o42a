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
package org.o42a.core.ref.impl.resolution;

import static org.o42a.core.value.Directive.SKIP_DIRECTIVE;

import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Directive;
import org.o42a.util.use.UserInfo;


public final class ErrorResolution extends Resolution {

	public ErrorResolution(Resolver resolver, ScopeInfo location) {
		super(resolver, location);
	}

	@Override
	public boolean isError() {
		return true;
	}

	@Override
	public final Obj toArtifact() {
		return getContext().getFalse();
	}

	@Override
	public final Directive toDirective() {
		return SKIP_DIRECTIVE;
	}

	@Override
	public final Obj materialize() {
		return toArtifact();
	}

	@Override
	public void resolveAll() {
	}

	@Override
	public void resolveValues(UserInfo user) {
	}

	@Override
	public String toString() {
		return "ERROR";
	}

}
