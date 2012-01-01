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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.o42a.common.source.SourceTree;
import org.o42a.util.io.SourceFileName;


public class TestSourceTree extends SourceTree<TestSource> {

	private final ArrayList<TestSourceTree> subTrees;

	public TestSourceTree(TestSource source) {
		super(source, new SourceFileName(source.getShortName()));

		final HashMap<String, TestSource> subSources = source.getSubSources();

		this.subTrees = new ArrayList<TestSourceTree>(subSources.size());
		for (TestSource s : subSources.values()) {
			this.subTrees.add(new TestSourceTree(s));
		}
	}

	@Override
	public Iterator<? extends TestSourceTree> subTrees() {
		return this.subTrees.iterator();
	}

}
