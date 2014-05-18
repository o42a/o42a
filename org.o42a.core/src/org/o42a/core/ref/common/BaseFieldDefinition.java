/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;

import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;


public abstract class BaseFieldDefinition extends FieldDefinition {

	private final Ref ref;

	public BaseFieldDefinition(Ref ref) {
		super(ref);
		assertSameScope(ref);
		this.ref = ref;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public final void overrideObject(ObjectDefiner definer) {

		final DefinitionTarget target = getDefinitionTarget();
		final DefinitionTarget definerTarget = definerTarget(definer);

		if (target.isDefault() || target.is(definerTarget)) {
			overridePlainObject(definer);
			return;
		}
		if (definerTarget.isLink()) {
			overrideLink(definer);
			return;
		}
		if (definerTarget.isMacro()) {
			overrideMacro(definer);
			return;
		}
		overridePlainObject(definer);
	}

	public abstract void overridePlainObject(ObjectDefiner definer);

	public void overrideLink(ObjectDefiner definer) {
		refAsValue(definer);
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		definer.setRef(getRef());
	}

	public void overrideMacro(ObjectDefiner definer) {
		refAsValue(definer);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	protected void refAsValue(ObjectDefiner definer) {
		definer.define(valueBlock(getRef())::definitions);
	}

}
