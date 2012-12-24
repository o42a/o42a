/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test;

import java.util.HashMap;

import org.o42a.util.io.StringSource;


public class TestSource extends StringSource {

	private final String shortName;
	private final HashMap<String, TestSource> subSources;

	public TestSource(CompilerTestCase test) {
		this(
				test.getModuleName().toString(),
				test.getModuleName().toString(),
				"");
	}

	public TestSource(
			CompilerTestCase test,
			String string,
			TestSource subSources) {
		super(test.getModuleName().toString(), string);
		this.shortName = test.getModuleName().toString();
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
			this.subSources.put(
					shortName,
					new TestSource(shortName, name, string));
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
