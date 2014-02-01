/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.UnderlyingBackend;
import org.o42a.codegen.Generator;
import org.o42a.codegen.ProxyGenerator;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public final class ConstGenerator extends ProxyGenerator {

	private final ConstBackend backend;

	public ConstGenerator(Generator proxiedGenerator) {
		super(proxiedGenerator);
		this.backend = new ConstBackend(this, new Underlying());
	}

	@Override
	public void write() {
		getDebug().write();

		final Generator underlyingGenerator =
				this.backend.getUnderlyingGenerator();

		for (;;) {

			final boolean hadGlobals = getGlobals().write();

			if (hadGlobals) {
				underlyingGenerator.getGlobals().write();
			}

			final boolean hadFunctions = getFunctions().write();

			if (hadFunctions) {
				underlyingGenerator.getFunctions().write();
				continue;
			}
			if (!hadGlobals) {
				break;
			}
		}

		underlyingGenerator.write();
	}

	@Override
	public void close() {
		this.backend.close();
	}

	@Override
	protected final CodeBackend codeBackend() {
		return this.backend.codeBackend();
	}

	@Override
	protected final DataAllocator dataAllocator() {
		return this.backend.dataAllocator();
	}

	@Override
	protected final DataWriter dataWriter() {
		return this.backend.dataWriter();
	}

	private final class Underlying implements UnderlyingBackend {

		@Override
		public CodeBackend codeBackend() {
			return proxiedCodeBackend();
		}

		@Override
		public DataAllocator dataAllocator() {
			return proxiedDataAllocator();
		}

		@Override
		public DataWriter dataWriter() {
			return proxiedDataWriter();
		}

	}

}
