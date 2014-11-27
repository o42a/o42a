/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field;


/**
 * A kinds of fields contained in object IR structure and VMT.
 */
public enum FldKind {

	STATELESS(-1),
	OBJ(0),
	ALIAS(1),
	VAR(2),
	OWNER(3, true),
	DEP(4, true),
	LOCAL(5),
	RESUME_FROM(6);

	private final int code;
	private final boolean inheritable;

	FldKind(int code) {
		this(code, false);
	}

	FldKind(int code, boolean inheritable) {
		this.code = code;
		this.inheritable = inheritable;
	}

	/**
	 * A code of this field kind.
	 *
	 * @return an integer value corresponding to the one from
	 * {@code o42a_fld_kind} enum, unless negative.
	 */
	public final int code() {
		return this.code;
	}

	/**
	 * Whether fields of this kind are stateless.
	 *
	 * <p>Stateless fields do not present in object IR structure. But they still
	 * may have corresponding records in VMT.</p>
	 *
	 * <p>Field descriptors for stateless fields are not constructed.</p>
	 *
	 * @return <code>false</code> if the {@link #code() code} is negative,
	 * or <code>true</code> otherwise.
	 */
	public final boolean isStateless() {
		return code() < 0;
	}

	/**
	 * Whether fields of this kind can be inherited from ancestor.
	 *
	 * @return <code>true</code> if the fields can be inherited from ancestor,
	 * or <code>false</code> if the fields do not depend on corresponding
	 * fields from object ancestor.
	 */
	public final boolean isInheritable() {
		return this.inheritable;
	}

}
