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
package org.o42a.core.value.impl;

import org.o42a.core.Scope;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.ValueDef;
import org.o42a.core.def.impl.RefCondDef;
import org.o42a.core.def.impl.RefValueDef;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.*;


public class RawValueAdapter extends ValueAdapter {

	private final Ref ref;

	public RawValueAdapter(Ref ref) {
		this.ref = ref;
	}

	public Ref ref() {
		return this.ref;
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return ref().valueStruct(scope);
	}

	@Override
	public ValueDef valueDef() {
		return new RefValueDef(this.ref);
	}

	@Override
	public CondDef condDef() {
		return new RefCondDef(this.ref);
	}

	@Override
	public Logical logical(Scope scope) {
		return ref().rescope(scope).getLogical();
	}

	@Override
	public Value<?> initialValue(LocalResolver resolver) {
		return ref().value(resolver);
	}

	@Override
	public LogicalValue initialLogicalValue(LocalResolver resolver) {
		return ref().value(resolver).getKnowledge().toLogicalValue();
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

}
