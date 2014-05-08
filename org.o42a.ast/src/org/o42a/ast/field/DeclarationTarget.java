/*
    Abstract Syntax Tree
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
package org.o42a.ast.field;

import org.o42a.ast.atom.SignType;


public enum DeclarationTarget implements SignType {

	VALUE(":=", 0),
	PROTOTYPE(":=>", Mask.PROTOTYPE_FIELD),
	INPUT(":=<", Mask.ABSTRACT_FIELD),
	ABSTRACT(":=<>", Mask.ABSTRACT_FIELD | Mask.PROTOTYPE_FIELD),
	STATIC("::=", Mask.STATIC_FIELD),
	STATIC_PROTOTYPE("::=>", Mask.STATIC_FIELD | Mask.PROTOTYPE_FIELD),
	ALIAS(":-", 0),
	OVERRIDE_VALUE("=", Mask.OVERRIDE_FIELD),
	OVERRIDE_PROTOTYPE("=>", Mask.OVERRIDE_FIELD | Mask.PROTOTYPE_FIELD),
	OVERRIDE_INPUT("=<", Mask.OVERRIDE_FIELD | Mask.ABSTRACT_FIELD),
	OVERRIDE_ABSTRACT(
			"=<>",
			Mask.OVERRIDE_FIELD | Mask.ABSTRACT_FIELD | Mask.PROTOTYPE_FIELD);

	private final String sign;
	private final int mask;

	DeclarationTarget(String sign, int mask) {
		this.sign = sign;
		this.mask = mask;
	}

	@Override
	public String getSign() {
		return this.sign;
	}

	public boolean isOverride() {
		return (this.mask & Mask.OVERRIDE_FIELD) != 0;
	}

	public boolean isPrototype() {
		return (this.mask & Mask.PROTOTYPE_FIELD) != 0;
	}

	public boolean isStatic() {
		return (this.mask & Mask.STATIC_FIELD) != 0;
	}

	public boolean isAbstract() {
		return (this.mask & Mask.ABSTRACT_FIELD) != 0;
	}

	private static final class Mask {

		static final int OVERRIDE_FIELD = 0x01;
		static final int PROTOTYPE_FIELD = 0x02;
		static final int STATIC_FIELD = 0x04;
		static final int ABSTRACT_FIELD = 0x08;

	}

}
