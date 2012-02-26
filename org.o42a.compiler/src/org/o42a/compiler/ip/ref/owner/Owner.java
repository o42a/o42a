/*
    Compiler
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
package org.o42a.compiler.ip.ref.owner;


import org.o42a.compiler.ip.ref.MemberOf;
import org.o42a.core.Distributor;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public abstract class Owner {

	public static Owner defaultOwner(Ref owner) {
		return new DefaultOwner(owner);
	}

	public static Owner dontDerefOwner(Ref owner) {
		return new DontDerefOwner(owner);
	}

	public static Owner neverDerefOwner(Ref owner) {
		return new NeverDerefOwner(owner);
	}

	static void redundantBodyRef(CompilerLogger logger, LogInfo location) {
		logger.error(
				"redundant_body_ref",
				location,
				"Redundant link body reference");
	}

	protected final Ref owner;

	public Owner(Ref owner) {
		this.owner = owner;
	}

	public abstract Ref ref();

	public abstract Owner body(LocationInfo location);

	public Owner member(
			LocationInfo location,
			MemberId memberId,
			StaticTypeRef declaredIn) {

		final Ref owner = ref();
		final Distributor distributor = this.owner.distribute();
		final MemberOf memberOf = new MemberOf(
				location,
				distributor,
				memberId,
				declaredIn);
		final BoundPath path = owner.getPath().append(memberOf);
		final Ref ref = path.setLocation(location).target(distributor);

		return memberOwner(ref);
	}

	@Override
	public String toString() {
		return String.valueOf(this.owner);
	}

	protected Owner memberOwner(final Ref ref) {
		return new DefaultOwner(ref);
	}

}
