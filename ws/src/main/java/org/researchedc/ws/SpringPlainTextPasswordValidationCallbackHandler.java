package org.researchedc.ws;

/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * Callback handler that validates a password using Spring Security's AuthenticationManager.
 * Uses WSS4J's WSPasswordCallback instead of the legacy Sun XWSS API.
 */
public class SpringPlainTextPasswordValidationCallbackHandler implements CallbackHandler, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private AuthenticationManager authenticationManager;

    private boolean ignoreFailure = false;

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback pwCallback = (WSPasswordCallback) callback;
                try {
                    Authentication authResult = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(pwCallback.getIdentifier(), pwCallback.getPassword()));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authentication success: " + authResult.toString());
                    }
                    SecurityContextHolder.getContext().setAuthentication(authResult);
                    pwCallback.setKey(pwCallback.getPassword() != null ? pwCallback.getPassword().getBytes() : null);
                } catch (AuthenticationException failed) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authentication request for user '" + pwCallback.getIdentifier() + "' failed: " + failed.toString());
                    }
                    SecurityContextHolder.clearContext();
                    if (!ignoreFailure) {
                        throw failed;
                    }
                }
                return;
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
}
