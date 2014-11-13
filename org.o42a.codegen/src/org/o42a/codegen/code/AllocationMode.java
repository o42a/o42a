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


/**
 * Allocation mode of {@link Allocatable#getAllocationMode() allocatable}.
 */
public enum AllocationMode {

	/**
	 * Allocate nothing. Only dispose.
	 */
	NO_ALLOCATION,

	/**
	 * Allocate when needed.
	 */
	LAZY_ALLOCATION,

	/**
	 * Allocate in allocator when needed.
	 */
	ALLOCATOR_ALLOCATION,

	/**
	 * Allocate in first non-debug allocator when needed.
	 */
	NON_DEBUG_ALLOCATION,

	/**
	 * Always allocate in allocator.
	 */
	MANDATORY_ALLOCATION,

	/**
	 * Allocate in first non-debug allocator when needed.
	 */
	NON_DEBUG_MANDATORY_ALLOCATION,;

	final boolean supportsAllocation() {
		return this != NO_ALLOCATION;
	}

	final boolean inAllocator() {
		return ordinal() >= ALLOCATOR_ALLOCATION.ordinal();
	}

	final boolean isMandatory() {
		return ordinal() >= MANDATORY_ALLOCATION.ordinal();
	}

	final boolean isNonDebug() {
		return this == NON_DEBUG_ALLOCATION
				|| this == NON_DEBUG_MANDATORY_ALLOCATION;
	}

}
