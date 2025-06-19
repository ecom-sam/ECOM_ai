package com.ecom.ai.ecomassistant.auth.config;

import com.ecom.ai.ecomassistant.auth.filter.JwtFilter;
import com.ecom.ai.ecomassistant.auth.realm.CouchbaseRealm;
import jakarta.servlet.Filter;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public DefaultWebSecurityManager securityManager(CouchbaseRealm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        return securityManager;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        var chain = new DefaultShiroFilterChainDefinition();

        //allow swagger
        chain.addPathDefinition("/swagger-ui/**", "anon");
        chain.addPathDefinition("/v3/api-docs/**", "anon");

        //user
        chain.addPathDefinition("/**/users/login", "anon");
        chain.addPathDefinition("/**/users/activate", "anon");

        //api use custom jwt filter
        chain.addPathDefinition("/**", "jwt");
        return chain;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            DefaultWebSecurityManager securityManager,
            ShiroFilterChainDefinition shiroFilterChainDefinition
    ) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        factoryBean.setFilterChainDefinitionMap(shiroFilterChainDefinition.getFilterChainMap());

        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", new JwtFilter());
        factoryBean.setFilters(filters);

        return factoryBean;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        var proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
