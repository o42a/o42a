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
package org.o42a.core.ir.field.link;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.op.ObjHolder.objTrap;
import static org.o42a.core.object.type.DerivationUsage.ALL_DERIVATION_USAGES;

import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectRefFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.link.LinkValueStruct;


public abstract class AbstractLinkFld extends RefFld<ObjectRefFunc> {

	public AbstractLinkFld(Field field, Obj target) {
		super(field, target);
	}

	@Override
	protected boolean mayOmit() {
		if (!super.mayOmit()) {
			return false;
		}
		return !getTarget().type().derivation().isUsed(
				getGenerator().getAnalyzer(),
				ALL_DERIVATION_USAGES);
	}

	@Override
	protected Obj targetType(Obj bodyType) {

		final Obj object =
				bodyType.member(getField().getKey())
				.toField()
				.object(dummyUser());

		final LinkValueStruct linkStruct =
				object.value().getValueStruct().toLinkStruct();

		return linkStruct.getTypeRef().getType();
	}

	protected final ObjectOp construct(ObjBuilder builder, CodeDirs dirs) {

		final Obj object = getField().toObject();
		final ObjectValue objectValue = object.value();

		assert objectValue.getValueType().isLink() :
			"Not a link: " + this;

		final LinkValueStruct linkStruct =
				objectValue.getValueStruct().toLinkStruct();
		final Definitions definitions = objectValue.getDefinitions();
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
		// to caller.
		return target.getRef()
				.op(builder.host())
				.target(dirs)
				.materialize(dirs, objTrap())
				.cast(null, dirs, linkStruct.getTypeRef().getType());
	}

}