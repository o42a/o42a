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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.LocationInfo;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;


public class MemberRef extends Wrap {

	private final Ref owner;
	private final MemberId memberId;
	private final StaticTypeRef declaredIn;

	public MemberRef(
			LocationInfo location,
			Ref owner,
			MemberId memberId,
			StaticTypeRef declaredIn) {
		super(location, owner.distribute());
		this.owner = owner;
		this.memberId = memberId;
		this.declaredIn = declaredIn;
	}

	@Override
	public String toString() {
		if (getWrapped() != null) {
			return getWrapped().toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.owner);

		final int len = out.length();

		if (len != 0) {

			final char last = out.charAt(len - 1);

			switch (last) {
			case ':':
			case '$':
				break;
			case '*':
				if (len != 1) {
					out.append(':');
				}
				break;
			default:
				out.append(':');
			}
		}

		out.append(this.memberId);
		if (this.declaredIn != null) {
			out.append("@<").append(this.declaredIn).append('>');
		}

		return out.toString();
	}

	@Override
	protected Ref resolveWrapped() {

		final Resolution ownerResolution = this.owner.getResolution();

		if (ownerResolution.isError()) {
			return errorRef(ownerResolution);
		}

		final Path memberPath = ownerResolution.member(
				this,
				this.memberId,
				this.declaredIn != null
				? this.declaredIn.typeObject(dummyUser()) : null);

		if (memberPath == null) {
			return errorRef(this);
		}

		return memberPath.target(this, distribute(), this.owner);
	}

}
