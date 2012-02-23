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
package org.o42a.core.object.link;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.value.ValueStructIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;


public final class LinkValueStruct
		extends ValueStruct<LinkValueStruct, ObjectLink> {

	private final TypeRef typeRef;

	LinkValueStruct(LinkValueType valueType, TypeRef typeRef) {
		super(valueType, ObjectLink.class);
		this.typeRef = typeRef;
	}

	@Override
	public final LinkValueType getValueType() {
		return (LinkValueType) super.getValueType();
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	@Override
	public ValueDef constantDef(
			Obj source,
			LocationInfo location,
			ObjectLink value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeRelation relationTo(ValueStruct<?, ?> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean convertibleFrom(ValueStruct<?, ?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkValueStruct prefixWith(PrefixPath prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkValueStruct upgradeScope(Scope toScope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScopeInfo toScoped() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkValueStruct reproduce(Reproducer reproducer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ValueKnowledge valueKnowledge(ObjectLink value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Value<ObjectLink> prefixValueWith(
			Value<ObjectLink> value,
			PrefixPath prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void resolveAll(Value<ObjectLink> value, Resolver resolver) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ValueStructIR<LinkValueStruct, ObjectLink> createIR(
			Generator generator) {
		// TODO Auto-generated method stub
		return null;
	}

}
