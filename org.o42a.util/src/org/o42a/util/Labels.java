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

	public static final Labels NO_LABELS = new Labels(new Label[0]);

	private final Label[] labels;

	private Labels(Label[] labels) {
		this.labels = labels;
	}

	public final boolean has(Label label) {
		for (Label l : this.labels) {
			if (l.equals(label)) {
				return true;
			}
		}
		return false;
	}

	public final Labels add(Label label) {
		assert label != null :
			"Label not specified";
		return new Labels(ArrayUtil.append(this.labels, label));
	}

	public final Labels addAll(Labels labels) {
		assert labels != null :
			"Labels not specified";
		return new Labels(ArrayUtil.append(this.labels, labels.labels));
	}

	@Override
	public String toString() {
		if (this.labels == null) {
			return super.toString();
		}
		return "Labels" + Arrays.toString(this.labels);
	}

}
