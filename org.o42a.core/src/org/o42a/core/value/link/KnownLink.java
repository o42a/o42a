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
package org.o42a.core.value.link;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;


public abstract class KnownLink extends Link {

	private final KnownLinkData data;

	public KnownLink(
			LocationInfo location,
			Distributor distributor,
			TargetRef targetRef) {
		super(location, distributor);
		this.data = new KnownLinkData(this, targetRef);
	}

	protected KnownLink(KnownLink prototype, TargetRef targetRef) {
		this(
				targetRef.getScope().getLocation().setDeclaration(prototype),
				prototype.distributeIn(targetRef.getScope().getContainer()),
				targetRef);
	}

	@Override
	public final boolean isSynthetic() {
		return false;
	}

	@Override
	public final TypeRef getInterfaceRef() {
		return this.data.getTypeRef();
	}

	public final TargetRef getTargetRef() {
		return this.data.getTargetRef();
	}

	public final ValueKnowledge getKnowledge() {
		return this.data.getKnowledge();
	}

	public final Value<KnownLink> toValue() {
		return getLinkParameters().compilerValue(this);
	}

	@Override
	public void resolveAll(FullResolver resolver) {
		this.data.resolveAll(resolver);
	}

	@Override
	public String toString() {
		if (this.data == null) {
			return super.toString();
		}
		return this.data.getTargetRef().toString();
	}

	protected abstract KnownLink prefixWith(PrefixPath prefix);

	@Override
	protected final Obj createTarget() {
		return this.data.createTarget();
	}

	private static final class KnownLinkData extends LinkData<KnownLink> {

		KnownLinkData(KnownLink link, TargetRef targetRef) {
			super(link, targetRef);
		}

		@Override
		protected TargetRef buildTargetRef() {
			throw new UnsupportedOperationException();
		}

	}

}
