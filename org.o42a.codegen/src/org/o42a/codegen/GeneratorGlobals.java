/*
    Compiler Code Generator
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
package org.o42a.codegen;

import org.o42a.codegen.data.Globals;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class GeneratorGlobals extends Globals {

	GeneratorGlobals(Generator generator) {
		super(generator);
	}

	@Override
	public DataAllocator dataAllocator() {
		return getGenerator().dataAllocator();
	}

	@Override
	public DataWriter dataWriter() {
		return getGenerator().dataWriter();
	}

	@Override
	protected void registerType(SubData<?> type) {
		getGenerator().registerType(type);
	}

	@Override
	protected void addType(SubData<?> type) {
		getGenerator().addType(type);
	}

	@Override
	protected void addGlobal(SubData<?> global) {
		getGenerator().addGlobal(global);
	}

}
