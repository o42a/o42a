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
package org.o42a.core.member.clause;

import static org.o42a.core.member.MemberName.clauseId;

import org.o42a.core.member.MemberId;
import org.o42a.util.string.Name;


public enum ClauseId {

	NAME("named", false) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (memberId == null) {
				return "<anonymous>";
			}
			return memberId.toString();
		}

	},

	SUFFIX("suffix", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* ~ *";
			}
			return name.toString() + " ~ *";
		}

	},

	ARGUMENT("argument", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[]";
			}
			return '[' + name.toString() + ']';
		}

	},

	INITIALIZER("initializer", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[=*]";
			}
			return "[=" + name + ']';
		}

	},

	ROW("row", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[[]]";
			}
			return "[[" + name + "]]";
		}

	},

	IMPERATIVE("imperative", false) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "{}";
			}
			return '{' + name.toString() + '}';
		}

	},

	STRING("string", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "''";
			}
			return '\'' + name.toString() + '\'';
		}

	},

	OPEN_INTERVAL_START("open interval start", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(*...)";
			}
			return '(' + name.toString() + "...)";
		}

	},

	LEFT_OPEN_INTERVAL_START("left-open interval start", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(*...]";
			}
			return '(' + name.toString() + "...]";
		}

	},

	RIGHT_OPEN_INTERVAL_START("right-open interval start", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[*...)";
			}
			return '[' + name.toString() + "...)";
		}

	},

	CLOSED_INTERVAL_START("closed interval start", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[*...]";
			}
			return '[' + name.toString() + "...]";
		}

	},

	OPEN_INTERVAL_END("open interval end", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(...*)";
			}
			return "(..." + name + ')';
		}

	},

	LEFT_OPEN_INTERVAL_END("left-open interval end", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(...*]";
			}
			return "(..." + name + ']';
		}

	},

	RIGHT_OPEN_INTERVAL_END("right-open interval end", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[...*)";
			}
			return "[..." + name + ')';
		}

	},

	CLOSED_INTERVAL_END("closed interval end", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[...*]";
			}
			return "[..." + name + ']';
		}

	},

	LEFT_BOUNDED_OPEN_INTERVAL("left-bounded open interval", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(*...-)";
			}
			return "(" + name + "...-)";
		}

	},

	LEFT_BOUNDED_CLOSED_INTERVAL("left-bounded closed interval", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "[*...-)";
			}
			return "[" + name + "...-)";
		}

	},

	RIGHT_BOUNDED_OPEN_INTERVAL("right-bounded open interval", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(-...*)";
			}
			return "(-..." + name + ')';
		}

	},

	RIGHT_BOUNDED_CLOSED_INTERVAL("right-bounded closed interval", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(-...*]";
			}
			return "(-..." + name + ']';
		}

	},

	UNBOUNDED_INTERVAL("unbounded interval", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "(-...-)";
			}
			return "(-" + name + "...-)";
		}

	},

	PLUS("unary plus operator", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "+*";
			}
			return '+' + name.toString();
		}

	},

	MINUS("unary minus operator", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "-*";
			}
			return '-' + name.toString();
		}

	},

	ADD("addition operator", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* + *";
			}
			return name + " + *";
		}

	},

	SUBTRACT("subtraction operator", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* - *";
			}
			return name + " - *";
		}

	},

	MULTIPLY("multiplication operator", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* * *";
			}
			return name + " * *";
		}

	},

	DIVIDE("division operator", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* / *";
			}
			return name + " / *";
		}

	},

	EQUALS("equality check", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* == *";
			}
			return name + " == *";
		}

	},

	COMPARE("comparison", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* <=> *";
			}
			return name + " <=> *";
		}

	},

	ASSIGN("value assignment", true) {

		@Override
		public String toString(MemberId memberId, Name name) {
			if (name == null) {
				return "* = *";
			}
			return name + " = *";
		}

	};

	private final String displayName;
	private final boolean hasValue;
	private final MemberId memberId;

	ClauseId(String displayName, boolean hasValue) {
		this.displayName = displayName;
		this.hasValue = hasValue;
		if (isName()) {
			this.memberId = null;
		} else {
			this.memberId = clauseId(this);
		}
	}

	public final boolean hasValue() {
		return this.hasValue;
	}

	public final boolean isName() {
		return this == NAME;
	}

	public final MemberId getMemberId() {
		return this.memberId;
	}

	public final String getDisplayName() {
		return this.displayName;
	}

	public void validateClause(Clause clause) {
		if (clause.getSubstitution().requiresValue() && !hasValue()) {
			clause.getLogger().error(
					"prohibited_value_substitution_clause",
					clause,
					"Value substitution is not allowed in %s clause",
					getDisplayName());
		}
	}

	public abstract String toString(MemberId memberId, Name name);

}
