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
package org.o42a.core.st;

import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


public abstract class ImplicationTargets<T extends ImplicationTargets<T>>
		implements LogInfo {

	static final int PREREQUISITE_MASK = 0x01;
	static final int PRECONDITION_MASK = 0x02;
	static final int VALUE_MASK = 0x04;
	static final int CLAIM_MASK = 0x10;
	static final int FIELD_MASK = 0x20;
	static final int CLAUSE_MASK = 0x40;
	static final int EXIT_MASK = 0x100;
	static final int REPEAT_MASK = 0x200;
	static final int ERROR_MASK = 0x1000;

	static final int CONDITIONAL_MASK =
			PREREQUISITE_MASK | PRECONDITION_MASK;
	static final int DECLARING_MASK = FIELD_MASK | CLAUSE_MASK;
	static final int LOOPING_MASK = EXIT_MASK | REPEAT_MASK;
	static final int BREAKING_MASK = VALUE_MASK | LOOPING_MASK;

	private final Loggable loggable;
	private final int mask;

	ImplicationTargets() {
		this.loggable = null;
		this.mask = 0;
	}

	ImplicationTargets(LogInfo loggable, int mask) {
		this.loggable = loggable.getLoggable();
		this.mask = mask;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	public final boolean isEmpty() {
		return (mask() & (~ERROR_MASK)) == 0;
	}

	public final boolean havePrerequisite() {
		return (mask() & PREREQUISITE_MASK) != 0;
	}

	public final boolean havePrecondition() {
		return (mask() & PRECONDITION_MASK) != 0;
	}

	public final boolean haveValue() {
		return (mask() & VALUE_MASK) != 0;
	}

	public final boolean haveError() {
		return (mask() & ERROR_MASK) != 0;
	}

	public final boolean conditional() {
		return (mask() & CONDITIONAL_MASK) != 0;
	}

	public final boolean breaking() {
		return (mask() & BREAKING_MASK) != 0;
	}

	public final T addPrerequisite() {
		return addMask(PREREQUISITE_MASK);
	}

	public final T addError() {
		return addMask(ERROR_MASK);
	}

	public final T add(T other) {
		if (getLoggable() != null || other.getLoggable() == null) {
			return addMask(other.mask());
		}
		return other.addMask(mask());
	}

	public final T override(T other) {
		if (getLoggable() != null || other.getLoggable() == null) {
			return setMask(other.mask());
		}
		return other;
	}

	public final T toPrerequisites() {
		assert !breaking() :
			"Prerequisite should not contain breaking statements";
		if (!havePrecondition()) {
			return self();
		}
		return setMask((mask() & ~PRECONDITION_MASK) | PREREQUISITE_MASK);
	}

	public final T toPreconditions() {
		assert !breaking() :
			"Preconditions should not contain breaking statements";
		if (!havePrerequisite()) {
			return self();
		}
		return setMask((mask() & ~PREREQUISITE_MASK) | PRECONDITION_MASK);
	}

	protected final int mask() {
		return this.mask;
	}

	protected final T addMask(int mask) {
		return setMask(mask() | mask);
	}

	protected final T removeMask(int mask) {
		return setMask(mask() & (~mask));
	}

	protected final T setMask(int mask) {
		if (mask == mask()) {
			return self();
		}
		return create(mask);
	}

	protected abstract T create(int mask);

	@SuppressWarnings("unchecked")
	private final T self() {
		return (T) this;
	}

}
