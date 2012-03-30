package org.o42a.core.object;

import org.o42a.core.AbstractScope;
import org.o42a.core.ref.path.Path;


public abstract class ObjectScope extends AbstractScope {

	private Obj object;
	private Path enclosingScopePath;

	@Override
	public final Obj getContainer() {
		return toObject();
	}

	@Override
	public Path getEnclosingScopePath() {
		if (this.enclosingScopePath != null) {
			return this.enclosingScopePath;
		}
		if (getEnclosingScope().isTopScope()) {
			return null;
		}
		return this.enclosingScopePath = toObject().scopePath();
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return this.object.toString();
	}

	protected final Obj setScopeObject(Obj object) {
		return this.object = object;
	}

	protected final Obj getScopeObject() {
		return this.object;
	}

}
