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
package org.o42a.core.artifact.object;

import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.User;


public final class ObjectType {

	private final Obj object;
	private ObjectResolution resolution = ObjectResolution.NOT_RESOLVED;
	private Ascendants ascendants;

	private ObjectType(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Ascendants getAscendants() {
		resolve(false);
		return this.ascendants;
	}

	public TypeRef getAncestor() {
		return getAscendants().getAncestor();
	}

	/**
	 * Object samples in descending precedence order.
	 *
	 * <p>This is an order reverse to their appearance in source code.</p>
	 *
	 * @return array of object samples.
	 */
	public final Sample[] getSamples() {
		return getAscendants().getSamples();
	}

	public final boolean derivedFrom(ObjectType other) {
		return derivedFrom(other, Derivation.ANY, Integer.MAX_VALUE);
	}

	public final boolean derivedFrom(ObjectType other, Derivation derivation) {
		return derivedFrom(other, derivation, Integer.MAX_VALUE);
	}

	public boolean derivedFrom(
			ObjectType other,
			Derivation derivation,
			int depth) {
		if (derivation.match(this, other)) {
			return true;
		}

		final int newDepth = depth - 1;

		if (newDepth < 0) {
			return false;
		}

		if (derivation.acceptAncestor()) {

			final TypeRef ancestor = getAncestor();

			if (ancestor != null) {

				final ObjectType ancestorType = ancestor.getType().objectType();

				if (ancestorType.derivedFrom(other, derivation, newDepth)) {
					return true;
				}
			}
		}

		if (derivation.acceptsSamples()) {
			for (Sample sample : getSamples()) {
				if (!derivation.acceptSample(sample)) {
					continue;
				}

				final ObjectType sampleType = sample.getType().objectType();

				if (sampleType.derivedFrom(other, derivation, newDepth)) {
					return true;
				}
			}
			for (Sample sample : getAscendants().getDiscardedSamples()) {
				if (!derivation.acceptSample(sample)) {
					continue;
				}

				final ObjectType sampleType = sample.getType().objectType();

				if (sampleType.derivedFrom(other, derivation, newDepth)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return this.object.toString();
	}

	final ObjectResolution getResolution() {
		return this.resolution;
	}

	final void setResolution(ObjectResolution resolution) {
		this.resolution = resolution;
	}

	final boolean resolve(boolean skipIfResolving) {
		if (this.resolution == ObjectResolution.NOT_RESOLVED) {
			try {
				this.resolution = ObjectResolution.RESOLVING_TYPE;
				this.ascendants = getObject().buildAscendants();
			} finally {
				this.resolution = ObjectResolution.NOT_RESOLVED;
			}
			this.resolution = ObjectResolution.TYPE_RESOLVED;
			this.ascendants.validate();
			getObject().postResolve();
			this.resolution = ObjectResolution.POST_RESOLVED;
		} else if (this.resolution == ObjectResolution.RESOLVING_TYPE) {
			if (!skipIfResolving) {
				getObject().getLogger().recursiveResolution(
						getObject(),
						getObject());
			}
			return false;
		}

		return this.resolution.resolved();
	}

	static final class UseableObjectType
			extends ObjectUsable<ObjectType> {

		private final ObjectType type;

		UseableObjectType(Obj object) {
			super(object);
			this.type = new ObjectType(object);
		}

		@Override
		protected ObjectType createUsed(User user) {
			return this.type;
		}

		final ObjectType getType() {
			return this.type;
		}

	}

}
