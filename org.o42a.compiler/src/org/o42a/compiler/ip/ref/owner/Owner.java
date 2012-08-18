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

	static void redundantBodyRef(CompilerLogger logger, LogInfo location) {
		logger.error(
				"redundant_body_ref",
				location,
				"Redundant link body reference");
	}

	protected final Ref ownerRef;

	Owner(Ref ownerRef) {
		assert ownerRef != null :
			"Owner did not specified";
		this.ownerRef = ownerRef;
	}

	public boolean isMacroExpanding() {
		return false;
	}

	public abstract Ref targetRef();

	public abstract Owner body(LocationInfo location, LocationInfo bodyRef);

	public abstract Owner deref(LocationInfo location, LocationInfo deref);

	public abstract Ref bodyRef();

	public final Owner member(
			LocationInfo location,
			MemberId memberId,
			StaticTypeRef declaredIn) {

		final Ref owner = targetRef();
		final Distributor distributor = this.ownerRef.distribute();
		final MemberOf memberOf = new MemberOf(
				location,
				distributor,
				memberId,
				declaredIn);
		final BoundPath path = owner.getPath().append(memberOf);
		final Ref ref = path.setLocation(location).target(distributor);

		return memberOwner(ref);
	}

	public final Owner expandMacro(LogInfo expansion) {
		return new MacroExpandingOwner(this, expansion);
	}

	public final Owner expandMemberMacro(LogInfo expansion) {
		return new MacroExpandingOwner(this, expansion);
	}

	@Override
	public String toString() {
		return String.valueOf(this.ownerRef);
	}

	protected Owner memberOwner(Ref ref) {
		return new DefaultOwner(ref);
	}

}
