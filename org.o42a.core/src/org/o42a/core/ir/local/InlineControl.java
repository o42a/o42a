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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;


public class InlineControl extends MainControl {

	private Code returnCode;

	public InlineControl(
			CodeBuilder builder,
			Code code,
			CodePos exit,
			CodePos falseDir) {
		super(builder, code, exit, falseDir);
	}

	@Override
	public void end() {
		super.end();
		if (this.returnCode != null) {
			this.returnCode.go(code().tail());
		}
	}

	@Override
	CodePos returnDir() {
		if (this.returnCode == null) {
			this.returnCode = code().addBlock("return");
		}
		return this.returnCode.head();
	}

}
