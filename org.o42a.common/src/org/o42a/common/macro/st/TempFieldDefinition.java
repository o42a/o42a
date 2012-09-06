/*
    Modules Commons
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
package org.o42a.common.macro.st;

import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;
import static org.o42a.core.object.link.LinkValueType.LINK;

import org.o42a.core.Scope;
import org.o42a.core.member.field.*;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.value.ValueStructFinder;


final class TempFieldDefinition extends FieldDefinition {

	private final Ref expansion;
	private final boolean condition;

	TempFieldDefinition(Ref expansion, boolean condition) {
		super(expansion, expansion.distribute());
		this.expansion = expansion;
		this.condition = condition;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return objectDefinition();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAncestor(ancestor(definer.getField().getEnclosingScope()));
		definer.define(new ExpandMacroBlock(expansion()));
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		if (this.expansion == null) {
			return super.toString();
		}
		if (this.condition) {
			return this.expansion.toString();
		}
		return '=' + this.expansion.toString();
	}

	private StaticTypeRef ancestor(Scope scope) {
		if (this.condition) {
			return LINK.typeRef(this.expansion, scope);
		}

		final ValueStructFinder valueStruct =
				new ParentValueStructFinder(scope);

		return LINK.typeRef(this, getScope(), valueStruct);
	}

	private Ref expansion() {

		final LocalScope local = this.expansion.getScope().toLocal();

		if (local == null) {
			return this.expansion;
		}

		final PrefixPath prefix =
				local.toMember()
				.getMemberKey()
				.toPath()
				.toPrefix(local.getEnclosingScope());

		return this.expansion.prefixWith(prefix);
	}

}
