/*
    Compiler Core
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
package org.o42a.core.ref.path;

import java.util.Arrays;

import org.o42a.util.ArrayUtil;


public final class PathLabels {

	public static final PathLabels NO_LABELS =
			new PathLabels(new PathLabel[0]);

	private final PathLabel[] labels;

	private PathLabels(PathLabel[] labels) {
		this.labels = labels;
	}

	public final boolean hasLabel(PathLabel label) {
		for (PathLabel l : this.labels) {
			if (l.equals(label)) {
				return true;
			}
		}
		return false;
	}

	public final PathLabels add(PathLabel label) {
		assert label != null :
			"Path label not specified";
		return new PathLabels(ArrayUtil.append(this.labels, label));
	}

	public final PathLabels addAll(PathLabels labels) {
		assert labels != null :
			"Path labels not specified";
		return new PathLabels(ArrayUtil.append(this.labels, labels.labels));
	}

	@Override
	public String toString() {
		if (this.labels == null) {
			return super.toString();
		}
		return "PathLabels" + Arrays.toString(this.labels);
	}

}
