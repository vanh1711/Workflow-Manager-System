package com.example.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lớp khởi chạy ứng dụng Spring Boot Task & Workflow Management System.
 * {@code @SpringBootApplication} bao gồm:
 * - {@code @Configuration}: Đánh dấu lớp cấu hình chứa các bean.
 * - {@code @EnableAutoConfiguration}: Cơ chế tự động nạp cấu hình của Spring Boot dựa vào classpath.
 * - {@code @ComponentScan}: Quét và phát hiện các thành phần bean (@Component, @Service, @Repository, @Controller)
 *   trong package {@code com.example.taskmanager} và các package con.
 */
@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }

}
