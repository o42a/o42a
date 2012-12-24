/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
