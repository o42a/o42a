/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.AdapterId.adapterTypeScope;
import static org.o42a.core.ref.path.Path.absolutePath;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberId;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;


public enum ClauseId {

	NAME("named", false) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return null;
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (memberId == null) {
				return "<anonymous>";
			}
			return memberId.toString();
		}

	},

	ARGUMENT("argument", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "clauses", "argument");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "[]";
			}
			return '[' + name + ']';
		}

	},

	ROW("row", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "clauses", "row");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "[[]]";
			}
			return "[[" + name + "]]";
		}

	},

	IMPERATIVE("imperative", false) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "clauses", "imperative");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "{}";
			}
			return '{' + name + '}';
		}

	},

	STRING("string", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "clauses", "string");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "''";
			}
			return '\'' + name + '\'';
		}

	},

	PLUS("unary plus operator", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "plus");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "+*";
			}
			return '+' + name;
		}

	},

	MINUS("unary minus operator", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "minus");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "-*";
			}
			return '-' + name;
		}

	},

	ADD("addition operator", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "add");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "* + *";
			}
			return name + " + *";
		}

	},

	SUBTRACT("subtraction operator", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "subtract");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "* - *";
			}
			return name + " - *";
		}

	},

	MULTIPLY("multiplication operator", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "multiply");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "* * *";
			}
			return name + " * *";
		}

	},

	DIVIDE("division operator", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "divide");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "* / *";
			}
			return name + " / *";
		}

	},

	EQUALS("equality check", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "equals");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "* == *";
			}
			return name + " == *";
		}

	},

	COMPARE("comparison", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "compare");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "* <compare> *";
			}
			return name + " <compare> *";
		}

	},

	ASSIGN("value assignment", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "assign");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "* = *";
			}
			return name + " = *";
		}

	},

	OPERAND("right operand", true) {

		@Override
		public Path adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "operand");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			if (name == null) {
				return "<operand>";
			}
			return "<operand>(" + name + ')';
		}

	};

	public static ClauseId byAdapterType(StaticTypeRef adapterType) {
		assert adapterType != null :
			"Clause adapter type not specified";

		final Obj type = adapterTypeScope(
				adapterType.typeObject(dummyUser())).toObject();

		for (ClauseId clauseId : values()) {
			if (!clauseId.hasAdapterType()) {
				continue;
			}

			final Scope start = adapterType.getContext().getRoot().getScope();
			final BoundPath adapterPath =
					clauseId.adapterPath(adapterType.getContext())
					.bind(adapterType, start);
			final PathResolution adapterResolution =
					adapterPath.resolve(pathResolver(start, dummyUser()));

			if (type == adapterResolution.getObject().toObject()) {
				return clauseId;
			}
		}

		return NAME;
	}

	private final String displayName;
	private final boolean hasValue;

	ClauseId(String displayName, boolean hasValue) {
		this.displayName = displayName;
		this.hasValue = hasValue;
	}

	public final String getDisplayName() {
		return this.displayName;
	}

	public final boolean hasValue() {
		return this.hasValue;
	}

	public final boolean hasAdapterType() {
		return this != NAME;
	}

	public final StaticTypeRef adapterType(
			LocationInfo location,
			Distributor distributor) {
		if (!hasAdapterType()) {
			return null;
		}
		return adapterPath(location.getContext())
				.bind(location, distributor.getScope())
				.staticTypeRef(distributor);
	}

	public abstract Path adapterPath(CompilerContext context);

	public void validateClause(Clause clause) {

		final PlainClause plainClause = clause.toPlainClause();

		if (plainClause != null && plainClause.isSubstitution()) {
			if (!hasValue()) {
				clause.getLogger().error(
						"prohibited_clause_substitution",
						clause,
						"Clause substitution is not allowed in %s clause",
						getDisplayName());
			}
		}
	}

	public abstract String toString(MemberId memberId, String name);

}
