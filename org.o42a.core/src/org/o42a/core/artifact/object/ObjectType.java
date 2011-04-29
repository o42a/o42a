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

import static java.util.Collections.unmodifiableMap;
import static org.o42a.core.artifact.object.ObjectResolution.NOT_RESOLVED;

import java.util.*;

import org.o42a.core.Scope;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.User;


public final class ObjectType {

	private final Obj object;
	private ObjectResolution resolution = NOT_RESOLVED;
	private Ascendants ascendants;
	private Map<Scope, ? extends Set<Derivation>> allAscendants;

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

	public boolean inherits(ObjectType other) {
		if (getObject() == other.getObject()) {
			return true;
		}

		final TypeRef ancestor = getAncestor();

		if (ancestor == null) {
			return false;
		}

		return ancestor.type(getObject()).inherits(other);
	}

	public final Map<Scope, ? extends Set<Derivation>> allAscendants() {
		if (this.allAscendants != null) {
			return this.allAscendants;
		}
		return this.allAscendants = unmodifiableMap(buildAllAscendants());
	}

	public final boolean derivedFrom(ObjectType other) {
		return allAscendants().containsKey(other.getObject().getScope());
	}

	public final boolean derivedFrom(ObjectType other, Derivation derivation) {

		final Set<Derivation> derivations =
			allAscendants().get(other.getObject().getScope());

		return derivations != null && derivations.contains(derivation);
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

	private HashMap<Scope, EnumSet<Derivation>> buildAllAscendants() {

		final HashMap<Scope, EnumSet<Derivation>> allAscendants =
			new HashMap<Scope, EnumSet<Derivation>>();

		allAscendants.put(
				getObject().getScope(),
				EnumSet.of(Derivation.SAME));

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {

			final ObjectType type = ancestor.type(getObject());

			for (Scope scope : type.allAscendants().keySet()) {
				allAscendants.put(
						scope,
						EnumSet.of(Derivation.INHERITANCE));
			}
		}

		addSamplesAscendants(allAscendants, getSamples());
		addSamplesAscendants(
				allAscendants,
				getAscendants().getDiscardedSamples());

		return allAscendants;
	}

	private void addSamplesAscendants(
			HashMap<Scope, EnumSet<Derivation>> allAscendants,
			Sample[] samples) {
		for (Sample sample : samples) {

			final ObjectType type = sample.type(getObject());

			for (Map.Entry<Scope, ? extends Set<Derivation>> e
					: type.allAscendants().entrySet()) {

				final Scope scope = e.getKey();

				for (Derivation derivation : e.getValue()) {

					final Derivation traversed =
						derivation.traverseSample(sample);
					EnumSet<Derivation> derivations =
						allAscendants.get(scope);

					if (derivations != null) {
						derivations.add(traversed);
					} else {
						derivations = EnumSet.of(traversed);
						allAscendants.put(scope, derivations);
					}
					for (Derivation implied : traversed.implied()) {
						derivations.add(implied);
					}
				}
			}
		}
	}

	static final class UsableObjectType extends ObjectUsable<ObjectType> {

		private final ObjectType type;

		UsableObjectType(Obj object) {
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
