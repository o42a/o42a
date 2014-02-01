/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.User;
import org.o42a.analysis.use.UserInfo;
import org.o42a.backend.constant.code.op.AbstractBE;


public abstract class TermBE extends AbstractBE {

	private final CBlockPart part;

	TermBE(CBlockPart part) {
		this.part = part.terminate(this);
	}

	@Override
	public final CBlockPart part() {
		return this.part;
	}

	public abstract JumpBE toJump();

	@Override
	public abstract void prepare();

	@Override
	public final User<SimpleUsage> toUser() {
		return getAnalyzer().toUseCase();
	}

	@Override
	public void useBy(UserInfo user) {
	}

}
