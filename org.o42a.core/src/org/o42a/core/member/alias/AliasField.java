/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.member.alias;

import static org.o42a.core.member.field.FieldKind.ALIAS_FIELD;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.alias.AliasFld;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldKind;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;


public final class AliasField extends Field {

	private final Ref ref;

	AliasField(MemberAliasField member) {
		super(member);
		this.ref = member.getRef();
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public final FieldKind getFieldKind() {
		return ALIAS_FIELD;
	}

	@Override
	protected Obj createObject() {
		return this.ref.getResolution().toObject();
	}

	@Override
	protected FieldIR createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends FieldIR {

		IR(Generator generator, AliasField field) {
			super(generator, field);
		}

		@Override
		protected AliasFld declare(ObjectIRBodyData data) {

			final AliasField field = (AliasField) getField();
			final Obj target =
					field.getRef().getResolution().toObject().mostWrapped();
			final AliasFld fld = new AliasFld(field, target);

			fld.allocate(data, target);

			return fld;
		}

		@Override
		protected Fld<?> declareDummy(ObjectIRBodyData data) {

			final AliasField field = (AliasField) getField();
			final AliasFld fld = new AliasFld(field, null);

			fld.allocateDummy(data);

			return fld;
		}

	}

}
