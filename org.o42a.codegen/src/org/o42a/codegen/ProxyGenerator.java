/*
    Compiler Code Generator
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
package org.o42a.codegen;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.debug.Debug;
import org.o42a.util.string.NameEncoder;


public abstract class ProxyGenerator extends Generator {

	private final Generator proxiedGenerator;
	private final Debug debug;

	public ProxyGenerator(Generator proxiedGenerator) {
		this.proxiedGenerator = proxiedGenerator;
		proxiedGenerator.proxied();
		this.debug = new Debug(this);
	}

	public final Generator getProxiedGenerator() {
		return this.proxiedGenerator;
	}

	@Override
	public final Analyzer getAnalyzer() {
		return getProxiedGenerator().getAnalyzer();
	}

	@Override
	public final Debug getDebug() {
		return this.debug;
	}

	@Override
	public final NameEncoder nameEncoder() {
		return getProxiedGenerator().nameEncoder();
	}

	protected final CodeBackend proxiedCodeBackend() {
		return getProxiedGenerator().codeBackend();
	}

	protected final DataAllocator proxiedDataAllocator() {
		return getProxiedGenerator().dataAllocator();
	}

	protected final DataWriter proxiedDataWriter() {
		return getProxiedGenerator().dataWriter();
	}

}
