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


public class DefaultCodeId extends CodeId {

	private final static char[] HEX_DIGITS = {
		'0', '1', '2', '3',
		'4', '5', '6', '7',
		'8', '9', 'A', 'B',
		'C', 'D', 'E', 'F',
	};

	private Separator startsFrom = Separator.NONE;
	private String id = "";
	private boolean lastEncoded;
	private CodeId local;

	protected DefaultCodeId(boolean top) {
		this.local = this;
		if (top) {
			this.id = separatorSign(Separator.TOP);
			this.startsFrom = Separator.TOP;
		}
	}

	protected DefaultCodeId(String id, boolean raw) {
		assert id != null :
			"Identifier not specified";
		this.local = this;
		if (raw) {
			this.id = id;
		} else {

			final StringBuilder result = new StringBuilder(id.length());

			append(result, id);

			this.id = result.toString();
		}
	}

	@Override
	public final String getId() {
		return this.id;
	}

	@Override
	public final Separator getStartsFrom() {
		return this.startsFrom;
	}

	@Override
	public final CodeId getLocal() {
		return this.local;
	}

	@Override
	public DefaultCodeId setLocal(CodeId local) {
		assert local != null :
			"Local not specified";

		final DefaultCodeId clone = clone();
		final int len = this.id.length();
		final String localId = local.getId();

		if (len == 0) {
			clone.id = localId;
		} else {

			final StringBuilder newId =
				new StringBuilder(len + localId.length() + 2);

			newId.append(this.id);
			clone.appendSeparator(newId, Separator.SUB, local.getStartsFrom());
			clone.appendId(newId, local);

			clone.id = newId.toString();
		}

		clone.local = local;

		return clone;
	}

	@Override
	public CodeId setLocal(String local) {
		return setLocal(new DefaultCodeId(local, false));
	}

	@Override
	public CodeId removeLocal() {
		if (this.local == this) {
			return this;
		}

		final DefaultCodeId clone = clone();

		clone.local = clone;

		return clone;
	}

	@Override
	public final boolean isLastEncoded() {
		return this.lastEncoded;
	}

	@Override
	protected CodeId separate(Separator separator, String name, boolean raw) {

		final DefaultCodeId clone = clone();
		final StringBuilder result =
			new StringBuilder(this.id.length() + name.length() + 2);

		result.append(this.id);
		clone.appendSeparator(
				result,
				separator,
				Separator.NONE);
		if (raw) {
			result.append(name);
		} else {
			clone.append(result, name);
		}
		clone.id = result.toString();

		return clone;
	}

	@Override
	protected CodeId separate(Separator separator, CodeId id) {

		final DefaultCodeId clone = clone();
		final StringBuilder result = new StringBuilder(
				this.id.length() + id.getId().length() + 2);

		result.append(this.id);
		clone.appendSeparator(
				result,
				separator,
				id.getStartsFrom());
		clone.appendId(result, id);
		clone.id = result.toString();

		return clone;
	}

	protected void append(StringBuilder result, String name) {

		final int len = name.length();
		int i = 0;

		while (i < len) {

			final int c = name.codePointAt(i);

			i += Character.charCount(c);

			if (isSpecial(c)) {
				appendSpecial(result, c);
				this.lastEncoded = false;
			} else if (isEncoded(c)) {
				if (this.lastEncoded) {
					closeEncoded(result);
					this.lastEncoded = false;
				}
				result.appendCodePoint(c);
			} else if (
					(c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z')
					|| c == '_') {
				result.appendCodePoint(c);
				this.lastEncoded = false;
			} else if (c == '-') {
				result.append("__");
				this.lastEncoded = false;
			} else {
				appendEncoded(result, c);
				this.lastEncoded = true;
			}
		}
	}

	protected void appendId(StringBuilder result, CodeId separator) {
		result.append(separator.getId());
		this.lastEncoded = separator.isLastEncoded();
	}

	protected boolean isSpecial(int c) {
		if (c == 'X') {
			return true;
		}
		if (c == 'Z' && isLastEncoded()) {
			return true;
		}
		return false;
	}

	protected boolean isEncoded(int c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
	}

	protected void appendSpecial(StringBuilder result, int c) {
		result.append('X').appendCodePoint(c);
	}

	protected void appendEncoded(StringBuilder result, int c) {
		result.append('X');

		char[] digits = new char[32];
		int i = 32;

		do {
		    digits[--i] = HEX_DIGITS[c & 0xF];
		    c >>>= 4;
		} while (c != 0);

		result.append(digits, i, 32 - i);
	}

	protected void closeEncoded(StringBuilder result) {
		result.append('Z');
	}

	private void appendSeparator(
			StringBuilder result,
			Separator separator,
			Separator next) {
		if (result.length() == 0) {
			if (separator != Separator.SUB) {
				appendSeparator(result, separator);
				this.startsFrom = separator;
			} else {
				this.startsFrom = next;
			}
			return;
		}
		if (isTop()) {
			if (separator != Separator.SUB) {
				appendSeparator(result, separator);
			}
			return;
		}
		if (separator != Separator.SUB) {
			appendSeparator(result, separator);
			return;
		}
		switch (next) {
		case NONE:
		case TOP:
		case IN:
			appendSeparator(result, separator);
			return;
		default:
			return;
		}
	}

	protected String separatorSign(Separator separator) {
		return separator.getDefaultSign();
	}

	@Override
	protected DefaultCodeId clone() {
		return (DefaultCodeId) super.clone();
	}

	private boolean isTop() {
		if (this.startsFrom != Separator.TOP) {
			return false;
		}
		return this.id.length() == separatorSign(Separator.TOP).length();
	}

	private void appendSeparator(
			StringBuilder result,
			Separator separator) {
		result.append(separatorSign(separator));
		this.lastEncoded = false;
	}

}
