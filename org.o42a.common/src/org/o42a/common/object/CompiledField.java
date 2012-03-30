/*
    Modules Commons
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.common.object;

import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.ObjectField;
import org.o42a.core.source.FieldCompiler;


public final class CompiledField extends ObjectField {

	private final FieldCompiler compiler;

	CompiledField(
			MemberOwner owner,
			FieldDeclaration declaration,
			FieldCompiler compiler) {
		super(owner, declaration);
		this.compiler = compiler;
	}

	private CompiledField(MemberField member, CompiledField propagatedFrom) {
		super(member, propagatedFrom);
		this.compiler = propagatedFrom.compiler;
	}

	public final FieldCompiler getCompiler() {
		return this.compiler;
	}

	@Override
	public final Obj toObject() {

		final Obj object = getScopeObject();

		if (object != null) {
			return object;
		}

		return setScopeObject(new CompiledObject(this));
	}

	@Override
	protected CompiledField propagate(MemberField member) {
		return new CompiledField(member, this);
	}

	final void init(Obj object) {
		setScopeObject(object);
	}

}
