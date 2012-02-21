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
package org.o42a.core.object.value;

import static org.o42a.core.object.def.DefKind.*;

import org.o42a.analysis.use.Usable;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.DefKind;


public abstract class ObjectValueParts {

	private final Obj object;

	private final CondPart requirement;
	private final CondPart condition;
	private final ValuePart claim;
	private final ValuePart proposition;

	public ObjectValueParts(Obj object) {
		this.object = object;

		final ObjectValue objectValue = (ObjectValue) this;

		this.requirement = new CondPart(objectValue, REQUIREMENT);
		this.condition = new CondPart(objectValue, CONDITION);
		this.claim = new ValuePart(objectValue, CLAIM);
		this.proposition = new ValuePart(objectValue, PROPOSITION);
	}

	public final Obj getObject() {
		return this.object;
	}

	public final CondPart requirement() {
		return this.requirement;
	}

	public final CondPart condition() {
		return this.condition;
	}

	public final ValuePart claim() {
		return this.claim;
	}

	public final ValuePart proposition() {
		return this.proposition;
	}

	public final CondPart condPart(boolean requirement) {
		return requirement ? requirement() : condition();
	}

	public final CondPart condPart(DefKind condKind) {
		assert !condKind.isValue() :
			"Condition definition kind expected: " + condKind;
		return condPart(condKind.isClaim());
	}

	public final ValuePart valuePart(boolean claim) {
		return claim ? claim() : proposition();
	}

	public final ValuePart valuePart(DefKind valueKind) {
		assert valueKind.isValue() :
			"Value definition kind expected: " + valueKind;
		return valuePart(valueKind.isClaim());
	}

	public final ObjectValuePart<?, ?> part(DefKind defKind) {
		switch (defKind) {
		case REQUIREMENT:
			return requirement();
		case CONDITION:
			return condition();
		case CLAIM:
			return claim();
		case PROPOSITION:
			return proposition();
		}

		throw new IllegalArgumentException(
				"Unsupported definition kind: " + defKind);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ObjectValue[" + this.object + ']';
	}

	protected abstract Usable<ValueUsage> uses();

}
