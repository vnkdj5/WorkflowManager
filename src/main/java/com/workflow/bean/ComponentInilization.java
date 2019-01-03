package com.workflow.bean;

import org.springframework.beans.factory.InitializingBean;

public class ComponentInilization implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        ComponentRepository.getInstance();

    }

}
