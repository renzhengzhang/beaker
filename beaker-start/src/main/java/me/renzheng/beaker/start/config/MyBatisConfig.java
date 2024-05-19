package me.renzheng.beaker.start.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatisConfig
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Configuration
@MapperScan("me.renzheng.beaker.dao.mapper")
public class MyBatisConfig {

    @Primary
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        bean.setMapperLocations(resolver.getResources("classpath*:mapper/**/*.xml"));
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

}
