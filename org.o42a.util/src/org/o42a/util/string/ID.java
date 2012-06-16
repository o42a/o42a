/*
    Utilities
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
package org.o42a.util.string;

import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;


public final class ID {

	private static final Name NO_NAME = CASE_SENSITIVE.canonicalName("");
	private static final ID NO_ID =
			new ID(null, Separator.NONE, NO_NAME, null, true);
	private static final ID TOP_ID =
			new ID(null, Separator.TOP, NO_NAME, null, true);

	public static ID id() {
		return NO_ID;
	}

	public static ID topId() {
		return TOP_ID;
	}

	public static ID id(String name) {
		return new ID(
				null,
				Separator.NONE,
				CASE_SENSITIVE.canonicalName(name),
				null,
				false);
	}

	public static ID rawId(String name) {
		return new ID(
				null,
				Separator.NONE,
				CASE_SENSITIVE.canonicalName(name),
				null,
				true);
	}

	private final ID prefix;
	private final Separator separator;
	private final Name name;
	private final ID suffix;
	private final boolean raw;

	private ID(
			ID prefix,
			Separator separator,
			Name name,
			ID suffix,
			boolean raw) {
		this.prefix = prefix;
		this.separator = separator;
		this.name = name;
		this.suffix = suffix;
		this.raw = raw;
	}

	public final ID getPrefix() {
		return this.prefix;
	}

	public final Separator getSeparator() {
		return this.separator;
	}

	public final Name getName() {
		return this.name;
	}

	public final ID getSuffix() {
		return this.suffix;
	}

	public final boolean isRaw() {
		return this.raw;
	}

	public final ID sub(String name) {
		assert name != null :
			"Identifier not specified";
		return separate(Separator.SUB, name, false);
	}

	public final ID sub(Name name) {
		assert name != null :
			"Identifier not specified";
		return separate(Separator.SUB, name, false);
	}

	public final ID rawSub(String name) {
		assert name != null :
			"Identifier not specified";
		return separate(Separator.SUB, name, true);
	}

	public final ID rawSub(Name name) {
		assert name != null :
			"Identifier not specified";
		return separate(Separator.SUB, name, true);
	}

	public final ID sub(ID id) {
		assert id != null :
			"Identifier not specified";
		return separate(Separator.SUB, id);
	}

	public final ID anonymous(int index) {
		return separate(Separator.ANONYMOUS, Integer.toString(index), true);
	}

	public final ID anonymous(String name) {
		return separate(Separator.ANONYMOUS, name, false);
	}

	public final ID anonymous(Name name) {
		return separate(Separator.ANONYMOUS, name, false);
	}

	public final ID detail(String detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(Separator.DETAIL, detail, false);
	}

	public final ID detail(Name detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(Separator.DETAIL, detail, false);
	}

	public final ID detail(ID detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(Separator.DETAIL, detail);
	}

	public final ID type(ID type) {
		assert type != null :
			"Identifier not specified";
		return separate(Separator.TYPE, type);
	}

	public final ID type(String type) {
		assert type != null :
			"Identifier not specified";
		return separate(Separator.TYPE, type, false);
	}

	public final ID type(Name type) {
		assert type != null :
			"Identifier not specified";
		return separate(Separator.TYPE, type, false);
	}

	public final ID in(ID in) {
		assert in != null :
			"Identifier not specified";
		return separate(Separator.IN, in);
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}

		final StringNameWriter out = new StringNameWriter();

		out.write(this);

		return out.toString();
	}

	private ID separate(Separator separator, ID id) {
		if (getSuffix() == null && getName().isEmpty()) {
			if (getPrefix() == null
					&& id.startsWith().discardsPrev(separator)) {
				return id;
			}
			return new ID(getPrefix(), separator, NO_NAME, id, true);
		}
		return new ID(this, separator, NO_NAME, id, true);
	}

	private final ID separate(Separator separator, String name, boolean raw) {
		return separate(separator, CASE_SENSITIVE.name(name), raw);
	}

	private ID separate(Separator separator, Name name, boolean raw) {
		if (getSuffix() == null
				&& getName().isEmpty()
				&& getSeparator().discardsNext(separator)) {
			return new ID(getPrefix(), getSeparator(), name, null, raw);
		}
		return new ID(this, separator, name, null, raw);
	}

	private final Separator startsWith() {

		ID id = this;

		for (;;) {

			final ID prefix = id.getPrefix();

			if (prefix == null) {
				return id.getSeparator();
			}

			id = prefix;
		}
	}

	public enum Separator {

		NONE("") {

			@Override
			boolean discardsPrev(Separator prev) {
				return false;
			}

			@Override
			boolean discardsNext(Separator next) {
				return false;
			}

		},

		TOP("$$") {

			@Override
			boolean discardsPrev(Separator prev) {
				return prev != TYPE && prev != IN;
			}

		},
		SUB(":"),
		ANONYMOUS(":"),
		DETAIL("/"),
		TYPE("@@"),
		IN("@");

		private final String defaultSign;

		Separator(String defaultSign) {
			this.defaultSign = defaultSign;
		}

		public final boolean isNone() {
			return this == NONE;
		}

		public String getDefaultSign() {
			return this.defaultSign;
		}

		boolean discardsPrev(Separator prev) {
			return prev == NONE;
		}

		boolean discardsNext(Separator next) {
			return next == this || next != TOP;
		}

	}

}
