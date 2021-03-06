/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.web.logging;

import org.jtalks.jcommune.service.security.SecurityService;
import org.jtalks.jcommune.web.filters.LoggingConfigurationFilter;
import org.mockito.Mock;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Anuar_Nurmakanov
 */
public class LoggingConfigurationFilterTest {
    @Mock
    private SecurityService securityService;
    @Mock
    private LoggerMdc loggerMdc;
    private LoggingConfigurationFilter loggingConfigurationFilter;

    @BeforeMethod
    public void init() {
        initMocks(this);
        this.loggingConfigurationFilter = new LoggingConfigurationFilter(securityService, loggerMdc);
    }

    @Test
    public void userShouldBeRegisteredAndUnregisteredWhenChainEnded() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain();
        String userName = "Shogun";
        when(securityService.getCurrentUserUsername()).thenReturn(userName);

        loggingConfigurationFilter.doFilter(request, response, filterChain);

        verify(loggerMdc).registerUser(userName);
        verify(loggerMdc).unregisterUser();
    }

    @Test
    public void anonymousUserRegisteredAndUnregistered() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession httpSession = spy(new MockHttpSession());
        request.setSession(httpSession);
        MockFilterChain filterChain = new MockFilterChain();

        String userName = "";
        when(securityService.getCurrentUserUsername()).thenReturn(userName);
        when(httpSession.getId()).thenReturn("AF7823");

        loggingConfigurationFilter.doFilter(request, response, filterChain);

        verify(loggerMdc).registerUser("anonymous-7823");
        verify(loggerMdc).unregisterUser();
    }

    /**
     * This is just a hypothetical situation, session id should always be present since it's generated by web server.
     *
     * @throws Exception don't care
     */
    @Test
    public void anonymousUserWithoutSessionIdShouldNotRegister() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession httpSession = spy(new MockHttpSession());
        request.setSession(httpSession);
        MockFilterChain filterChain = new MockFilterChain();

        when(securityService.getCurrentUserUsername()).thenReturn("");
        when(httpSession.getId()).thenReturn("");

        loggingConfigurationFilter.doFilter(request, response, filterChain);

        verify(loggerMdc, times(0)).registerUser(anyString());
        verify(loggerMdc, times(0)).unregisterUser();
    }

}
