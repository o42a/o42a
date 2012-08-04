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
