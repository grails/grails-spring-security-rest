/* Copyright 2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.cache;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generate better cache key, compare to {@link org.springframework.cache.interceptor.DefaultKeyGenerator}
 */
public class CustomCacheKeyGenerator implements KeyGenerator {

	public Object generate(Object target, Method method, Object... params) {
		Class<?> objClass = AopProxyUtils.ultimateTargetClass(target);
		List<Object> cacheKey = new ArrayList<Object>(4);
		cacheKey.add(objClass.getName().intern());
		cacheKey.add(System.identityHashCode(target));
		cacheKey.add(method.toString().intern());
		if (params != null) {
			cacheKey.addAll(Arrays.asList(params));
		}
		return cacheKey;
	}
}
