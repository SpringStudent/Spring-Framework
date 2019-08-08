/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.context;

/**
 *适合于消息解析的对象的接口
 * {@link MessageSource}。
 *
 * <p>Spring's own validation error classes implement this interface.
 *
 * @author Juergen Hoeller
 * @see MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 * @see org.springframework.validation.ObjectError
 * @see org.springframework.validation.FieldError
 */
public interface MessageSourceResolvable {

	/**
	 * 按顺序返回用于解析此消息的代码
	 * 他们应该受到审判。 因此，最后一个代码将是默认代码
	 * @return a String array of codes which are associated with this message
	 */
	String[] getCodes();

	/**
	 * 返回用于解析此消息的参数数组。
	 * @return an array of objects to be used as parameters to replace
	 * placeholders within the message text
	 * @see java.text.MessageFormat
	 */
	Object[] getArguments();

	/**
	 * 返回用于解决此消息的默认消息。
	 * @return the default message, or {@code null} if no default
	 */
	String getDefaultMessage();

}
