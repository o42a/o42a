/*
    Utilities
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.util;

import java.util.Arrays;


public final class Labels {

	public static final Labels NO_LABELS = new Labels(new Entry[0]);

	private final Entry[] entries;

	private Labels(Entry[] entries) {
		this.entries = entries;
	}

	public final boolean have(Label<?> label) {
		for (Entry entry : this.entries) {
			if (entry.getLabel().equals(label)) {
				return true;
			}
		}
		return false;
	}

	public final Labels add(Label<?> label) {
		return put(label, null);
	}

	@SuppressWarnings("unchecked")
	public final <L extends Label<T>, T> T get(L label) {
		for (Entry entry : this.entries) {
			if (entry.getLabel().equals(label)) {
				return (T) entry.getValue();
			}
		}
		return null;
	}

	public final <L extends Label<T>, T> Labels put(L label, T value) {
		assert label != null :
			"Label not specified";
		return addEntry(new Entry(label, value));
	}

	public final Labels addAll(Labels labels) {
		assert labels != null :
			"Labels not specified";
		if (this.entries.length == 0) {
			return labels;
		}
		if (labels.entries.length == 0) {
			return this;
		}

		Labels result = this;

		for (Entry entry : labels.entries) {
			result = result.addEntry(entry);
		}

		return result;
	}

	public final Labels remove(Label<?> label) {
		assert label != null :
			"Label not specified";

		for (int i = 0; i < this.entries.length; ++i) {

			final Entry entry = this.entries[i];

			if (entry.getLabel().equals(label)) {
				if (this.entries.length == 0) {
					return NO_LABELS;
				}
				return new Labels(ArrayUtil.remove(this.entries, i));
			}
		}

		return this;
	}

	@Override
	public String toString() {
		if (this.entries == null) {
			return super.toString();
		}
		return "Labels" + Arrays.toString(this.entries);
	}

	private Labels addEntry(final Entry newEntry) {

		final Label<?> label = newEntry.getLabel();

		for (int i = 0; i < this.entries.length; ++i) {

			final Entry entry = this.entries[i];

			if (entry.getLabel().equals(label)) {

				final Entry[] newEntries = this.entries.clone();

				newEntries[i] = newEntry;

				return new Labels(newEntries);
			}
		}

		return new Labels(ArrayUtil.append(this.entries, newEntry));
	}

	private static final class Entry {

		private final Label<?> label;
		private final Object value;

		Entry(Label<?> label, Object value) {
			this.label = label;
			this.value = value;
		}

		public final Label<?> getLabel() {
			return this.label;
		}

		public final Object getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			if (this.value == null) {
				if (this.label == null) {
					return super.toString();
				}
				return this.label.toString();
			}
			return this.label + "=" + this.value;
		}

	}

}
