/*
    Utilities
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
package org.o42a.util.use;

import java.util.Iterator;


public abstract class Uses implements UseInfo {

	private UseFlag useFlag;
	private int rev;

	public final boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public UseFlag getUseBy(UseCase useCase) {
		if (useCase.caseFlag(this.useFlag)) {
			return this.useFlag;
		}

		final Iterator<? extends UseInfo> usedBy = usedBy();

		if (usedBy == null) {
			return this.useFlag = useCase.unusedFlag();
		}

		final int rev = useCase.start(this);
		boolean skipped = false;

		if (this.rev == rev) {
			return null;
		}
		this.rev = rev;
		while (usedBy.hasNext()) {

			final UseFlag flag = usedBy.next().getUseBy(useCase);

			if (flag == null) {
				skipped = true;
				continue;
			}
			if (flag.isUsed()) {
				useCase.end(this);
				return this.useFlag = flag;
			}
		}
		if (useCase.end(this) || !skipped) {
			return this.useFlag = useCase.unusedFlag();
		}
		return null;
	}

	protected abstract Iterator<? extends UseInfo> usedBy();

}
