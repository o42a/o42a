/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.link;

import static org.o42a.core.ir.object.op.ObjHolder.objTrap;

import org.o42a.core.ir.field.ObjectRefVmtRecord;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectRefFn;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.value.TypeParameters;


public abstract class AbstractLinkVmtRecord<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>>
				extends ObjectRefVmtRecord<F, T> {

	public AbstractLinkVmtRecord(RefFld<F, T, ObjectRefFn> fld) {
		super(fld);
	}

	protected ObjectOp construct(ObjBuilder builder, CodeDirs dirs) {

		final Obj object = fld().getField().toObject();

		assert object.type().getValueType().isLink() :
			"Not a link: " + this;

		final TypeParameters<?> parameters = object.type().getParameters();
		final Definitions definitions = object.value().getDefinitions();
		final DefTarget target = definitions.target();

		assert target.exists() :
			"Link target can not be constructed";

		if (target.isUnknown()) {
			dirs.code().go(dirs.falseDir());
			return builder.getContext()
					.getNone()
					.ir(getGenerator())
					.op(builder, dirs.code());
		}

		// Links and variables should trap the object before returning
		// to the caller.
		return target.getRef()
				.op(builder.host())
				.path()
				.target()
				.materialize(dirs, objTrap())
				.cast(
						null,
						dirs,
						parameters.getValueType()
						.toLinkType()
						.interfaceRef(parameters)
						.getType());
	}

}
