/*
    Compiler Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.test;

import java.util.HashMap;

import org.o42a.util.io.StringSource;


public class TestSource extends StringSource {

	private final String shortName;
	private final HashMap<String, TestSource> subSources;

	public TestSource(CompilerTestCase test) {
		this(test.getModuleName(), test.getModuleName(), "");
	}

	public TestSource(
			CompilerTestCase test,
			String string,
			TestSource subSources) {
		super(test.getModuleName(), string);
		this.shortName = test.getModuleName();
		this.subSources = subSources.getSubSources();
	}

	private TestSource(String shortName, String name, String string) {
		super(name, string);
		this.shortName = shortName;
		this.subSources = new HashMap<String, TestSource>();
	}

	public String getShortName() {
		return this.shortName;
	}

	public final HashMap<String, TestSource> getSubSources() {
		return this.subSources;
	}

	public final void addSource(String path, String string) {
		addSource(path, path, string);
	}

	private void addSource(String shortName, String name, String string) {

		final int idx = shortName.indexOf('/');

		if (idx < 0) {
			this.subSources.put(name, new TestSource(shortName, name, string));
			return;
		}

		final String src = shortName.substring(0, idx);
		final TestSource subSources = this.subSources.get(src);

		if (subSources == null) {
			throw new IllegalStateException(src + " not found in " + this);
		}

		subSources.addSource(shortName.substring(idx + 1), name, string);
	}

}
