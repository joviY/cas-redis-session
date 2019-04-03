package com.jovi.cas.casredissession.config;

import lombok.Data;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

@Configuration
public class CASConfig {

    private static boolean casEnabled = true;
    @Autowired
    private SpringCASAutoConfig springCASAutoConfig;

    @Bean
    public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener() {
        ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> listenerRegist = new ServletListenerRegistrationBean<>();
        listenerRegist.setEnabled(casEnabled);
        listenerRegist.setListener(new SingleSignOutHttpSessionListener());
        listenerRegist.setOrder(1);
        return listenerRegist;
    }

    /**
     * 用户注销后的filter,该过滤器要在其他过滤器之前
     * @return
     */
    @Bean
    public FilterRegistrationBean singleSignOutFilter() {
        FilterRegistrationBean<SingleSignOutFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter(new SingleSignOutFilter());
        filterRegistration.setOrder(2);
        filterRegistration.setEnabled(casEnabled);
        if (springCASAutoConfig.getSignOutFilters().size() > 0) {
            filterRegistration.setUrlPatterns(springCASAutoConfig.getSignOutFilters());
        } else {
            filterRegistration.addUrlPatterns("/*");
        }
        filterRegistration.addInitParameter("casServerUrlPrefix", springCASAutoConfig.getCasServerUrlPrefix());
        filterRegistration.addInitParameter("serverName", springCASAutoConfig.getServerName());
        return filterRegistration;
    }

    /**
     * 该过滤器负责用户的认证工作
     * @return
     */
    @Bean
    public FilterRegistrationBean authenticationFilter() {
        FilterRegistrationBean<Filter> filterRegistr = new FilterRegistrationBean<>();
        filterRegistr.setFilter(new AuthenticationFilter());
        filterRegistr.setEnabled(casEnabled);
        if (springCASAutoConfig.getAuthFilters().size() > 0) {
            filterRegistr.setUrlPatterns(springCASAutoConfig.getAuthFilters());
        } else {
            filterRegistr.addUrlPatterns("/*");
        }
        filterRegistr.setOrder(4);
        filterRegistr.addInitParameter("casServerLoginUrl", springCASAutoConfig.getCasServerLoginUrl());
        filterRegistr.addInitParameter("serverName", springCASAutoConfig.getServerName());
        filterRegistr.addInitParameter("useSession", springCASAutoConfig.isUseSession() ? "true" : "false");
        filterRegistr.addInitParameter("redirectAfterValidation", springCASAutoConfig.isRedirectAfterValidation() ? "true" : "false");
        return filterRegistr;
    }

    /**
     * 该过滤器是对用户的ticket验证
     * @return
     */
    @Bean
    public FilterRegistrationBean cas20ProxyReceivingTicketValidationFilter() {
        FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
        Cas20ProxyReceivingTicketValidationFilter cas20ProxyReceivingTicketValidationFilter = new Cas20ProxyReceivingTicketValidationFilter();
        cas20ProxyReceivingTicketValidationFilter.setServerName(springCASAutoConfig.getServerName());
        filterRegistration.setFilter(cas20ProxyReceivingTicketValidationFilter);
        filterRegistration.setOrder(3);
        filterRegistration.setEnabled(casEnabled);
        if (springCASAutoConfig.getValidateFilters().size() > 0) {
            filterRegistration.setUrlPatterns(springCASAutoConfig.getValidateFilters());
        } else {
            filterRegistration.addUrlPatterns("/*");
        }
        filterRegistration.addInitParameter("casServerUrlPrefix", springCASAutoConfig.getCasServerUrlPrefix());
        filterRegistration.addInitParameter("serverName", springCASAutoConfig.getServerName());
        return filterRegistration;
    }

    /**
     * 过滤器对http请求进行包装，可通过HttpServletRequest.getRemoteUser()获取到用户登陆名和密码
     * @return
     */
    @Bean
    public FilterRegistrationBean httpServletRequestWrapperFilter() {
        FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter(new HttpServletRequestWrapperFilter());
        filterRegistration.setEnabled(casEnabled);
        if (springCASAutoConfig.getRequestWrapperFilters().size() > 0) {
            filterRegistration.setUrlPatterns(springCASAutoConfig.getRequestWrapperFilters());
        } else {
            filterRegistration.addUrlPatterns("/login");
        }
        filterRegistration.setOrder(5);
        return filterRegistration;
    }

    /**
     * 该过滤器使得可以通过org.jasig.cas.client.util.AssertionHolder来获取用户的登录名。
     比如AssertionHolder.getAssertion().getPrincipal().getName()。
     这个类把Assertion信息放在ThreadLocal变量中，这样应用程序不在web层也能够获取到当前登录信息
     */
    @Bean
    public FilterRegistrationBean assertionThreadLocalFilter() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new AssertionThreadLocalFilter());
        filterRegistration.setEnabled(true);
        if(springCASAutoConfig.getAssertionFilters().size()>0)
            filterRegistration.setUrlPatterns(springCASAutoConfig.getAssertionFilters());
        else
            filterRegistration.addUrlPatterns("/*");
        filterRegistration.setOrder(7);
        return filterRegistration;
    }
}
