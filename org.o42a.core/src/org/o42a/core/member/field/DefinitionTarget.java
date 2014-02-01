/*
    Compiler Core
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
package org.o42a.core.member.field;

import org.o42a.core.value.TypeParameters;


public final class DefinitionTarget {

	private static final DefinitionTarget DEFAULT_DEFINITION =
			new DefinitionTarget();
	private static final DefinitionTarget OBJECT_DEFINITION =
			new DefinitionTarget(0, false);
	private static final DefinitionTarget MACRO_DEFINITION =
			new DefinitionTarget(0, true);
	private static final DefinitionTarget LINK_DEFINITION =
			new DefinitionTarget(1, false);
	private static final DefinitionTarget LINK_TO_LINK_DEFINITION =
			new DefinitionTarget(2, false);

	public static DefinitionTarget defaultDefinition() {
		return DEFAULT_DEFINITION;
	}

	public static DefinitionTarget objectDefinition() {
		return OBJECT_DEFINITION;
	}

	public static DefinitionTarget macroDefinition() {
		return MACRO_DEFINITION;
	}

	public static DefinitionTarget linkDefinition(int linkDepth) {
		assert linkDepth > 0 :
			"Link depth should be positive";
		if (linkDepth == 1) {
			return LINK_DEFINITION;
		}
		if (linkDepth == 2) {
			return LINK_TO_LINK_DEFINITION;
		}
		return new DefinitionTarget(linkDepth, false);
	}

	public static DefinitionTarget definitionTarget(
			TypeParameters<?> typeParameters) {
		if (typeParameters.getValueType().isMacro()) {
			return macroDefinition();
		}

		final int linkDepth = typeParameters.getLinkDepth();

		if (linkDepth != 0) {
			return linkDefinition(linkDepth);
		}

		return objectDefinition();
	}

	private final int linkDepth;
	private final boolean macro;
	private final boolean isDefault;

	private DefinitionTarget() {
		this.linkDepth = 0;
		this.macro = false;
		this.isDefault = true;
	}

	private DefinitionTarget(int linkDepth, boolean macro) {
		this.linkDepth = linkDepth;
		this.macro = macro;
		this.isDefault = false;
	}

	public final boolean isDefault() {
		return this.isDefault;
	}

	public final boolean isMacro() {
		return this.macro;
	}

	public final boolean isLink() {
		return getLinkDepth() > 0;
	}

	public final int getLinkDepth() {
		return this.linkDepth;
	}

	public final boolean is(DefinitionTarget other) {
		if (this.linkDepth != other.linkDepth) {
			return false;
		}
		if (this.macro != other.macro) {
			return false;
		}
		if (this.isDefault != other.isDefault) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + this.linkDepth;
		result = prime * result + (this.macro ? 1231 : 1237);

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final DefinitionTarget other = (DefinitionTarget) obj;

		return is(other);
	}

	@Override
	public String toString() {
		if (this.linkDepth != 0) {
			if (this.linkDepth == 1) {
				return "LinkDefinition";
			}
			return "LinkDefinition[depth=" + this.linkDepth + ']';
		}
		if (this.macro) {
			return "MacroDefinition";
		}
		if (this.isDefault) {
			return "DefaultDefinition";
		}
		return "ObjectDefinition";
	}

}
