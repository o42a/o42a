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
package org.o42a.core.ir.local;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.value.ValOp;


final class DefaultMainControl extends MainControl {

	private Block returnCode;

	public DefaultMainControl(
			LocalBuilder builder,
			Block code,
			CodePos exit,
			CodePos falseDir,
			ValOp result) {
		super(builder, code, exit, falseDir, result);
	}

	@Override
	final CodePos returnDir() {
		if (this.returnCode != null) {
			return this.returnCode.head();
		}

		this.returnCode = code().addBlock(code().id("return"));
		this.returnCode.returnVoid();

		return this.returnCode.head();
	}

}
