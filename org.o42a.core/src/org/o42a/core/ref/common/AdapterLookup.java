/*
    Compiler Core
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
package org.o42a.core.ref.common;

import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.ref.Ref;


public abstract class AdapterLookup extends Wrap {

	private final Ref adaptable;
	private final StaticTypeRef adapterType;
	private final StaticTypeRef secondAdapterType;
	private boolean second;

	public AdapterLookup(
			LocationSpec location,
			Ref adaptable,
			StaticTypeRef adapterType) {
		this(location, adaptable, adapterType, null);
	}

	public AdapterLookup(
			LocationSpec location,
			Ref adaptable,
			StaticTypeRef adapterType,
			StaticTypeRef secondAdapterType) {
		super(location, adaptable.distribute());
		this.adaptable = adaptable;
		this.adapterType = adapterType;
		this.secondAdapterType = secondAdapterType;
	}

	public Ref getAdaptable() {
		return this.adaptable;
	}

	public StaticTypeRef getAdapterType() {
		return this.adapterType;
	}

	public StaticTypeRef getSecondAdapterType() {
		return this.secondAdapterType;
	}

	public boolean isSecond() {
		return this.second;
	}

	@Override
	public String toString() {
		return this.adaptable + "@@(" + this.adapterType + ')';
	}

	@Override
	protected Ref resolveWrapped() {

		final Ref adapter;

		if (this.secondAdapterType != null) {

			final Ref firstAdapter =
				this.adaptable.findAdapter(this, this.adapterType);

			if (firstAdapter.getResolution().isFalse()) {
				adapter = this.adaptable.adapt(this, this.secondAdapterType);
				this.second = true;
			} else {
				adapter = firstAdapter;
			}
		} else {
			adapter = this.adaptable.adapt(this, this.adapterType);
		}
		if (adapter == null) {
			noAdapter(this.adaptable);
			return null;
		}

		return adapter;
	}

	protected abstract void noAdapter(Ref owner);

}
