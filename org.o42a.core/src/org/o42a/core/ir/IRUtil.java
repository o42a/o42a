/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.ir;

import static java.lang.Character.*;

import org.o42a.core.Scope;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.Clause;


public class IRUtil {

	public static String canonicalName(String name) {

		final int len = name.length();
		final StringBuilder result = new StringBuilder(len);
		boolean prevDigit = false;
		boolean prevLetter = false;
		boolean prevSeparator = false;

		for (int i = 0; i < len; ++i) {

			final char c = name.charAt(i);

			if (isWhitespace(c) || isISOControl(c) || c == '_') {
				if (prevSeparator) {
					continue;
				}
				if (result.length() == 0) {
					continue;
				}
				prevSeparator = true;
				continue;
			}
			if (isDigit(c)) {
				if (prevSeparator) {
					if (prevDigit) {
						result.append('_');
					}
					prevSeparator = false;
				}
				prevDigit = true;
				prevLetter = false;
				result.append(c);
				continue;
			}
			if (isLetter(c)) {
				if (prevSeparator) {
					if (prevLetter) {
						result.append('_');
					}
					prevSeparator = false;
				}
				prevDigit = false;
				prevLetter = true;
				result.append(toLowerCase(c));
				continue;
			}
			prevDigit = false;
			prevLetter = false;
			prevSeparator = false;
			result.append(toLowerCase(c));
		}

		return result.toString();
	}

	public static String encodeName(String name) {

		final int len = name.length();
		final StringBuilder result = new StringBuilder(len);

		for (int i = 0; i < len; ++i) {

			final char c = name.charAt(i);

			if ((c >= '0' && c <= '9')
					|| (c >= 'a' && c <= 'z')
					|| c == '_') {
				result.append(c);
			} else if (c == '-') {
				result.append("__");
			} else {
				printHex(result, c);
			}
		}

		return result.toString();
	}

	public static String encodeMemberId(
			IRGenerator generator,
			ScopeIR enclosingIR,
			Member member) {

		final StringBuilder out = new StringBuilder();
		final MemberId id = member.getId();

		addMemberId(generator, out, enclosingIR, id);
		if (member.isOverride()) {
			addDeclaredIn(generator, out, member.getKey().getOrigin());
		}
		for (Scope reproducedFrom : id.getReproducedFrom()) {
			addDeclaredIn(generator, out, reproducedFrom);
		}

		return out.toString();
	}

	private static void printHex(StringBuilder result, int c) {
		if (c == 0) {
			result.append(hex(0));
			return;
		}
		for (;;) {

			final int s = c & 0xF000;

			c = (c << 4) & 0xFFFF;
			if (s != 0) {
				result.append(hex(s >>> 12));
				break;
			}
		}
		while (c != 0) {

			final int s = c & 0xF000;

			result.append(hex(s >>> 12));
			c = (c << 4) & 0xFFFF;
		}
	}

	private static final char hex(int symbol) {
		return (char) ('A' + symbol);
	}

	private static void addMemberId(
			IRGenerator generator,
			StringBuilder out,
			MemberId id) {
		if (id.toName() != null) {
			out.append(IRSymbolSeparator.SUB);
		}
		addMemberId(generator, out, null, id);
	}

	private static void addMemberId(
			IRGenerator generator,
			StringBuilder out,
			ScopeIR enclosingIR,
			MemberId id) {

		final String name = id.toName();

		if (name != null) {

			final String encodedName = encodeName(name);

			if (enclosingIR != null) {
				out.append(enclosingIR.prefix(
						IRSymbolSeparator.SUB,
						encodedName));
			} else {
				out.append(encodedName);
			}

			return;
		}

		final AdapterId adapterId = id.toAdapterId();

		if (adapterId != null) {

			final ScopeIR adapterTypeIR =
				adapterId.getAdapterTypeScope().ir(generator);

			if (enclosingIR != null) {
				out.append(enclosingIR.prefix(
						IRSymbolSeparator.TYPE,
						adapterTypeIR.getId()));
			} else {
				out.append(IRSymbolSeparator.TYPE);
				out.append(adapterTypeIR.getId());
			}

			return;
		}

		final MemberId[] ids = id.toIds();

		if (ids != null) {
			addMemberId(
					generator,
					out,
					enclosingIR,
					ids[0]);
			for (int i = 1; i < ids.length; ++i) {
				addMemberId(generator, out, ids[i]);
			}

			return;
		}

		throw new IllegalStateException(
				"Can not generate IR identifier for " + id);
	}

	private static void addDeclaredIn(
			IRGenerator generator,
			StringBuilder out,
			Scope scope) {

		final Clause clause = scope.getContainer().toClause();

		if (clause == null) {
			out.append(IRSymbolSeparator.IN);
			out.append(scope.ir(generator).getId());
			return;
		}

		addDeclaredIn(generator, out, clause.getEnclosingScope());
		addMemberId(generator, out, clause.getKey().getMemberId());
	}

	private IRUtil() {
	}

}
