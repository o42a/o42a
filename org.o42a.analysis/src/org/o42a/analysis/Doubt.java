/*
    Compilation Analysis
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.analysis;


public abstract class Doubt {

	private Doubt next;
	private Analyzer analyzer;

	public final Analyzer getAnalyzer() {
		return this.analyzer;
	}

	public final boolean addTo(Analyzer analyzer) {
		if (this.analyzer == analyzer) {
			return false;
		}
		if (this.analyzer == null) {
			this.analyzer = analyzer;
			analyzer.addDoubt(this);
			return true;
		}
		this.analyzer = analyzer;
		this.next = null;
		reused();
		analyzer.addDoubt(this);
		return true;
	}

	public abstract void resolveDoubt();

	protected abstract void reused();

	final Doubt getNext() {
		return this.next;
	}

	final void setNext(Doubt next) {
		this.next = next;
	}

}
