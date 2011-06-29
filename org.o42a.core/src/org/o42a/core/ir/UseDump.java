package org.o42a.core.ir;

import java.util.ArrayList;

import org.o42a.codegen.Generator;
import org.o42a.util.use.User;
import org.o42a.util.use.UserInfo;


/**
 * Use tracking dumper.
 *
 * <p>This class is only used for debugging purposes.</p>
 */
public class UseDump {

	private static final ArrayList<UseDump> uses = new ArrayList<UseDump>();

	public static void dumpUse(Object what, UserInfo user) {
		uses.add(new UseDump(what.toString(), user.toUser()));
	}

	public static void dumpSeparator(String what) {
		uses.add(new Separator(what));
	}

	public static void printUses(Generator generator) {
		for (UseDump dump : uses) {
			dump.print(generator);
		}
	}

	protected final String what;
	protected final User user;

	UseDump(String what, User user) {
		this.what = what;
		this.user = user;
	}

	public void print(Generator generator) {
		if (this.user.isUsedBy(generator.getUseCase())) {
			System.err.println("(!) " + this.what);
			System.err.println("  + " + this.user);
		}
	}

	@Override
	public String toString() {
		return this.what + " by " + this.user;
	}

	private static final class Separator extends UseDump {

		Separator(String what) {
			super(what, null);
		}

		@Override
		public void print(Generator generator) {
			System.err.println("(!) " + this.what);
		}

		@Override
		public String toString() {
			return this.what;
		}

	}

}
