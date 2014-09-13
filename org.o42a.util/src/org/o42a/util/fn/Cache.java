/*
    Utilities
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.util.fn;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;


public final class Cache<K, V> implements Function<K, V> {

	public static <K, V> Cache<K, V> cache(Function<K, V> valueByKey) {
		return new Cache<>(new HashMap<>(), valueByKey);
	}

	public static <K, V> Cache<K, V> identityCache(Function<K, V> valueByKey) {
		return new Cache<>(new IdentityHashMap<>(), valueByKey);
	}

	public static <K, V> Cache<K, V> cache(
			Map<K, V> cache,
			Function<K, V> valueByKey) {
		assert cache != null :
			"Cache not specified";
		return new Cache<>(cache, valueByKey);
	}

	private final Function<K, V> valueByKey;
	private Map<K, V> cache;

	private Cache(Map<K, V> cache, Function<K, V> valueByKey) {
		this.valueByKey = valueByKey;
		this.cache = cache;
	}

	public final V get(K key) {

		final V cached = this.cache.get(key);

		if (cached != null) {
			return cached;
		}

		final V value = this.valueByKey.apply(key);

		this.cache.put(key, value);

		return value;
	}

	@Override
	public final V apply(K key) {
		return get(key);
	}

}
