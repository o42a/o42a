/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.backend.constant.code.op;

import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.SimpleUsage.simpleUsable;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.backend.constant.ConstGenerator;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.data.ConstBackend;


public abstract class AbstractBE implements UserInfo {

	private final Usable<SimpleUsage> uses;

	public AbstractBE() {
		this.uses = simpleUsable(this);
	}

	private final ConstBackend getBackend() {
		return part().code().getBackend();
	}

	public final ConstGenerator getGenerator() {
		return getBackend().getGenerator();
	}

	public final Analyzer getAnalyzer() {
		return getGenerator().getAnalyzer();
	}

	public abstract CCodePart<?> part();

	public abstract void prepare();

	public final void reveal() {
		if (this.uses.isUsed(getAnalyzer(), ALL_SIMPLE_USAGES)) {
			emit();
		}
	}

	@Override
	public final User<?> toUser() {
		return this.uses.toUser();
	}

	public final void alwaysEmit() {
		useBy(getAnalyzer());
	}

	public final void useBy(UserInfo user) {
		this.uses.useBy(user, SIMPLE_USAGE);
	}

	public final <BE extends AbstractBE> BE use(BE backend) {
		backend.useBy(this);
		return backend;
	}

	public final <CO extends COp<?, ?>> CO use(CO op) {
		use(op.backend());
		return op;
	}

	protected abstract void emit();

}
