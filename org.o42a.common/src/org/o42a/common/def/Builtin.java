/*
    Modules Commons
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
package org.o42a.common.def;

import org.o42a.codegen.code.Code;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;


public interface Builtin extends LocationInfo {

	Obj toObject();

	Value<?> calculateBuiltin(Resolver resolver);

	void resolveBuiltin(Obj object);

	void writeBuiltin(Code code, ValOp result, HostOp host);

}
