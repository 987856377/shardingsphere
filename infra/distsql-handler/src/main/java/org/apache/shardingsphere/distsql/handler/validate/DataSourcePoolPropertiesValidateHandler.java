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

package org.apache.shardingsphere.distsql.handler.validate;

import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.infra.datasource.pool.props.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.DataSourcePoolPropertiesValidator;

import java.util.Collection;
import java.util.Map;

/**
 * Data source pool properties validate handler.
 */
public final class DataSourcePoolPropertiesValidateHandler {
    
    /**
     * Validate data source properties map.
     * 
     * @param propsMap data source pool properties map
     * @throws InvalidStorageUnitsException invalid storage units exception
     */
    public void validate(final Map<String, DataSourcePoolProperties> propsMap) {
        Collection<String> errorMessages = new DataSourcePoolPropertiesValidator().validate(propsMap);
        if (!errorMessages.isEmpty()) {
            throw new InvalidStorageUnitsException(errorMessages);
        }
    }
}
