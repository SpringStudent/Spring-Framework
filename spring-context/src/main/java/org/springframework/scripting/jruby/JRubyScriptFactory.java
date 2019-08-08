/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.scripting.jruby;

import java.io.IOException;
import java.lang.reflect.Method;

import org.jruby.RubyException;
import org.jruby.exceptions.JumpException;
import org.jruby.exceptions.RaiseException;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.springframework.scripting.ScriptFactory} implementation
 * for a JRuby script.
 *
 * <p>Typically used in combination with a
 * {@link org.springframework.scripting.support.ScriptFactoryPostProcessor};
 * see the latter's javadoc for a configuration example.
 *
 * <p>Note: Spring 4.0 supports JRuby 1.5 and higher, with 1.7.x recommended.
 * As of Spring 4.2, JRuby 9.0.0.0 is supported as well but primarily through
 * {@link org.springframework.scripting.support.StandardScriptFactory}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see JRubyScriptUtils
 * @see org.springframework.scripting.support.ScriptFactoryPostProcessor
 * @deprecated in favor of JRuby support via the JSR-223 abstraction
 * ({@link org.springframework.scripting.support.StandardScriptFactory})
 */
@Deprecated
public class JRubyScriptFactory implements ScriptFactory, BeanClassLoaderAware {

	private static final Method getMessageMethod = ClassUtils.getMethodIfAvailable(RubyException.class, "getMessage");


	private final String scriptSourceLocator;

	private final Class<?>[] scriptInterfaces;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * Create a new JRubyScriptFactory for the given script source.
	 * @param scriptSourceLocator a locator that points to the source of the script.
	 * Interpreted by the post-processor that actually creates the script.
	 * @param scriptInterfaces the Java interfaces that the scripted object
	 * is supposed to implement
	 */
	public JRubyScriptFactory(String scriptSourceLocator, Class<?>... scriptInterfaces) {
		Assert.hasText(scriptSourceLocator, "'scriptSourceLocator' must not be empty");
		Assert.notEmpty(scriptInterfaces, "'scriptInterfaces' must not be empty");
		this.scriptSourceLocator = scriptSourceLocator;
		this.scriptInterfaces = scriptInterfaces;
	}


	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	@Override
	public String getScriptSourceLocator() {
		return this.scriptSourceLocator;
	}

	@Override
	public Class<?>[] getScriptInterfaces() {
		return this.scriptInterfaces;
	}

	/**
	 * JRuby scripts do require a config interface.
	 */
	@Override
	public boolean requiresConfigInterface() {
		return true;
	}

	/**
	 * Load and parse the JRuby script via JRubyScriptUtils.
	 * @see JRubyScriptUtils#createJRubyObject(String, Class[], ClassLoader)
	 */
	@Override
	public Object getScriptedObject(ScriptSource scriptSource, Class<?>... actualInterfaces)
			throws IOException, ScriptCompilationException {
		try {
			return JRubyScriptUtils.createJRubyObject(
					scriptSource.getScriptAsString(), actualInterfaces, this.beanClassLoader);
		}
		catch (RaiseException ex) {
			String msg = null;
			RubyException rubyEx = ex.getException();
			if (rubyEx != null) {
				if (getMessageMethod != null) {
					// JRuby 9.1.7+ enforces access via getMessage() method
					msg = ReflectionUtils.invokeMethod(getMessageMethod, rubyEx).toString();
				}
				else {
					// JRuby 1.7.x: no accessor, just a public message field
					if (rubyEx.message != null){
						msg = rubyEx.message.toString();
					}
				}
			}
			throw new ScriptCompilationException(scriptSource, (msg != null ? msg : "Unexpected JRuby error"), ex);
		}
		catch (JumpException ex) {
			throw new ScriptCompilationException(scriptSource, ex);
		}
	}

	@Override
	public Class<?> getScriptedObjectType(ScriptSource scriptSource)
			throws IOException, ScriptCompilationException {

		return null;
	}

	@Override
	public boolean requiresScriptedObjectRefresh(ScriptSource scriptSource) {
		return scriptSource.isModified();
	}


	@Override
	public String toString() {
		return "JRubyScriptFactory: script source locator [" + this.scriptSourceLocator + "]";
	}

}
