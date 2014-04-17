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
import org.o42a.core.ir.cmd.ResumeCallback;
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
	private Control sentenceControl;
	private AltBlocks altBlocks;
	private Control altControl;
	private Control statementControl;
	private ResumeCallback prevResumeCallback;

	public SentenceIndex(Control control, Scope origin) {
		this.origin = origin;
		this.control = control;
	}

	public final Scope getOrigin() {
		return this.origin;
	}

	public Control startBlock(Sentences sentences) {

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
	}

	public AltBlocks startSentence(Control control, String prefix, int len) {
		this.altBlocks =
				new AltBlocks(control, ID.id(prefix + "_sent"), len);

		control.code().go(this.altBlocks.get(0).head());
		this.sentenceControl = control;

		return this.altBlocks;
	}

	public void endSentence() {
	}

	public Control startAlt(int index) {
		this.statementControl = null;

		final Block altCode = this.altBlocks.get(index);
		final int nextIdx = index + 1;

		if (nextIdx >= this.altBlocks.size()) {
			// last alternative
			return this.altControl = this.sentenceControl.alt(
					altCode,
					this.sentenceControl.falseDir());
		}

		return this.altControl =
				this.sentenceControl.alt(
						altCode,
						this.altBlocks.get(nextIdx).head());
	}

	public final void endAlt(Sentence sentence) {
		if (this.prevResumeCallback != null) {
			startStatement("resume");
			endStatement();
		}
		if (this.statementControl != this.altControl) {
			this.statementControl.end();
		}
		this.altControl.end();
		if (sentence.getKind().isExclamatory()) {
			this.altControl.exitBraces();
		} else {
			// Interrogative condition satisfied or sentence successfully
			// completed. Go to the next sentence.
			this.altControl.code().go(this.sentenceControl.code().tail());
		}
	}

	public Control startStatement(String index) {
		if (this.prevResumeCallback == null) {
			if (this.statementControl == null) {
				this.statementControl = this.altControl;
			}
			return this.statementControl;
		}
		assert this.statementControl == null :
			"Statement control already created";
		return this.statementControl =
				this.altControl.resume(index, this.prevResumeCallback);
	}

	public void endStatement() {
		this.prevResumeCallback = this.statementControl.getResumeCallback();
		this.statementControl.setResumeCallback(null);
		if (this.prevResumeCallback == null) {
			return;
		}
		if (this.statementControl != this.altControl) {
			this.statementControl.end();
		}
		this.statementControl = null;
	}

}
