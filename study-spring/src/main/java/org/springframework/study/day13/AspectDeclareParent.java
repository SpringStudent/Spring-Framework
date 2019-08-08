package org.springframework.study.day13;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

/**
 * @author 周宁
 * @Date 2019-07-23 16:34
 */
@Aspect
public class AspectDeclareParent {

    @DeclareParents(value = "org.springframework.study.day13.NoMethodAspectBean", defaultImpl = IDeclareParentImpl.class)
    private IDeclareParent iDeclareParent;
}
