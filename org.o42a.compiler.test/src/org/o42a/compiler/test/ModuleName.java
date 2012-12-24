/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test;

import static java.lang.Character.*;
import static org.o42a.util.string.Name.caseInsensitiveName;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.o42a.util.string.Name;


final class ModuleName extends TestWatcher {

	private Name moduleName;

	public final Name getModuleName() {
		return this.moduleName;
	}

	public final void setModuleName(Name moduleName) {
		this.moduleName = moduleName;
	}

	@Override
	protected void starting(Description description) {
		setModuleName(toModuleName(description.getMethodName()));
	}

	private static Name toModuleName(String name) {

		final int length = name.length();
		final StringBuilder moduleName = new StringBuilder(length);
		boolean prevLower = false;

		for (int i = 0; i < length;) {

			final int c = name.codePointAt(i);

			i += charCount(c);

			if (!isUpperCase(c)) {
				prevLower = true;
				if (moduleName.length() == 0) {
					moduleName.appendCodePoint(toUpperCase(c));
				} else {
					moduleName.appendCodePoint(c);
				}
				continue;
			}
			if (prevLower) {
				moduleName.append(' ');
				prevLower = false;
			}
			moduleName.appendCodePoint(toLowerCase(c));
		}

		return caseInsensitiveName(moduleName.toString());
	}

}
