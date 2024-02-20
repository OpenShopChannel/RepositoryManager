package org.oscwii.repositorymanager.config;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class JdbiConfig
{
    @Bean
    public SqlObjectPlugin sqlObjectPlugin()
    {
        return new SqlObjectPlugin();
    }

    @Bean
    public Jdbi jdbi(DataSource ds, List<JdbiPlugin> jdbiPlugins, List<RowMapper<?>> rowMappers)
    {
        var dataSourceProxy = new TransactionAwareDataSourceProxy(ds);
        Jdbi jdbi = Jdbi.create(dataSourceProxy);

        jdbiPlugins.forEach(jdbi::installPlugin);
        rowMappers.forEach(jdbi::registerRowMapper);

        return jdbi;
    }
}
