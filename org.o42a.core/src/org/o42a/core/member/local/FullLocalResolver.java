/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.member.local;

import org.o42a.analysis.use.User;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.RefUsage;


public final class FullLocalResolver extends FullResolver {

	FullLocalResolver(LocalResolver resolver, User<?> user, RefUsage refUsage) {
		super(resolver, user, refUsage);
	}

	public final LocalResolver getLocalResolver() {
		return (LocalResolver) getResolver();
	}

	public final LocalScope getLocal() {
		return getLocalResolver().getLocal();
	}

	@Override
	public final FullLocalResolver setRefUsage(RefUsage refUsage) {
		return (FullLocalResolver) super.setRefUsage(refUsage);
	}

	@Override
	public String toString() {
		if (getResolver() == null) {
			return super.toString();
		}
		return "FullLocalResolver[" + getScope()
				+ " by " + toUser()
				+ " for " + getRefUsage()  + ']';
	}

}
