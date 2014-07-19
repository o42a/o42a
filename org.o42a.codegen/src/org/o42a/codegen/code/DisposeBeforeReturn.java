/*
    Compiler Code Generator
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
package org.o42a.codegen.code;


final class DisposeBeforeReturn implements BeforeReturn {

	private final BeforeReturn beforeReturn;

	DisposeBeforeReturn(BeforeReturn beforeReturn) {
		this.beforeReturn = beforeReturn;
	}

	@Override
	public void beforeReturn(Code code, boolean dispose) {
		if (dispose) {
			disposeAll(code);
		}
		this.beforeReturn.beforeReturn(code, dispose);
	}

	private static void disposeAll(Code code) {

		Allocator allocator = code.getAllocator();

		do {
			allocator.dispose(code);
			allocator = allocator.getEnclosingAllocator();
		} while (allocator != null);
	}

}
