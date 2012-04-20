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
package org.o42a.core.object.link.impl;

import static org.o42a.core.object.link.impl.LinkCopyDef.linkValue;

import org.o42a.core.Scope;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;


public class LinkValueAdapter extends ValueAdapter {

	private final Ref ref;
	private final LinkValueStruct expectedStruct;

	public LinkValueAdapter(Ref ref, LinkValueStruct expectedStruct) {
		assert expectedStruct != null :
			"Link value structure not specified";
		this.ref = ref;
		this.expectedStruct = expectedStruct;
	}

	public final Ref ref() {
		return this.ref;
	}

	public final LinkValueStruct getExpectedStruct() {
		return this.expectedStruct;
	}

	@Override
	public Def valueDef() {
		return new LinkCopyDef(ref(), getExpectedStruct().getValueType());
	}

	@Override
	public Logical logical(Scope scope) {
		return ref().rescope(scope).getLogical();
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return linkValue(
				ref(),
				resolver,
				getExpectedStruct().getValueType());
	}

	@Override
	public LogicalValue initialCond(LocalResolver resolver) {
		return ref().value(resolver).getKnowledge().toLogicalValue();
	}

}
