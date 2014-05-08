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
package org.o42a.core.member;

import static org.o42a.core.st.sentence.Local.ANONYMOUS_LOCAL_MEMBER;

import org.o42a.util.string.ID;
import org.o42a.util.string.SubID;


public enum MemberKind {

	TEMP() {

		@Override
		ID toID(MemberName memberName) {
			return TEMP_PREFIX_ID.suffix(memberName.getName());
		}

		@Override
		ID toDisplayID(MemberName memberName) {
			return memberName.getName().toID();
		}

	},

	FIELD() {

		@Override
		ID toID(MemberName memberName) {
			return memberName.getName().toID();
		}

		@Override
		ID toDisplayID(MemberName memberName) {
			return toID(memberName);
		}

	},

	CLAUSE_ID() {

		@Override
		ID toID(MemberName memberName) {
			return memberName.getName().toID();
		}

		@Override
		ID toDisplayID(MemberName memberName) {
			return toID(memberName);
		}

	},

	CLAUSE_NAME() {

		@Override
		ID toID(MemberName memberName) {
			return CLAUSE_PREFIX_ID.suffix(memberName.getName());
		}

		@Override
		ID toDisplayID(MemberName memberName) {
			return CLAUSE_DISPLAY_PREFIX
					.suffix(memberName.getName())
					.suffix(DISPLAY_SUFFIX);
		}

	},

	LOCAL() {

		@Override
		ID toID(MemberName memberName) {
			return LOCAL_PREFIX_ID.suffix(memberName.getName());
		}

		@Override
		ID toDisplayID(MemberName memberName) {
			if (memberName.equals(ANONYMOUS_LOCAL_MEMBER)) {
				return ANONYMOUS_LOCAL_DISPLAY_ID;
			}
			return LOCAL_DISPLAY_PREFIX
					.suffix(memberName.getName())
					.suffix(DISPLAY_SUFFIX);
		}

	},

	LOCAL_FIELD() {

		@Override
		ID toID(MemberName memberName) {
			return LOCAL_FIELD_PREFIX_ID.suffix(memberName.getName());
		}

		@Override
		ID toDisplayID(MemberName memberName) {
			return LOCAL_FIELD_DISPLAY_PREFIX
					.suffix(memberName.getName())
					.suffix(DISPLAY_SUFFIX);
		}

	},

	ALIAS() {

		@Override
		ID toID(MemberName memberName) {
			return ALIAS_PREFIX_ID.suffix(memberName.getName());
		}

		@Override
		ID toDisplayID(MemberName memberName) {
			return memberName.getName().toID();
		}

	};

	private static final ID TEMP_PREFIX_ID = ID.id("T");
	private static final ID CLAUSE_PREFIX_ID = ID.id("C");
	private static final ID LOCAL_PREFIX_ID = ID.id("L");
	private static final ID LOCAL_FIELD_PREFIX_ID = ID.id("LF");
	private static final ID ALIAS_PREFIX_ID = ID.id("A");

	private static final ID CLAUSE_DISPLAY_PREFIX =
			ID.id(ID.displayText("Clause `"));
	private static final ID ANONYMOUS_LOCAL_DISPLAY_ID =
			ID.id(ID.displayText("$"));
	private static final ID LOCAL_DISPLAY_PREFIX =
			ID.id(ID.displayText("Local `"));
	private static final ID LOCAL_FIELD_DISPLAY_PREFIX =
			ID.id(ID.displayText("Local field `"));
	private static final SubID DISPLAY_SUFFIX =
			ID.displayText("`");

	abstract ID toID(MemberName memberName);

	abstract ID toDisplayID(MemberName memberName);

}
