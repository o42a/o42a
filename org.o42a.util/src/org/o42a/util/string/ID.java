/*
    Utilities
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
package org.o42a.util.string;

import static org.o42a.util.string.Capitalization.AS_IS;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;
import static org.o42a.util.string.DisplayNameEncoder.DISPLAY_NAME_ENCODER;


public final class ID implements SubID {

	private static final Name NO_NAME = AS_IS.name("");
	private static final ID NO_ID =
			new ID(null, IDSeparator.NONE, NO_NAME, null);
	private static final ID TOP_ID =
			new ID(null, IDSeparator.TOP, NO_NAME, null);

	public static ID id() {
		return NO_ID;
	}

	public static ID topId() {
		return TOP_ID;
	}

	public static ID id(String name) {
		return CASE_SENSITIVE.name(name).toID();
	}

	public static ID rawId(String name) {
		return AS_IS.name(name).toID();
	}

	public static ID id(SubID subID) {
		return new ID(null, IDSeparator.NONE, NO_NAME, subID);
	}

	public static SubID displayText(String text) {
		return new DescriptionOfID(ID.id(), text);
	}

	private final ID prefix;
	private final IDSeparator separator;
	private final Name name;
	private final SubID suffix;
	private ID local;

	ID(ID prefix, IDSeparator separator, Name name, SubID suffix) {
		this.prefix = prefix;
		this.name = name;
		this.suffix = suffix;
		this.local = this;
		if (prefix == null
				&& !separator.isTop()
				&& !separator.isNone()
				&& (!name.isEmpty() || suffix != null)) {
			this.separator = IDSeparator.NONE;
		} else {
			this.separator = separator;
		}
	}

	public final ID getPrefix() {
		return this.prefix;
	}

	public final IDSeparator getSeparator() {
		return this.separator;
	}

	public final Name getName() {
		return this.name;
	}

	public final SubID getSuffix() {
		return this.suffix;
	}

	public final ID getLocal() {
		return this.local;
	}

	public final ID setLocal(String local) {
		assert local != null :
			"Local not specified";
		return setLocal(CASE_SENSITIVE.name(local));
	}

	public final ID setLocal(Name local) {
		assert local != null :
			"Local not specified";
		return setLocal(new ID(null, IDSeparator.NONE, local, null));
	}

	public final ID setLocal(ID local) {
		assert local != null :
			"Local not specified";

		final ID result = sub(local);

		if (result == local) {
			return local.removeLocal();
		}

		result.local = local;

		return result;
	}

	public final ID removeLocal() {
		if (this.local == this) {
			return this;
		}
		return new ID(getPrefix(), getSeparator(), getName(), getSuffix());
	}

	public final ID sub(String name) {
		assert name != null :
			"Identifier not specified";
		return separate(IDSeparator.SUB, name);
	}

	public final ID sub(Name name) {
		assert name != null :
			"Identifier not specified";
		return separate(IDSeparator.SUB, name);
	}

	public final ID sub(SubID id) {
		assert id != null :
			"Identifier not specified";
		return separate(IDSeparator.SUB, id);
	}

	public final ID sub(ID id) {
		assert id != null :
			"Identifier not specified";
		return separate(IDSeparator.SUB, id);
	}

	public final ID suffix(String suffix) {
		assert suffix != null :
			"Suffix not specified";
		return separate(IDSeparator.NONE, suffix);
	}

	public final ID suffix(Name suffix) {
		assert suffix != null :
			"Suffix not specified";
		return separate(IDSeparator.NONE, suffix);
	}

	public final ID suffix(SubID suffix) {
		assert suffix != null :
			"Suffix not specified";
		return separate(IDSeparator.NONE, suffix);
	}

	public final ID suffix(ID suffix) {
		assert suffix != null :
			"Suffix not specified";
		return separate(IDSeparator.NONE, suffix);
	}

	public final ID anonymous(int index) {
		return separate(IDSeparator.ANONYMOUS, Integer.toString(index));
	}

	public final ID anonymous(String name) {
		return separate(IDSeparator.ANONYMOUS, name);
	}

	public final ID anonymous(Name name) {
		return separate(IDSeparator.ANONYMOUS, name);
	}

	public final ID detail(String detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(IDSeparator.DETAIL, detail);
	}

	public final ID detail(Name detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(IDSeparator.DETAIL, detail);
	}

	public final ID detail(SubID detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(IDSeparator.DETAIL, detail);
	}

	public final ID detail(ID detail) {
		assert detail != null :
			"Identifier not specified";
		return separate(IDSeparator.DETAIL, detail);
	}

	public final ID type(String type) {
		assert type != null :
			"Identifier not specified";
		return separate(IDSeparator.TYPE, type);
	}

	public final ID type(Name type) {
		assert type != null :
			"Identifier not specified";
		return separate(IDSeparator.TYPE, type);
	}

	public final ID type(SubID type) {
		assert type != null :
			"Identifier not specified";
		return separate(IDSeparator.TYPE, type);
	}

	public final ID type(ID type) {
		assert type != null :
			"Identifier not specified";
		return separate(IDSeparator.TYPE, type);
	}

	public final ID in(SubID in) {
		assert in != null :
			"Identifier not specified";
		return separate(IDSeparator.IN, in);
	}

	public final ID in(ID in) {
		assert in != null :
			"Identifier not specified";
		return separate(IDSeparator.IN, in);
	}

	public final SubID setDescription(String description) {
		assert description != null :
			"Description not specified";
		return new DescriptionOfID(this, description);
	}

	@Override
	public final ID toID() {
		return this;
	}

	@Override
	public final ID toDisplayID() {
		return this;
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return DISPLAY_NAME_ENCODER.print(this);
	}

	private final ID separate(IDSeparator separator, String name) {
		return separate(separator, CASE_SENSITIVE.name(name));
	}

	private ID separate(IDSeparator separator, Name name) {
		if (getSuffix() == null && getName().isEmpty()) {
			if (getPrefix() == null
					&& getSeparator().isNone()
					&& !separator.isTop()) {
				return new ID(null, IDSeparator.NONE, name, null);
			}
			if (separator.discardsPrev(getSeparator())) {
				return new ID(getPrefix(), separator, name, null);
			}
			if (getSeparator().discardsNext(separator)) {
				return new ID(getPrefix(), getSeparator(), name, null);
			}
		}
		return new ID(this, separator, name, null);
	}

	private ID separate(IDSeparator separator, SubID subID) {
		if (getSuffix() == null && getName().isEmpty()) {
			if (getPrefix() == null
					&& getSeparator().isNone()
					&& !separator.isTop()) {
				return new ID(null, IDSeparator.NONE, NO_NAME, subID);
			}
			if (separator.discardsPrev(getSeparator())) {
				return new ID(getPrefix(), separator, NO_NAME, subID);
			}
			if (getSeparator().discardsNext(separator)) {
				return new ID(getPrefix(), getSeparator(), NO_NAME, subID);
			}
		}
		return new ID(this, separator, NO_NAME, subID);
	}

	private ID separate(IDSeparator separator, ID id) {
		if (getSuffix() == null && getName().isEmpty()) {
			if (id.startsWith().discardsPrev(separator)) {
				if (getPrefix() == null) {
					return id;
				}
				return new ID(getPrefix(), separator, NO_NAME, id);
			}
		}
		return new ID(this, separator, NO_NAME, id);
	}

	private final IDSeparator startsWith() {

		ID id = this;

		for (;;) {

			final ID prefix = id.getPrefix();

			if (prefix == null) {
				return id.getSeparator();
			}

			id = prefix;
		}
	}

}
