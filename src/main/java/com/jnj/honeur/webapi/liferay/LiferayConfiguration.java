//package com.jnj.honeur.webapi.liferay;
//
//import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
//import com.jnj.honeur.webapi.DataSourceLookup;
////import com.jnj.honeur.webapi.service.HoneurUserService;
//import com.jnj.honeur.webapi.shiro.LiferayPermissionManager;
//import org.ohdsi.webapi.shiro.Entities.UserRepository;
//import org.ohdsi.webapi.shiro.PermissionManager;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//
///**
// * Create the right PermissionManager.
// *  - LiferayPermissionManager: Gets users and roles from liferay
// *  - WebApiPermissionManager: Gets users and roles from webapi
// *
// * @author Sander Bylemans
// */
//@Configuration
//public class LiferayConfiguration {
//
//    @Value("${datasource.honeur.enabled}")
//    private boolean liferay;
//
//    @Value("${webapi.central}")
//    private boolean central;
//
//    @Bean
//    public PermissionManager createPermissionManager(){
//        if(liferay && central){
//            return new LiferayPermissionManager();
//        } else {
//            return new PermissionManager();
//        }
//    }
//
////    @Bean
////    public DataSourceLookup createDataSourceLookup() {
////        if (liferay) {
////            return new DataSourceLookup();
////        } else {
////            return null;
////        }
////    }
//
//
////    @Bean
////    public CohortDefinitionService createCohortDefinitionService(){
////        if (liferay) {
////            return new HoneurCohortDefinitionService();
////        }
////        return new CohortDefinitionService();
////    }
//
////    @Bean
////    public UserService createUserService(){
////        if (liferay) {
////            return new HoneurUserService();
////        }
////        return new UserService();
////    }
//}
