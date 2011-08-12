/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.member.AdapterId.adapterTypeScope;
import static org.o42a.core.ref.path.Path.absolutePath;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Distributor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.path.AbsolutePath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;


public enum ClauseId {

	NAME(false) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
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

	ARGUMENT(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
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

	IMPERATIVE(false) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
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

	STRING(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
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

	PLUS(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "plus");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "+_";
		}

	},

	MINUS(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "minus");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "-_";
		}

	},

	ADD(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "add");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "_+_";
		}

	},

	SUBTRACT(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "subtract");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "_-_";
		}

	},

	MULTIPLY(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "multiply");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "_*_";
		}

	},

	DIVIDE(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "divide");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "_/_";
		}

	},

	EQUALS(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "equals");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "==";
		}

	},

	COMPARE(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "compare");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "<compare>";
		}

	},

	OPERAND(true) {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "operators", "operand");
		}

		@Override
		public String toString(MemberId memberId, String name) {
			return "<operand>";
		}

	};

	private final boolean hasValue;

	ClauseId(boolean hasValue) {
		this.hasValue = hasValue;
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

		final AbsolutePath adapterPath = adapterPath(location.getContext());

		return adapterPath.target(location, distributor).toStaticTypeRef();
	}

	public abstract AbsolutePath adapterPath(CompilerContext context);

	public abstract String toString(MemberId memberId, String name);

	public static ClauseId byAdapterType(StaticTypeRef adapterType) {

		final Obj type = adapterTypeScope(
				adapterType.typeObject(dummyUser())).toObject();

		for (ClauseId clauseId : values()) {
			if (!clauseId.hasAdapterType()) {
				continue;
			}

			final AbsolutePath adapterPath =
					clauseId.adapterPath(adapterType.getContext());

			if (type == adapterPath.resolve(
					adapterType.getContext()).getArtifact().toObject()) {
				return clauseId;
			}
		}

		return NAME;
	}

}
