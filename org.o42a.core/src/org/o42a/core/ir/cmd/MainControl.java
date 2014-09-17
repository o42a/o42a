/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ir.cmd;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.CodePtr;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.DefStore;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID;


final class MainControl extends Control {

	private final DefDirs dirs;
	private final Block continuation;

	private int seq;
	private Block returnCode;
	private ResumePosition[] resumePositions;
	private Block resume;
	private CodePos start;

	MainControl(DefDirs dirs, Block continuation) {
		this.dirs = dirs;
		this.continuation = continuation;
	}

	@Override
	public final LocalsCode locals() {
		return builder().locals();
	}

	@Override
	public final Block code() {
		return this.dirs.code();
	}

	@Override
	public final CodePos exit() {
		return this.continuation.head();
	}

	@Override
	public final CodePos falseDir() {
		return this.dirs.falseDir();
	}

	public final ValOp finalResult() {
		return this.dirs.result();
	}

	final CodeBuilder builder() {
		return this.dirs.getBuilder();
	}

	final DefStore mainStore() {
		return this.dirs.store();
	}

	@Override
	final MainControl main() {
		return this;
	}

	@Override
	final BracesControl braces() {
		return null;
	}

	@Override
	CodePos returnDir() {
		if (this.returnCode == null) {
			this.returnCode = code().addBlock("return");
		}
		return this.returnCode.head();
	}

	void init() {
		this.resume = code().addBlock("resume");
		code().go(this.resume.head());
		this.start = code().tail();
	}

	final ID anonymousName() {
		return ID.id(Integer.toString(++this.seq));
	}

	void addResumePosition(CodePos resumePos, ResumeCallback callback) {

		final ResumePosition position =
				new ResumePosition(resumePos, callback);

		if (this.resumePositions == null) {
			this.resumePositions = new ResumePosition[] {position};
		} else {
			this.resumePositions =
					ArrayUtil.append(this.resumePositions, position);
		}
	}

	@Override
	Control done() {
		resume();
		if (this.continuation.exists()) {
			this.continuation.go(code().tail());
		}
		if (this.returnCode != null) {
			this.dirs.returnValue(this.returnCode, finalResult());
		}
		return this;
	}

	private void resume() {
		if (this.resumePositions == null) {
			this.resume.go(this.start);
			return;
		}

		final CodeDirs resumeDirs =
				getBuilder().dirs(this.resume, falseDir());
		final AnyOp resumeFrom =
				host().resumeFrom(resumeDirs)
				.ptr(this.resume)
				.resumePtr(this.resume)
				.load(null, this.resume);

		resumeFrom.isNull(null, this.resume).go(this.resume, this.start);

		final CodePos[] targets = new CodePos[this.resumePositions.length];

		for (int i = 0; i < targets.length; ++i) {
			targets[i] = this.resumePositions[i].resumeFrom;
		}

		this.resume.debug("Resuming");

		final CodePos[] heads = this.resume.go(
				resumeFrom.toCode(null, this.resume),
				targets);

		for (int i = 0; i < heads.length; ++i) {

			final CodePos head = heads[i];
			final CodePtr ptr = head.code().ptr();

			assert ptr.is(head) :
				"Not a block head: " + head;

			this.resumePositions[i].callback.resumedAt(ptr);
		}
	}

	private static final class ResumePosition {

		private final CodePos resumeFrom;
		private final ResumeCallback callback;

		ResumePosition(CodePos resumeFrom, ResumeCallback callback) {
			this.resumeFrom = resumeFrom;
			this.callback = callback;
		}

	}

}
