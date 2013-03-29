/*
    Compiler
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
package org.o42a.compiler.ip.ref.owner;

import static org.o42a.compiler.ip.ref.AccessDistributor.accessDistributor;

import org.o42a.compiler.ip.ref.MemberOf;
import org.o42a.core.Distributor;
import org.o42a.core.member.AccessSource;
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

	private final AccessSource accessSource;
	private final Ref ownerRef;

	Owner(AccessSource accessSource, Ref ownerRef) {
		assert accessSource != null :
			"Access source not specified";
		assert ownerRef != null :
			"Owner reference not specified";
		this.accessSource = accessSource;
		this.ownerRef = ownerRef;
	}

	public final AccessSource accessSource() {
		return this.accessSource;
	}

	public final Ref ownerRef() {
		return this.ownerRef;
	}

	public boolean isMacroExpanding() {
		return false;
	}

	public abstract boolean isBodyReferred();

	public abstract Ref targetRef();

	public abstract Owner body(LocationInfo location, LocationInfo bodyRef);

	public abstract Owner deref(LocationInfo location, LocationInfo deref);

	public abstract Ref bodyRef();

	public final Owner member(
			LocationInfo location,
			MemberId memberId,
			StaticTypeRef declaredIn) {

		final Ref owner = targetRef();
		final Distributor distributor = distribute();
		final MemberOf memberOf = new MemberOf(
				location,
				accessDistributor(distributor, accessSource()),
				memberId,
				declaredIn);
		final BoundPath path = owner.getPath().append(memberOf);
		final Ref ref = path.setLocation(location).target(distributor);

		return memberOwner(ref);
	}

	public final Owner expandMacro(LogInfo expansion) {
		return new MacroExpandingOwner(this, expansion);
	}

	public final Owner plainOwner() {
		return new NonLinkOwner(accessSource(), ownerRef());
	}

	public final CompilerLogger getLogger() {
		return ownerRef().getLogger();
	}

	public final Distributor distribute() {
		return ownerRef().distribute();
	}

	@Override
	public String toString() {
		if (this.ownerRef == null) {
			return super.toString();
		}
		return this.ownerRef.toString();
	}

	protected Owner memberOwner(Ref ref) {
		return new DefaultOwner(accessSource(), ref);
	}

}
