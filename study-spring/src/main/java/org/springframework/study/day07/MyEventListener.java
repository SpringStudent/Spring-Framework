package org.springframework.study.day07;

import org.springframework.beans.factory.parsing.*;
/**
 * @author 周宁
 * @Date 2019-07-15 19:21
 */
public class MyEventListener implements ReaderEventListener {
    @Override
    public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {

    }

    @Override
    public void componentRegistered(ComponentDefinition componentDefinition) {
        System.out.println(componentDefinition);
    }

    @Override
    public void aliasRegistered(AliasDefinition aliasDefinition) {

    }

    @Override
    public void importProcessed(ImportDefinition importDefinition) {

    }
}
