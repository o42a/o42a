/*
    Utilities
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
package org.o42a.util.string;



public abstract class NameEncoderProxy extends NameEncoder {

	private final NameEncoder out;

	public NameEncoderProxy(NameEncoder out) {
		assert out != null :
			"Proxied name writer not specified";
		this.out = out;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.out + ']';
	}

	protected final NameEncoder out() {
		return this.out;
	}

	@Override
	protected void writeName(CPWriter out, Name name) {
		out().writeName(out, name);
	}

	@Override
	protected void writeSeparator(CPWriter out, IDSeparator separator) {
		out().writeSeparator(out, separator);
	}

	@Override
	protected void endSeparator(CPWriter out, IDSeparator separator) {
		out().endSeparator(out, separator);
	}

	@Override
	protected ID expandSubID(SubID subID) {
		return out().expandSubID(subID);
	}

}
