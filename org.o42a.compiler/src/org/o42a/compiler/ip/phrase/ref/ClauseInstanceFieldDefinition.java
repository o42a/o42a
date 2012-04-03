package org.o42a.compiler.ip.phrase.ref;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.ref.common.DefaultFieldDefinition;
import org.o42a.core.ref.path.BoundPath;


final class ClauseInstanceFieldDefinition extends DefaultFieldDefinition {

	private final ClauseInstanceConstructor constructor;

	ClauseInstanceFieldDefinition(
			BoundPath path,
			Distributor distributor,
			ClauseInstanceConstructor constructor) {
		super(path, distributor);
		this.constructor = constructor;
	}

	@Override
	public boolean isLink() {
		return pathToLink(path());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		this.constructor.getAscendants().updateAscendants(definer);
		definer.define(this.constructor.instance().getDefinition());
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

}
