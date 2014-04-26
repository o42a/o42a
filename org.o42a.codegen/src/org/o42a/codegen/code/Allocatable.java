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

	int NORMAL_DISPOSE_PRIORITY = 0;
	int LAST_DISPOSE_PRIORITY = -1;
	int DEBUG_DISPOSE_PRIORITY = -2;

	AllocationMode getAllocationMode();

	int getDisposePriority();

	T allocate(Allocations code, Allocated<T> allocated);

	void init(Code code, T allocated);

	void dispose(Code code, Allocated<T> allocated);

}
