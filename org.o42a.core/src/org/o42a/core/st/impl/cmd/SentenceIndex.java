/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.st.impl.cmd;

import static org.o42a.util.string.Capitalization.AS_IS;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


final class SentenceIndex {

	private static final Name BLK_SUFFIX = AS_IS.name("_blk");
	private static final Name NEXT_SUFFIX = AS_IS.name("_next");

	private final Scope origin;
	private final Control control;
	private Block blockCode;
	private Block afterBlock;
	private Control blockControl;
	private int sentence;
	private boolean prereqWritten;
	private AltBlocks altBlocks;
	private int alt;
	private Control altControl;
	private int statement;

	public SentenceIndex(Control control, Scope origin) {
		this.origin = origin;
		this.control = control;
	}

	public final Scope getOrigin() {
		return this.origin;
	}

	public Control startBlock(Sentences sentences) {
		if (this.blockControl != null) {
			return this.blockControl;
		}

		final ID name =
				this.control.name(sentences.getName()).suffix(BLK_SUFFIX);
		this.blockCode = this.control.addBlock(name);
		this.afterBlock = this.control.addBlock(name.suffix(NEXT_SUFFIX));

		if (sentences.isParentheses()) {
			return this.blockControl = this.control.parentheses(
					this.blockCode,
					this.afterBlock.head());
		}

		return this.blockControl = this.control.braces(
				this.blockCode,
				this.afterBlock.head(),
				sentences.getName());
	}

	public void endBlock() {
		this.blockControl.end();
		if (this.blockCode.created()) {
			this.control.code().go(this.blockCode.head());
			if (this.afterBlock.exists()) {
				this.afterBlock.go(this.control.code().tail());
			}
			if (this.blockCode.exists()) {
				this.blockCode.go(this.control.code().tail());
			}
		}

		this.blockControl = null;
		this.afterBlock = null;
	}

	public AltBlocks startSentence(Control control, String prefix, int len) {
		if (this.altBlocks != null) {
			return this.altBlocks;
		}

		this.altBlocks =
				new AltBlocks(control, ID.id(prefix + "_sent"), len);

		control.code().go(this.altBlocks.get(0).head());

		return this.altBlocks;
	}

	public void endSentence() {
		this.altBlocks = null;
	}

	public Control startAlt(Control control) {
		if (this.altControl != null) {
			return this.altControl;
		}

		final int altIdx = getAlt();
		final Block altCode = this.altBlocks.get(altIdx);
		final int nextIdx = altIdx + 1;

		if (nextIdx >= this.altBlocks.size()) {
			// last alternative
			return this.altControl = control.alt(altCode, control.exit());
		}

		return this.altControl =
				control.alt(altCode, this.altBlocks.get(nextIdx).head());
	}

	public final void endAlt(Sentence sentence, Control control) {
		this.altControl.end();
		endAlt(sentence, this.altControl, control);
		this.altControl = null;
	}

	public final void endSingleAlt(Sentence sentence, Control control) {
		assert this.altControl == null :
			"Not a single alternative";
		endAlt(sentence, control, control);
	}

	private void endAlt(
			Sentence sentence,
			Control altControl,
			Control control) {
		if (sentence.getKind().isExclamatory()) {
			altControl.exitBraces();
		} else {
			// Interrogative condition satisfied or sentence successfully
			// completed. Go to the next sentence.
			if (altControl.code() != control.code()) {
				altControl.code().go(control.code().tail());
			}
		}
	}

	public final int getSentence() {
		return this.sentence;
	}

	public final void nextSentence() {
		assert this.altBlocks == null && this.altControl == null:
			"Can not start next sentence";
		++this.sentence;
		this.prereqWritten = false;
		this.alt = 0;
		this.statement = 0;
	}

	public final boolean isPrereqWritten() {
		return this.prereqWritten;
	}

	public final void writePrereq() {
		this.prereqWritten = true;
	}

	public final int getAlt() {
		return this.alt;
	}

	public final void nextAlt() {
		assert this.altControl == null :
			"Can not start next alternative";
		++this.alt;
		this.statement = 0;
	}

	public final int getStatement() {
		return this.statement;
	}

	public final void nextStatement() {
		++this.statement;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("SentenceIndex[sentence=");
		out.append(this.sentence);
		if (this.prereqWritten) {
			out.append(", prereq written");
		}
		out.append(", alt=");
		out.append(this.alt);
		out.append(", statement=");
		out.append(this.statement);
		out.append("]");

		return out.toString();
	}

}
