/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.compiler.ip.module;

import static org.o42a.util.StringCodec.canonicalName;

import org.o42a.util.io.Source;


public final class SourceFileName {

	private static final String FILE_SUFFIX = ".o42a";
	private static final String[] NO_PARTS = new String[0];

	private final Source source;
	private final String fileName;
	private final String name;
	private final String[] adaptee;
	private final String[] declaredIn;
	private final String comment;
	private final boolean valid;

	SourceFileName(Source source) {
		this.source = source;
		this.fileName = fileName(source);

		final String meaningful;
		final int commentIdx = this.fileName.indexOf("__");

		if (commentIdx < 0) {
			meaningful = this.fileName;
			this.comment = null;
		} else {
			meaningful = this.fileName.substring(0, commentIdx);
			this.comment = this.fileName.substring(commentIdx + 2);
		}

		if (meaningful.isEmpty()) {
			this.name = null;
			this.adaptee = NO_PARTS;
			this.declaredIn = NO_PARTS;
			this.valid = false;
			return;
		}

		boolean valid = true;
		final String main;

		if (meaningful.length() > 1) {

			final String declaredInStr;
			final int declaredInIdx = meaningful.indexOf('@', 1);

			if (declaredInIdx > 0) {
				main = meaningful.substring(0, declaredInIdx);
				declaredInStr = meaningful.substring(declaredInIdx + 1);
			} else {
				main = meaningful;
				declaredInStr = null;
			}

			final String[] declaredIn = split(declaredInStr);

			valid = canonical(declaredIn) & valid;
			this.declaredIn = declaredIn;
		} else {
			main = meaningful;
			this.declaredIn = NO_PARTS;
		}

		if (meaningful.startsWith("@")) {

			final String adapteeStr = main.substring(1);
			final String[] adaptee = split(adapteeStr);

			valid = canonical(adaptee) & valid;
			this.name = null;
			this.adaptee = adaptee;
		} else {

			final StringBuilder name = new StringBuilder(main.length());

			valid = canonicalName(main, name) & valid;

			this.name = name.toString();
			this.adaptee = NO_PARTS;
		}

		this.valid = valid;
	}

	public final Source getSource() {
		return this.source;
	}

	public final String getFileName() {
		return this.fileName;
	}

	public final String getName() {
		return this.name;
	}

	public final String[] getAdaptee() {
		return this.adaptee;
	}

	public final String[] getDeclaredIn() {
		return this.declaredIn;
	}

	public final String getComment() {
		return this.comment;
	}

	public final boolean isValid() {
		return this.valid;
	}

	@Override
	public String toString() {
		if (this.fileName == null) {
			return super.toString();
		}
		return this.fileName;
	}

	private static String fileName(Source source) {

		final String fileName = source.getFileName();

		if (!fileName.endsWith(FILE_SUFFIX)) {
			return fileName;
		}

		return fileName.substring(
					0,
					fileName.length() - FILE_SUFFIX.length());
	}

	private static String[] split(String name) {
		return name.split("\\.");
	}

	private static boolean canonical(String[] parts) {
		if (parts == null) {
			return true;
		}

		boolean valid = true;
		final StringBuilder out = new StringBuilder(parts[0].length());

		for (int i = 0; i < parts.length; ++i) {
			out.setLength(0);
			if (!canonicalName(parts[i], out)) {
				valid = false;
			} else if (out.length() == 0) {
				valid = false;
			}
			parts[i] = out.toString();
		}

		return valid;
	}

}
