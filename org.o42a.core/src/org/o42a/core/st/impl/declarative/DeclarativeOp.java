/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.st.impl.declarative;

import java.util.List;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.util.string.ID;


final class DeclarativeOp {

	public static void writeSentences(
			DefDirs dirs,
			HostOp host,
			Scope origin,
			DeclarativeSentences block,
			InlineDeclarativeSentences inline) {

		final List<DeclarativeSentence> sentences = block.getSentences();
		final int size = sentences.size();

		if (size == 0) {
			return;
		}

		for (int i = 0; i < size; ++i) {
			writeSentence(
					ID.id((i + 1) + "_sent"),
					dirs,
					host,
					origin,
					sentences.get(i),
					inline != null ? inline.get(i) : null);
		}
	}

	private static void writeSentence(
			ID prefix,
			DefDirs dirs,
			HostOp host,
			Scope origin,
			DeclarativeSentence sentence,
			InlineDeclarativeSentence inline) {

		final DeclarativeSentence prerequisite = sentence.getPrerequisite();
		final Block prereqFailed;

		if (prerequisite == null) {
			prereqFailed = null;
		} else {
			prereqFailed = dirs.addBlock(prefix.sub("prereq_failed"));

			final DefDirs prereqDirs = dirs.setFalseDir(prereqFailed.head());

			writeSentence(
					prefix.sub("prereq"),
					prereqDirs,
					host,
					origin,
					prerequisite,
					inline != null ? inline.getPrerequisite() : null);
		}

		final List<Declaratives> alts = sentence.getAlternatives();
		final int size = alts.size();

		if (size <= 1) {
			if (size != 0) {
				writeStatements(
						dirs,
						host,
						origin,
						alts.get(0),
						inline != null ? inline.get(0) : null);
			}
			endPrereq(dirs, prereqFailed);
			return;
		}

		final Block end = dirs.addBlock(prefix.sub("end"));
		Block code = dirs.code();
		int i = 0;

		for (;;) {

			final DefDirs altDirs;
			final Block next;
			final int nextIdx = i + 1;

			if (nextIdx < size) {
				next = dirs.addBlock(prefix.sub((nextIdx + 1) + "_alt"));
				altDirs = dirs.sub(code).setFalseDir(next.head());
			} else {
				next = null;
				altDirs = dirs.sub(code).setFalseDir(dirs.falseDir());
			}

			writeStatements(
					altDirs,
					host,
					origin,
					alts.get(i),
					inline != null ? inline.get(i) : null);

			altDirs.done();

			if (next == null) {
				if (code.exists()) {
					code.go(dirs.code().tail());
				}
				break;
			}
			if (code.exists()) {
				code.go(end.head());
			}
			i = nextIdx;
			code = next;
		}

		if (end.exists()) {
			end.go(dirs.code().tail());
		}
		endPrereq(dirs, prereqFailed);
	}

	private static void endPrereq(DefDirs dirs, Block prereqFailed) {
		if (prereqFailed != null && prereqFailed.exists()) {
			prereqFailed.go(dirs.code().tail());
		}
	}

	private static void writeStatements(
			DefDirs dirs,
			HostOp host,
			Scope origin,
			Declaratives statements,
			InlineDefiners inline) {
		throw new UnsupportedOperationException();

		/*final List<Definer> definers = statements.getImplications();
		final int size = definers.size();

		for (int i = 0; i < size; ++i) {
			if (inline != null) {

				final InlineEval inlineEval = inline.get(i);

				if (inlineEval != null) {
					inlineEval.write(dirs, host);
					continue;
				}
			}

			definers.get(i).eval(origin).write(dirs, host);
		}*/
	}

}
