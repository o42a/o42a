/*
    Utilities
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
package org.o42a.util.io;

import static org.o42a.util.string.Name.caseInsensitiveName;

import org.o42a.util.string.Name;


public final class SourceFileName {

	public static final String FILE_SUFFIX = ".o42a";

	private static final Name[] NO_PARTS = new Name[0];

	private final String fileName;
	private final String name;
	private final String extension;
	private final Name fieldName;
	private final Name[] adapterId;
	private final Name[] declaredIn;
	private final String comment;
	private final boolean valid;

	public SourceFileName(String fileName) {
		this.fileName = fileName;
		if (!fileName.endsWith(FILE_SUFFIX)) {
			this.name = fileName;
			this.extension = null;
		} else {
			this.name = fileName.substring(
					0,
					fileName.length() - FILE_SUFFIX.length());
			this.extension = "o42a";
		}

		final String meaningful;
		final int commentIdx = this.name.indexOf("__");

		if (commentIdx < 0) {
			meaningful = this.name;
			this.comment = null;
		} else {
			meaningful = this.name.substring(0, commentIdx);
			this.comment = this.name.substring(commentIdx + 2);
		}

		if (meaningful.isEmpty()) {
			this.fieldName = null;
			this.adapterId = NO_PARTS;
			this.declaredIn = NO_PARTS;
			this.valid = false;
			return;
		}

		boolean valid = true;
		final String main;

		if (meaningful.length() > 1) {

			final Name[] declaredIn;
			final int declaredInIdx = meaningful.indexOf('@', 1);

			if (declaredInIdx > 0) {
				main = meaningful.substring(0, declaredInIdx);
				declaredIn = split(meaningful.substring(declaredInIdx + 1));
			} else {
				main = meaningful;
				declaredIn = NO_PARTS;
			}

			valid = validNames(declaredIn) & valid;
			this.declaredIn = declaredIn;
		} else {
			main = meaningful;
			this.declaredIn = NO_PARTS;
		}

		if (meaningful.startsWith("@")) {

			final String adapteeStr = main.substring(1);
			final Name[] adaptee = split(adapteeStr);

			valid = validNames(adaptee) & valid;
			this.fieldName = null;
			this.adapterId = adaptee;
		} else {
			this.fieldName = caseInsensitiveName(main);
			valid &= this.fieldName.isValid();
			this.adapterId = NO_PARTS;
		}

		this.valid = valid;
	}

	public final String getFileName() {
		return this.fileName;
	}

	public final String getName() {
		return this.name;
	}

	public final String getExtension() {
		return this.extension;
	}

	public final Name getFieldName() {
		return this.fieldName;
	}

	public final Name[] getAdapterId() {
		return this.adapterId;
	}

	public final Name[] getDeclaredIn() {
		return this.declaredIn;
	}

	public final String getComment() {
		return this.comment;
	}

	public final boolean isValid() {
		return this.valid;
	}

	public final boolean isAdapter() {
		return this.adapterId.length != 0;
	}

	public final boolean isOverride() {
		return this.declaredIn.length != 0;
	}

	@Override
	public String toString() {
		if (this.fileName == null) {
			return super.toString();
		}
		return this.fileName;
	}

	private static Name[] split(String name) {

		final String[] parts = name.split("\\.");
		final Name[] names = new Name[parts.length];

		for (int i = 0; i < parts.length; ++i) {
			names[i] = caseInsensitiveName(parts[i]);
		}

		return names;
	}

	private static boolean validNames(Name[] parts) {
		if (parts == null || parts.length == 0) {
			return true;
		}

		boolean valid = true;

		for (int i = 0; i < parts.length; ++i) {
			if (!parts[i].isValid()) {
				valid = false;
			}
		}

		return valid;
	}

}
