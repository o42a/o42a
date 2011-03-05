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
package org.o42a.core.ir;

import static org.o42a.core.ir.op.BinaryFunc.BINARY;
import static org.o42a.core.ir.op.ObjectCondFunc.OBJECT_COND;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;
import static org.o42a.core.ir.op.ObjectValFunc.OBJECT_VAL;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.CodePtr;
import org.o42a.core.ir.object.ObjectIRGenerator;
import org.o42a.core.ir.op.*;


public class IRGenerator extends ObjectIRGenerator {

	public IRGenerator(Generator generator) {
		super(generator);
	}

	public final CodePtr<BinaryFunc> castFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_cast",
				BINARY);
	}

	public final CodePtr<ObjectRefFunc> newFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_new",
				OBJECT_REF);
	}

	public final CodePtr<ObjectCondFunc> falseFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_cond_false",
				OBJECT_COND);
	}

	public final CodePtr<ObjectCondFunc> trueFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_cond_true",
				OBJECT_COND);
	}

	public final CodePtr<ObjectValFunc> falseValFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_val_false",
				OBJECT_VAL);
	}

	public final CodePtr<ObjectValFunc> unknownValFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_val_unknown",
				OBJECT_VAL);
	}

	public final CodePtr<ObjectRefFunc> nullObjectRef() {
		return getGenerator().externalFunction(
				"o42a_obj_ref_null",
				OBJECT_REF);
	}

}
