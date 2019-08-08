/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.aop;

/**
 * Minimal interface for exposing the target class behind a proxy.
 *
 * 用于暴露代理target class的最小接口
 * <p>由AOP代理对象和代理工厂（通过{@link org.springframework.aop.framework.Advised}）
 * 以及{@link TargetSource TargetSources}实现
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)
 */
public interface TargetClassAware {

	/**
	 * 返回实现对象（通常是代理配置或实际代理）后面的目标类
	 * @return the target Class, or {@code null} if not known
	 */
	Class<?> getTargetClass();

}
