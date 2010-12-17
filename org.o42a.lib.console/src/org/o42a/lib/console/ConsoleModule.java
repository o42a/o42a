/*
    Console Module
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.lib.console;

import static org.o42a.lib.console.MainCall.mainCall;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.core.CompilerContext;
import org.o42a.core.artifact.intrinsic.Module;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.ir.IRGenerator;
import org.o42a.lib.console.impl.Print;


public class ConsoleModule extends Module {

	public static ConsoleModule consoleModule(CompilerContext context) {

		final URL base;

		try {

			final URL self = ConsoleModule.class.getResource(
					ConsoleModule.class.getSimpleName() + ".class");

			base = new URL(self, "../../../..");
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}

		final CompilerContext moduleContext =
			context.urlContext(
				"Console",
				base,
				"console.o42a",
				DECLARATION_LOGGER);

		return new ConsoleModule(moduleContext);
	}

	private ConsoleModule(CompilerContext context) {
		super(context, "Console");
	}

	public void generateMain(IRGenerator generator) {

		final Obj mainModule = getContext().getIntrinsics().getMainModule();

		if (mainModule == null) {
			return;
		}

		final MainCall mainCall = mainCall(toObject(), mainModule);

		if (mainCall != null) {
			mainCall.generateMain(generator);
		}
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		getFieldRegistry().declareMember(new Print(this).toMember());
		super.declareMembers(members);
	}

}
