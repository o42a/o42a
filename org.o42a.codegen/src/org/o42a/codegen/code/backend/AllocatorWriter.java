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
package org.o42a.codegen.code.backend;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Disposal;


/**
 * Allocator allocations writer.
 *
 * <p>It is created once by {@link BlockWriter#startAllocation(
 * org.o42a.codegen.code.Allocator)} method, the used to make allocations one or
 * more times by calling {@link #allocate(Code, CodePos)} method. When all
 * allocations are made, the {@link #combine(Code)} method is called to create
 * PHI nodes for all possible allocations.</p>
 *
 * <p>All allocations are disposed with {@link #dispose(Code)} method.</p>
 */
public interface AllocatorWriter extends Disposal {

	/**
	 * Makes an allocator allocations.
	 *
	 * <p>This method can be called multiple times in different code blocks.
	 * After allocation the execution flow jumps to allocator. The
	 * {@link #combine(Code)} method should combine all possible allocations
	 * made in different blocks, e.g. by creating a PHI node.</p>
	 *
	 * @param code code to perform allocation in.
	 * @param target the position of jump position following this allocation.
	 */
	void allocate(Code code, CodePos target);

	/**
	 * Combines all allocations made by {@link #allocate(Code, CodePos)}
	 * method call.
	 *
	 * <p>This method is called once per allocator. But it can be called
	 * multiple times: for allocator itself, and for its nested allocators.
	 * In the latter case it should combine allocations made by parent allocator
	 * with allocations made specifically for target one (see {@code target}
	 * argument of {@link #allocate(Code, CodePos)} method).</p>
	 *
	 * @param code code to combine allocations in.
	 */
	void combine(Code code);

}
