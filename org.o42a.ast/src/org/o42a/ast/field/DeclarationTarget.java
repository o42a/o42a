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

	VALUE(":=", false, false, false),
	INPUT(":=<", false, true, false),
	PROTOTYPE(":=>", false, false, true),
	ABSTRACT(":=<>", false, true, true),
	OVERRIDE_VALUE("=", true, false, false),
	OVERRIDE_INPUT("=<", true, true, false),
	OVERRIDE_PROTOTYPE("=>", true, false, true),
	OVERRIDE_ABSTRACT("=<>", true, true, true);

	private final String sign;
	private final boolean override;
	private final boolean _abstract;
	private final boolean prototype;

	DeclarationTarget(
			String sign,
			boolean override,
			boolean _abstract,
			boolean prototype) {
		this.sign = sign;
		this.override = override;
		this._abstract = _abstract;
		this.prototype = prototype;
	}

	@Override
	public String getSign() {
		return this.sign;
	}

	public boolean isOverride() {
		return this.override;
	}

	public boolean isAbstract() {
		return this._abstract;
	}

	public boolean isPrototype() {
		return this.prototype;
	}

}
