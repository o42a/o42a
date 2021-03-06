/*
    Compiler Commons
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.common.phrase;

import static org.o42a.util.fn.Init.init;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.fn.Init;


final class ClauseInstanceConstructor extends ObjectConstructor {

	private final ClauseInstance instance;
	private final Init<AscendantsDefinition> ascendants = init(
			() -> instance()
			.getPhraseContext()
			.ascendants(this, distribute()));

	ClauseInstanceConstructor(
			ClauseInstance instance,
			Distributor distributor) {
		super(
				new Location(distributor.getContext(), instance.getLocation()),
				distributor);
		this.instance = instance;
	}

	private ClauseInstanceConstructor(
			ClauseInstance instance,
			Distributor distributor,
			AscendantsDefinition ascendants) {
		super(
				new Location(distributor.getContext(), instance.getLocation()),
				distributor);
		this.instance = instance;
		this.ascendants.set(ascendants);
	}

	@Override
	public boolean isEager() {
		return getAscendants().isEager();
	}

	public final ClauseInstance instance() {
		return this.instance;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return getAscendants().getAncestor();
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref, ref)
				.setParameters(toSynthetic().toRef().typeParameters());
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ClauseInstanceFieldDefinition(ref, this);
	}

	@Override
	public ClauseInstanceConstructor reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final AscendantsDefinition ascendants =
				getAscendants().reproduce(reproducer.getReproducer());

		if (ascendants == null) {
			return null;
		}

		return new ClauseInstanceConstructor(
				this.instance,
				reproducer.distribute(),
				ascendants);
	}

	@Override
	public String toString() {
		return this.instance.toString();
	}

	@Override
	protected Obj createObject() {
		return new ClauseInstanceObject(this);
	}

	final AscendantsDefinition getAscendants() {
		return this.ascendants.get();
	}

}
