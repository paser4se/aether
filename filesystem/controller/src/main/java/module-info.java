/**
 * 
 */
/**
 * @author roart
 *
 */
module filesystem {
    exports roart.filesystem;

    requires common.config;
    requires common.constants;
    requires common.filesystem;
    requires curator.client;
    requires curator.framework;
    requires slf4j.api;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.context;
    requires spring.web;
    requires zookeeper;
}
