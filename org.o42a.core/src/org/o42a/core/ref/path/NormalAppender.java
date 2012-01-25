package org.o42a.core.ref.path;


public abstract class NormalAppender extends NormalStep {

	public abstract Path appendTo(Path path);

	@Override
	public final InlineStep toInline() {
		return null;
	}

	@Override
	public final NormalAppender toAppender() {
		return this;
	}

}