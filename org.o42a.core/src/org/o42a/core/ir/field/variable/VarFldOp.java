/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.field.variable;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondCode;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRType;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectRefFunc;


public class VarFldOp extends RefFldOp<VarFld.Op, ObjectRefFunc> {

	VarFldOp(VarFld fld, ObjOp host, VarFld.Op ptr) {
		super(fld, host, ptr);
	}

	@Override
	public VarFld fld() {
		return (VarFld) super.fld();
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {

		final Link variable = fld().getField().getArtifact();
		final Code code = dirs.code();
		final ObjectOp valueObject = value.materialize(dirs);
		final StructRecOp<ObjectIRType.Op> boundRec = ptr().bound(null, code);
		final ObjectIRType.Op knownBound = boundRec.load(null, code);

		// Bound is already known.
		final CondCode boundUnknown =
				knownBound.isNull(null, code)
				.branch(code, "bound_unknown", "bound_known");
		final Code boundKnown = boundUnknown.otherwise();

		if (boundKnown.isDebug()) {
			boundKnown.dumpName(
					"Known bound: ",
					knownBound.toData(null, boundKnown));
		}

		final CodeDirs boundKnownDirs = dirs.sub(boundKnown);
		final ObjectOp castObject = valueObject.dynamicCast(
				boundKnown.id("cast_target"),
				boundKnownDirs,
				knownBound.op(getBuilder(), DERIVED),
				variable.getTypeRef().typeObject(dummyUser()),
				true);

		ptr().object(null, boundKnown).store(
				boundKnown,
				castObject.toData(boundKnown));
		boundKnown.dump("Assigned: ", ptr());
		castObject.writeLogicalValue(boundKnownDirs);
		boundKnown.go(code.tail());

		// Bound is not known yet.
		final VariableAssignerFunc assigner =
				ptr().assigner(null, boundUnknown).load(null, boundUnknown);
		final BoolOp assigned =
				assigner.assign(boundUnknown, host(), valueObject);

		assigned.goUnless(boundUnknown, dirs.falseDir());
		boundUnknown.go(code.tail());
	}

}
