/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.object.Derivation;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.def.SourceSpec;


abstract class DefCollector<D extends SourceSpec> {

	private final Obj object;
	private final Obj ancestor;
	private final Obj[] samples;

	public DefCollector(Obj object) {
		this.object = object;

		final TypeRef ancestorRef = object.getAncestor();

		if (ancestorRef == null) {
			this.ancestor = null;
		} else {
			this.ancestor = ancestorRef.getType();
		}

		final Sample[] samples = object.getSamples();

		this.samples = new Obj[samples.length];
		for (int i = 0; i < samples.length; ++i) {
			this.samples[i] = samples[i].getType();
		}
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Obj getAncestor() {
		return this.ancestor;
	}

	public final Obj[] getSamples() {
		return this.samples;
	}

	public void addDef(D def) {

		final Obj source = def.getSource();

		if (explicit(source)) {
			explicitDef(def);
			return;
		}
		if (belongsToSample(source)) {
			explicitDef(def);
			return;
		}
	}

	public void addDefs(D[] sources) {
		for (D source : sources) {
			addDef(source);
		}
	}

	protected abstract void explicitDef(D def);

	private boolean explicit(Obj source) {
		return source == getObject();
	}

	private boolean belongsToSample(Obj source) {
		for (Obj sample : this.samples) {
			if (sample.derivedFrom(source, Derivation.PROPAGATION)) {
				return true;
			}
		}
		return false;
	}

}
