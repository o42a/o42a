/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;


public class MemberById extends Wrap {

	private final StaticTypeRef declaredIn;
	private final MemberId memberId;

	public MemberById(
			LocationSpec location,
			Distributor distributor,
			MemberId memberId,
			StaticTypeRef declaredIn) {
		super(location, distributor);
		this.memberId = memberId;
		this.declaredIn = declaredIn;
	}

	@Override
	public String toString() {

		final Ref wrapped = getWrapped();

		if (wrapped != null) {
			return wrapped.toString();
		}
		if (this.memberId == null) {
			return super.toString();
		}

		return this.memberId.toString();
	}

	@Override
	protected Ref resolveWrapped() {

		final Distributor distributor = distribute();
		final Container enclosing = getContainer();
		final Obj declaredIn =
			this.declaredIn != null ? this.declaredIn.getType() : null;
		final Path result = enclosing.findPath(this, this.memberId, declaredIn);

		if (result == null) {
			getLogger().unresolved(this, this.memberId);
			return errorRef(this);
		}

		return result.target(this, distributor);
	}

}
