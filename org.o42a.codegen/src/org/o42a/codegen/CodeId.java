/*
    Compiler Code Generator
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.codegen;


public abstract class CodeId implements Cloneable {

	public abstract String getId();

	public abstract Separator getStartsFrom();

	public abstract CodeId getLocal();

	public abstract CodeId setLocal(CodeId local);

	public abstract CodeId setLocal(String local);

	public abstract CodeId removeLocal();

	public abstract boolean isLastEncoded();

	public final CodeId sub(String name) {
		assert name != null :
			"Identifier not specified";
		return separate(Separator.SUB, name, false);
	}

	public final CodeId rawSub(String name) {
		assert name != null :
			"Identifier not specified";
		return separate(Separator.SUB, name, true);
	}

	public final CodeId sub(CodeId id) {
		assert id != null :
			"Identifier not specified";
		return separate(Separator.SUB, id);
	}

	public final CodeId anonymous(int index) {
		return separate(Separator.ANONYMOUS, Integer.toString(index), true);
	}

	public final CodeId detail(String detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(Separator.DETAIL, detail, false);
	}

	public final CodeId detail(CodeId detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(Separator.DETAIL, detail);
	}

	public final CodeId type(CodeId type) {
		assert type != null :
			"Identifier not specified";
		return separate(Separator.TYPE, type);
	}

	public final CodeId in(CodeId in) {
		assert in != null :
			"Identifier not specified";
		return separate(Separator.IN, in);
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected abstract CodeId separate(
			Separator separator,
			String name,
			boolean raw);

	protected abstract CodeId separate(Separator separator, CodeId id);

	@Override
	protected CodeId clone() {
		try {
			return (CodeId) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public enum Separator {

		NONE(""),
		TOP("."),
		SUB("."),
		ANONYMOUS("."),
		DETAIL("$"),
		TYPE("$$"),
		IN(".$");

		private final String defaultSign;

		Separator(String defaultSign) {
			this.defaultSign = defaultSign;
		}

		public String getDefaultSign() {
			return this.defaultSign;
		}

	}

}
