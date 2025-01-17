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

package org.apache.shardingsphere.infra.datasource.pool.props;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourceReflection;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.props.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.pool.props.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Data source pool properties creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePoolPropertiesCreator {
    
    /**
     * Create data source pool properties.
     *
     * @param configs data source configurations
     * @return created data source pool properties
     */
    public static Map<String, DataSourcePoolProperties> createFromConfiguration(final Map<String, DataSourceConfiguration> configs) {
        return configs.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Create data source properties.
     *
     * @param config data source configuration
     * @return created data source properties
     */
    public static DataSourcePoolProperties create(final DataSourceConfiguration config) {
        return new DataSourcePoolProperties(config.getConnection().getDataSourceClassName(), createProperties(config));
    }
    
    /**
     * Create data source properties.
     *
     * @param dataSources data sources
     * @return created data source properties
     */
    public static Map<String, DataSourcePoolProperties> create(final Map<String, DataSource> dataSources) {
        return dataSources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Create data source properties.
     * 
     * @param dataSource data source
     * @return created data source properties
     */
    public static DataSourcePoolProperties create(final DataSource dataSource) {
        DataSource realDataSource = dataSource instanceof CatalogSwitchableDataSource ? ((CatalogSwitchableDataSource) dataSource).getDataSource() : dataSource;
        return new DataSourcePoolProperties(realDataSource.getClass().getName(), createProperties(realDataSource));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final DataSourceConfiguration config) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataSourceClassName", config.getConnection().getDataSourceClassName());
        result.put("url", config.getConnection().getUrl());
        result.put("username", config.getConnection().getUsername());
        result.put("password", config.getConnection().getPassword());
        result.put("connectionTimeoutMilliseconds", config.getPool().getConnectionTimeoutMilliseconds());
        result.put("idleTimeoutMilliseconds", config.getPool().getIdleTimeoutMilliseconds());
        result.put("maxLifetimeMilliseconds", config.getPool().getMaxLifetimeMilliseconds());
        result.put("maxPoolSize", config.getPool().getMaxPoolSize());
        result.put("minPoolSize", config.getPool().getMinPoolSize());
        result.put("readOnly", config.getPool().getReadOnly());
        if (null != config.getPool().getCustomProperties()) {
            result.putAll((Map) config.getPool().getCustomProperties());
        }
        return result;
    }
    
    private static Map<String, Object> createProperties(final DataSource dataSource) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<DataSourcePoolMetaData> metaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, dataSource.getClass().getName());
        for (Entry<String, Object> entry : new DataSourceReflection(dataSource).convertToProperties().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            if (!metaData.isPresent() || isValidProperty(propertyName, propertyValue, metaData.get()) && !metaData.get().getTransientFieldNames().contains(propertyName)) {
                result.put(propertyName, propertyValue);
            }
        }
        return result;
    }
    
    private static boolean isValidProperty(final String key, final Object value, final DataSourcePoolMetaData metaData) {
        return null == value || !metaData.getSkippedProperties().containsKey(key) || !value.equals(metaData.getSkippedProperties().get(key));
    }
    
    /**
     * Create data source configuration.
     *
     * @param props data source pool properties
     * @return created data source configuration
     */
    public static DataSourceConfiguration createConfiguration(final DataSourcePoolProperties props) {
        return new DataSourceConfiguration(getConnectionConfiguration(props.getConnectionPropertySynonyms()), getPoolConfiguration(props.getPoolPropertySynonyms(), props.getCustomProperties()));
    }
    
    private static ConnectionConfiguration getConnectionConfiguration(final ConnectionPropertySynonyms connectionPropSynonyms) {
        Map<String, Object> standardProps = connectionPropSynonyms.getStandardProperties();
        return new ConnectionConfiguration(
                (String) standardProps.get("dataSourceClassName"), (String) standardProps.get("url"), (String) standardProps.get("username"), (String) standardProps.get("password"));
    }
    
    private static PoolConfiguration getPoolConfiguration(final PoolPropertySynonyms poolPropSynonyms, final CustomDataSourcePoolProperties customProps) {
        Map<String, Object> standardProps = poolPropSynonyms.getStandardProperties();
        Long connectionTimeoutMilliseconds = toLong(standardProps, "connectionTimeoutMilliseconds", null);
        Long idleTimeoutMilliseconds = toLong(standardProps, "idleTimeoutMilliseconds", null);
        Long maxLifetimeMilliseconds = toLong(standardProps, "maxLifetimeMilliseconds", null);
        Integer maxPoolSize = toInt(standardProps, "maxPoolSize", null);
        Integer minPoolSize = toInt(standardProps, "minPoolSize", null);
        Boolean readOnly = toBoolean(standardProps, "readOnly", null);
        Properties newCustomProps = new Properties();
        newCustomProps.putAll(customProps.getProperties());
        return new PoolConfiguration(connectionTimeoutMilliseconds, idleTimeoutMilliseconds, maxLifetimeMilliseconds, maxPoolSize, minPoolSize, readOnly, newCustomProps);
    }
    
    private static Long toLong(final Map<String, Object> props, final String name, final Long defaultValue) {
        if (!props.containsKey(name)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(props.get(name)));
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    private static Integer toInt(final Map<String, Object> props, final String name, final Integer defaultValue) {
        if (!props.containsKey(name)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(props.get(name)));
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    private static Boolean toBoolean(final Map<String, Object> props, final String name, final Boolean defaultValue) {
        if (!props.containsKey(name)) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(String.valueOf(props.get(name)));
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
}
