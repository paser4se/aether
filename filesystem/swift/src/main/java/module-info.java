/**
 * 
 */
/**
 * @author roart
 *
 */
module swift {
    exports roart.filesystem.swift;

    requires common.config;
    requires common.constants;
    requires common.filesystem;
    requires common.model;
    requires filesystem;
    requires joss;
    requires slf4j.api;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.web;
    requires spring.boot;
}