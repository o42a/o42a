/*
    Compiler Core
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
package org.o42a.core.ref;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.impl.normalizer.InlineFalse;
import org.o42a.core.ref.impl.normalizer.InlineTrue;
import org.o42a.core.ref.impl.normalizer.UnknownInlineCond;
import org.o42a.util.Cancelable;


public abstract class InlineCond implements Cancelable {

	public static final InlineCond INLINE_TRUE = InlineTrue.INSTANCE;
	public static final InlineCond INLINE_FALSE = InlineFalse.INSTANCE;
	public static final InlineCond INLINE_UNKNOWN = UnknownInlineCond.INSTANCE;

	public abstract void writeCond(CodeDirs dirs, HostOp host);

}
