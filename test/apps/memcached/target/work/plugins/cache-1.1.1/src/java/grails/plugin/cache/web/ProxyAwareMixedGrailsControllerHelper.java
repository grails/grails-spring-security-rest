/* Copyright 2012-2013 SpringSource.
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
package grails.plugin.cache.web;

import grails.web.Action;
import groovy.lang.GroovyObject;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.web.servlet.mvc.MethodGrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.mvc.MixedGrailsControllerHelper;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.ReflectionUtils;

/**
 * The default implementation doesn't expect controllers to be proxied, and this
 * causes action methods to be ignored. Registered as the
 * "grailsControllerHelper" bean to replace the default.
 *
 * @author Burt Beckwith
 */
public class ProxyAwareMixedGrailsControllerHelper extends MixedGrailsControllerHelper {

	@Override
	protected Object retrieveAction(GroovyObject controller, String actionName, HttpServletResponse response) {

		Method method = ReflectionUtils.findMethod(AopProxyUtils.ultimateTargetClass(controller),
				actionName, MethodGrailsControllerHelper.NOARGS);

		if (method != null) {
			ReflectionUtils.makeAccessible(method);
			if (method.getAnnotation(Action.class) != null) {
				return method;
			}
		}

		return super.retrieveAction(controller, actionName, response);
	}
}
