/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.mock.staticmock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate a test class for whose {@code @Test} methods
 * static methods on JPA-annotated {@code @Entity} classes should be mocked.
 *
 * <p>See {@link AnnotationDrivenStaticEntityMockingControl} for details.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @deprecated as of Spring 4.3, in favor of a custom aspect for such purposes
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MockStaticEntityMethods {

}
