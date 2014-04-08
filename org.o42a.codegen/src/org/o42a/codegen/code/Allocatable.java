/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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


public interface Allocatable<T> {

	int NORMAL_ALLOC_PRIORITY = 1;
	int HIGH_ALLOC_PRIORITY = 0;
	int DEBUG_ALLOC_PRIORITY = -1;

	/**
	 * Whether allocation created for this allocatable is mandatory.
	 *
	 * <p>Mandatory allocations are always made in their allocator, otherwise
	 * allocations are only made when necessary.</p>
	 *
	 * @return <code>true</code> if allocation is mandatory, or
	 * <code>false</code> otherwise.
	 */
	boolean isMandatory();

	int getPriority();

	T allocate(AllocationCode<T> code);

	void initialize(AllocationCode<T> code);

	void dispose(Code code, Allocated<T> allocated);

}
