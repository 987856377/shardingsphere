/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.authority.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Authority rule configuration.
 */
@Getter
@Setter
public final class AuthorityRuleConfiguration implements GlobalRuleConfiguration {
    
    private final Collection<ShardingSphereUser> users;
    
    private final AlgorithmConfiguration provider;
    
    private final Map<String, AlgorithmConfiguration> authenticators = new LinkedHashMap<>();
    
    private String defaultAuthenticator;
    
    public AuthorityRuleConfiguration(final Collection<ShardingSphereUser> users, final AlgorithmConfiguration provider) {
        this.users = users;
        this.provider = provider;
    }
    
    public AuthorityRuleConfiguration(final Collection<ShardingSphereUser> users, final AlgorithmConfiguration provider, final String defaultAuthenticator) {
        this.users = users;
        this.provider = provider;
        this.defaultAuthenticator = defaultAuthenticator;
    }
}
