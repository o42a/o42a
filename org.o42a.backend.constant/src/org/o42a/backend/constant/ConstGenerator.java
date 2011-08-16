/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant;

import org.o42a.codegen.Generator;
import org.o42a.codegen.ProxyGenerator;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public class ConstGenerator extends ProxyGenerator {

	public ConstGenerator(Generator proxiedGenerator) {
		super(proxiedGenerator);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		getProxiedGenerator().close();
	}

	@Override
	protected CodeBackend codeBackend() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DataAllocator dataAllocator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DataWriter dataWriter() {
		// TODO Auto-generated method stub
		return null;
	}

}
