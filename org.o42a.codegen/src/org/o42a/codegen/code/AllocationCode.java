/*
    Compiler Code Generator
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.codegen.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.backend.MultiCodePos;
import org.o42a.util.ArrayUtil;


public final class AllocationCode extends Code {

	private final Code enclosing;
	private Code destruction;
	private AllocationWriter writer;
	private Code alts[];

	AllocationCode(Code enclosing, CodeId name) {
		super(enclosing, name != null ? name : enclosing.id().detail("alloc"));
		this.enclosing = enclosing;
		enclosing.go(head());
	}

	public final Code getEnclosing() {
		return this.enclosing;
	}

	@Override
	public final boolean exists() {
		return true;
	}

	public final Code alt(String name) {
		return alt(getEnclosing().addBlock(name));
	}

	public final Code alt(CodeId name) {
		return alt(getEnclosing().addBlock(name));
	}

	public final Code destruction() {
		assert assertIncomplete();
		if (this.destruction != null) {
			return this.destruction;
		}
		setDestruction(getEnclosing().addBlock(id().detail("destruct")));
		return this.destruction;
	}

	@Override
	public void done() {
		if (isComplete()) {
			return;
		}

		if (this.alts != null) {

			final CodeWriter alts[] = new CodeWriter[1 + this.alts.length];
			int i = 0;

			alts[0] = writer();
			for (Code alt : this.alts) {
				alts[++i] = alt.writer();
			}

			final Code destruct;
			final MultiCodePos target;

			if (this.destruction == null) {
				destruct = addBlock(id().detail("destruct"));
				target = destruct.writer().comeFrom(alts);
				setDestruction(destruct);
			} else {
				destruct = addBlock(id().detail("pre-destruct"));
				target = destruct.writer().comeFrom(alts);
				destruct.go(this.destruction.head());
			}
			this.destruction.writer().goToOneOf(target);
		} else if (this.destruction != null) {
			go(this.destruction.head());
			this.destruction.go(getEnclosing().tail());
		} else {
			writer().dispose(writer());
			go(getEnclosing().tail());
		}

		super.done();
	}

	@Override
	public final AllocationWriter writer() {
		if (this.writer != null) {
			return this.writer;
		}
		return this.writer =
			getEnclosing().writer().allocationBlock(this, getId());
	}

	private Code alt(Code alt) {
		if (this.alts == null) {
			this.alts = new Code[] {alt};
		} else {
			this.alts = ArrayUtil.append(this.alts, alt);
		}
		return alt;
	}

	private void setDestruction(Code destruction) {
		this.destruction = destruction;
		writer().dispose(this.destruction.writer());
	}

}
