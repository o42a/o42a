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
package org.o42a.core.object.link;

import static org.o42a.core.source.CompilerLogger.logDeclaration;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;


public abstract class KnownLink extends ObjectLink {

	private final KnownLinkData data;

	public KnownLink(LocationInfo location, Distributor distributor) {
		super(location, distributor);
		this.data = new KnownLinkData(this);
	}

	public KnownLink(
			LocationInfo location,
			Distributor distributor,
			TargetRef targetRef) {
		super(location, distributor);
		this.data = new KnownLinkData(this, targetRef);
	}

	protected KnownLink(ObjectLink prototype, TargetRef targetRef) {
		this(
				new Location(
						targetRef.getContext(),
						targetRef.getScope().getLoggable().setReason(
								logDeclaration(prototype))),
				prototype.distributeIn(targetRef.getScope().getContainer()),
				targetRef);
	}

	@Override
	public final boolean isSynthetic() {
		return false;
	}

	@Override
	public final TypeRef getTypeRef() {
		return this.data.getTypeRef();
	}

	public final TargetRef getTargetRef() {
		return this.data.getTargetRef();
	}

	public final ValueKnowledge getKnowledge() {
		return this.data.getKnowledge();
	}

	public final Value<KnownLink> toValue() {
		return getValueStruct().compilerValue(this);
	}

	@Override
	public void resolveAll(Resolver resolver) {
		this.data.resolveAll(resolver);
	}

	protected abstract KnownLink prefixWith(PrefixPath prefix);

	protected abstract TargetRef buildTargetRef();

	@Override
	protected final Obj createTarget() {
		return this.data.createTarget();
	}

	private static final class KnownLinkData extends LinkData<KnownLink> {

		KnownLinkData(KnownLink link) {
			super(link);
		}

		KnownLinkData(KnownLink link, TargetRef targetRef) {
			super(link, targetRef);
		}

		@Override
		protected TargetRef buildTargetRef() {
			return getLink().buildTargetRef();
		}

	}

}
