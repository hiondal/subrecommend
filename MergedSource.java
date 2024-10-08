// File: spending/build.gradle

dependencies {
    implementation project(':subrecommend-biz') // Subrecommend 서비스의 DTO 참조를 위해 추가
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.cloud:spring-cloud-starter-config'		//Config client

    implementation 'org.springframework.boot:spring-boot-starter-amqp'
    implementation 'com.mysql:mysql-connector-j'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}



// File: spending/build/resources/main/application.yml
spring:
  cloud:
    config:
      uri: ${CONFIG_SERVER_FQDN:http://localhost:9001}
      name: spending-service
      profile: default
      label: main
  config:
    import: "optional:configserver:"

// File: spending/src/main/resources/application.yml
spring:
  cloud:
    config:
      uri: ${CONFIG_SERVER_FQDN:http://localhost:9001}
      name: spending-service
      profile: default
      label: main
  config:
    import: "optional:configserver:"

// File: spending/src/main/java/com/subspend/SpendingApplication.java
package com.subspend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SpendingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpendingApplication.class, args);
    }
}

// File: spending/src/main/java/com/subspend/repository/SpendingRepository.java
package com.subspend.repository;

import com.subspend.model.Spending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpendingRepository extends JpaRepository<Spending, Long> {
    List<Spending> findByUserId(String userId);
}

// File: spending/src/main/java/com/subspend/config/RabbitMQConfig.java
package com.subspend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Exchange spendingExchange() {
        return new TopicExchange("spending-exchange");
    }

    @Bean
    public Queue spendingUpdatedQueue() {
        return QueueBuilder.durable("spending-updated-queue")
                .withArgument("x-dead-letter-exchange", "spending-dlx-exchange")
                .withArgument("x-dead-letter-routing-key", "spending-updated.dlx")
                .build();
    }

    @Bean
    public Binding spendingUpdatedBinding(Queue spendingUpdatedQueue, TopicExchange spendingExchange) {
        return BindingBuilder.bind(spendingUpdatedQueue).to(spendingExchange).with("spending.updated");
    }

    @Bean
    public Exchange spendingDlxExchange() {
        return new FanoutExchange("spending-dlx-exchange");
    }

    @Bean
    public Queue spendingUpdatedDlxQueue() {
        return QueueBuilder.durable("spending-updated-dlx-queue").build();
    }

    @Bean
    public Binding spendingUpdatedDlxBinding(Queue spendingUpdatedDlxQueue, FanoutExchange spendingDlxExchange) {
        return BindingBuilder.bind(spendingUpdatedDlxQueue).to(spendingDlxExchange);
    }
}

// File: spending/src/main/java/com/subspend/config/DataInitializer.java
package com.subspend.config;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {
    private final SpendingRepository spendingRepository;

    @Autowired
    public DataInitializer(SpendingRepository spendingRepository) {
        this.spendingRepository = spendingRepository;
    }

    @Override
    public void run(String... args) {
        initSampleData();
    }

    private void initSampleData() {
        List<Spending> spendings = Arrays.asList(
                createSpending("user1", "식비", new BigDecimal("580000"), LocalDate.now()),
                createSpending("user1", "엔터테인먼트", new BigDecimal("150000"), LocalDate.now().minusDays(1)),
                createSpending("user1", "쇼핑", new BigDecimal("250000"), LocalDate.now().minusDays(2)),
                createSpending("user1", "뷰티", new BigDecimal("100000"), LocalDate.now().minusDays(3))
        );
        spendingRepository.saveAll(spendings);
    }

    private Spending createSpending(String userId, String category, BigDecimal amount, LocalDate date) {
        Spending spending = new Spending();
        spending.setUserId(userId);
        spending.setCategory(category);
        spending.setAmount(amount);
        spending.setDate(date);
        return spending;
    }
}

// File: spending/src/main/java/com/subspend/web/SpendingController.java
package com.subspend.web;

import com.subspend.model.Spending;
import com.subspend.service.SpendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spending")
public class SpendingController {
    private final SpendingService spendingService;

    @Autowired
    public SpendingController(SpendingService spendingService) {
        this.spendingService = spendingService;
    }

    @PostMapping
    public Spending createSpending(@RequestBody Spending spending) {
        return spendingService.createSpending(spending);
    }
}

// File: spending/src/main/java/com/subspend/model/Spending.java
package com.subspend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "spendings")
@Data
public class Spending {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;
}

// File: spending/src/main/java/com/subspend/service/SpendingService.java
package com.subspend.service;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import com.subrecommend.biz.dto.TopSpendingDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpendingService {
    private final SpendingRepository spendingRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public SpendingService(SpendingRepository spendingRepository, RabbitTemplate rabbitTemplate) {
        this.spendingRepository = spendingRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Spending createSpending(Spending spending) {
        Spending savedSpending = spendingRepository.save(spending);
        updateTopSpendingCategory(savedSpending.getUserId());
        return savedSpending;
    }

    private void updateTopSpendingCategory(String userId) {
        List<Spending> userSpendings = spendingRepository.findByUserId(userId);
        Map<String, BigDecimal> categorySpending = userSpendings.stream()
                .collect(Collectors.groupingBy(Spending::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Spending::getAmount, BigDecimal::add)));

        String topCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (topCategory != null) {
            TopSpendingDTO topSpending = new TopSpendingDTO();
            topSpending.setTopCategory(topCategory);
            topSpending.setTotalSpending(categorySpending.get(topCategory));
            topSpending.setUserId(userId);

            rabbitTemplate.convertAndSend("sub-event-exchange", "spending.updated", topSpending);
        }
    }
}

// File: subrecommend-biz/build.gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}

bootJar {
    enabled = false
}

jar {
    enabled = true
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/dto/CategoryDTO.java
package com.subrecommend.biz.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private String name;
    private String image;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/dto/RecommendedCategoryDTO.java
package com.subrecommend.biz.dto;

import lombok.Data;

@Data
public class RecommendedCategoryDTO {
    private String categoryName;
    private String categoryImage;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/dto/SubscriptionDTO.java
package com.subrecommend.biz.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubscriptionDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String logo;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/dto/SubscriptionDetailDTO.java
package com.subrecommend.biz.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionDetailDTO extends SubscriptionDTO {
    private String category;
    private int maxSharing;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/dto/TopSpendingDTO.java
package com.subrecommend.biz.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopSpendingDTO {
    private String userId;
    private String topCategory;
    private BigDecimal totalSpending;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/dto/SpendingDTO.java
package com.subrecommend.biz.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SpendingDTO {
    private String userId;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/inport/ISpendingService.java
package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.TopSpendingDTO;

public interface ISpendingService {
    TopSpendingDTO getTopSpendingCategory(String userId);
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/inport/ICategoryService.java
package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.CategoryDTO;
import java.util.List;

public interface ICategoryService {
    List<CategoryDTO> getAllCategories();
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/inport/IRecommendationService.java
package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.RecommendedCategoryDTO;

public interface IRecommendationService {
    RecommendedCategoryDTO getRecommendedCategory(String userId);
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/inport/ISubscriptionService.java
package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import java.util.List;

public interface ISubscriptionService {
    List<SubscriptionDTO> getSubscriptionsByCategory(String category);
    SubscriptionDetailDTO getSubscriptionDetails(String subscriptionId);
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/outport/ISpendingViewProvider.java
package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.SpendingDTO;
import java.util.List;

public interface ISpendingViewProvider {
    List<SpendingDTO> getUserSpending(String userId);
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/outport/ITopSpendingViewProvider.java
package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.TopSpendingDTO;

public interface ITopSpendingViewProvider {
    TopSpendingDTO getTopSpendingView(String userId);
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/outport/ISubscriptionProvider.java
package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import java.util.List;

public interface ISubscriptionProvider {
    List<SubscriptionDTO> getSubscriptionsByCategory(String category);
    SubscriptionDetailDTO getSubscriptionById(String subscriptionId);
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/outport/ICategoryProvider.java
package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.CategoryDTO;
import java.util.List;

public interface ICategoryProvider {
    List<CategoryDTO> getCategories();
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/service/CategoryServiceImpl.java
package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.CategoryDTO;
import com.subrecommend.biz.usecase.inport.ICategoryService;
import com.subrecommend.biz.usecase.outport.ICategoryProvider;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {
    private final ICategoryProvider categoryProvider;

    public CategoryServiceImpl(ICategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryProvider.getCategories();
    }
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/service/SubscriptionServiceImpl.java
package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import com.subrecommend.biz.usecase.inport.ISubscriptionService;
import com.subrecommend.biz.usecase.outport.ISubscriptionProvider;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements ISubscriptionService {
    private final ISubscriptionProvider subscriptionProvider;

    public SubscriptionServiceImpl(ISubscriptionProvider subscriptionProvider) {
        this.subscriptionProvider = subscriptionProvider;
    }

    @Override
    public List<SubscriptionDTO> getSubscriptionsByCategory(String category) {
        return subscriptionProvider.getSubscriptionsByCategory(category);
    }

    @Override
    public SubscriptionDetailDTO getSubscriptionDetails(String subscriptionId) {
        return subscriptionProvider.getSubscriptionById(subscriptionId);
    }
}


// File: subrecommend-biz/src/main/java/com/subrecommend/biz/usecase/service/RecommendationServiceImpl.java
package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.RecommendedCategoryDTO;
import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.biz.usecase.inport.IRecommendationService;
import com.subrecommend.biz.usecase.outport.ICategoryProvider;
import com.subrecommend.biz.usecase.outport.ITopSpendingViewProvider;
import org.springframework.stereotype.Service;

@Service
public class RecommendationServiceImpl implements IRecommendationService {
    private final ITopSpendingViewProvider topSpendingViewProvider;
    private final ICategoryProvider categoryProvider;

    public RecommendationServiceImpl(ITopSpendingViewProvider topSpendingViewProvider, ICategoryProvider categoryProvider) {
        this.topSpendingViewProvider = topSpendingViewProvider;
        this.categoryProvider = categoryProvider;
    }

    @Override
    public RecommendedCategoryDTO getRecommendedCategory(String userId) {
        TopSpendingDTO topSpendingDTO = topSpendingViewProvider.getTopSpendingView(userId);

        if (topSpendingDTO == null) {
            return null;
        }

        String topCategory = topSpendingDTO.getTopCategory();

        RecommendedCategoryDTO recommendedCategory = new RecommendedCategoryDTO();
        recommendedCategory.setCategoryName(topCategory);
        categoryProvider.getCategories().stream()
                .filter(c -> c.getName().equals(topCategory))
                .findFirst()
                .ifPresent(c -> recommendedCategory.setCategoryImage(c.getImage()));

        return recommendedCategory;
    }
}


// File: subrecommend-biz/src/main/java/com/subrecommend/biz/domain/Category.java
package com.subrecommend.biz.domain;

import lombok.Data;

@Data
public class Category {
    private Long id;
    private String name;
    private String image;
}


// File: subrecommend-biz/src/main/java/com/subrecommend/biz/domain/Subscription.java
package com.subrecommend.biz.domain;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Subscription {
    private String id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private String logo;
    private int maxSharing;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/domain/Spending.java
package com.subrecommend.biz.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Spending {
    private Long id;
    private String userId;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
}

// File: subrecommend-biz/src/main/java/com/subrecommend/biz/exception/BizException.java
package com.subrecommend.biz.exception;

public class BizException extends RuntimeException {
    private final String errorCode;

    public BizException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

// File: subrecommend-infra/build.gradle
dependencies {
    implementation project(':subrecommend-biz')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.cloud:spring-cloud-starter-config'		//Config client

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'mysql:mysql-connector-java:8.0.33'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'

    implementation 'org.springframework.boot:spring-boot-starter-amqp'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}

bootJar {
    enabled = true
}


// File: subrecommend-infra/build/resources/main/application.yml
spring:
  cloud:
    config:
      uri: ${CONFIG_SERVER_FQDN:http://localhost:9001}
      name: subrecommend-service
      profile: default
      label: main
  config:
    import: "optional:configserver:"

// File: subrecommend-infra/src/main/resources/application.yml
spring:
  cloud:
    config:
      uri: ${CONFIG_SERVER_FQDN:http://localhost:9001}
      name: subrecommend-service
      profile: default
      label: main
  config:
    import: "optional:configserver:"

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/SubRecommendApplication.java
package com.subrecommend.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.subrecommend.biz", "com.subrecommend.infra"})
@EntityScan("com.subrecommend.infra.out.entity")

@EnableDiscoveryClient
public class SubRecommendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubRecommendApplication.class, args);
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/dto/ErrorResponse.java
package com.subrecommend.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String errorCode;
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/entity/SubscriptionEntity.java
package com.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "subscriptions")
@Data
public class SubscriptionEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String logo;

    @Column(nullable = false)
    private int maxSharing;
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/entity/CategoryEntity.java
package com.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String image;
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/entity/SubscriptionCategoryEntity.java
package com.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subscription_categories")
@Data
public class SubscriptionCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String image;
}


// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/entity/TopSpendingView.java
package com.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "top_spending_view")
@Data
public class TopSpendingView {
    @Id
    private String userId;

    @Column(nullable = false)
    private String topCategory;

    @Column(nullable = false)
    private BigDecimal totalSpending;
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/adapter/TopSpendingViewProviderImpl.java
package com.subrecommend.infra.out.adapter;

import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.biz.usecase.outport.ITopSpendingViewProvider;
import com.subrecommend.infra.out.entity.TopSpendingView;
import com.subrecommend.infra.out.repo.TopSpendingViewRepository;
import org.springframework.stereotype.Component;

@Component
public class TopSpendingViewProviderImpl implements ITopSpendingViewProvider {
    private final TopSpendingViewRepository topSpendingViewRepository;

    public TopSpendingViewProviderImpl(TopSpendingViewRepository topSpendingViewRepository) {
        this.topSpendingViewRepository = topSpendingViewRepository;
    }

    @Override
    public TopSpendingDTO getTopSpendingView(String userId) {
        TopSpendingView topSpendingView = topSpendingViewRepository.findById(userId).orElse(null);

        if (topSpendingView != null) {
            TopSpendingDTO topSpendingDTO = new TopSpendingDTO();
            topSpendingDTO.setUserId(topSpendingView.getUserId());
            topSpendingDTO.setTopCategory(topSpendingView.getTopCategory());
            topSpendingDTO.setTotalSpending(topSpendingView.getTotalSpending());
            return topSpendingDTO;
        }

        return null;
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/adapter/CategoryProviderImpl.java
package com.subrecommend.infra.out.adapter;

import com.subrecommend.biz.dto.CategoryDTO;
import com.subrecommend.biz.usecase.outport.ICategoryProvider;
import com.subrecommend.infra.out.entity.CategoryEntity;
import com.subrecommend.infra.out.repo.ICategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryProviderImpl implements ICategoryProvider {
    private final ICategoryRepository categoryRepository;

    public CategoryProviderImpl(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDTO> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO mapToDTO(CategoryEntity entity) {
        CategoryDTO dto = new CategoryDTO();
        dto.setName(entity.getName());
        dto.setImage(entity.getImage());
        return dto;
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/adapter/SubscriptionProviderImpl.java
package com.subrecommend.infra.out.adapter;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import com.subrecommend.biz.usecase.outport.ISubscriptionProvider;
import com.subrecommend.infra.out.entity.SubscriptionEntity;
import com.subrecommend.infra.out.repo.ISubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscriptionProviderImpl implements ISubscriptionProvider {
    private final ISubscriptionRepository subscriptionRepository;

    public SubscriptionProviderImpl(ISubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<SubscriptionDTO> getSubscriptionsByCategory(String category) {
        return subscriptionRepository.findByCategory(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDetailDTO getSubscriptionById(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(this::mapToDetailDTO)
                .orElse(null);
    }

    private SubscriptionDTO mapToDTO(SubscriptionEntity entity) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setLogo(entity.getLogo());
        return dto;
    }

    private SubscriptionDetailDTO mapToDetailDTO(SubscriptionEntity entity) {
        SubscriptionDetailDTO dto = new SubscriptionDetailDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCategory(entity.getCategory());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setLogo(entity.getLogo());
        dto.setMaxSharing(entity.getMaxSharing());
        return dto;
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/repo/ICategoryRepository.java
package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<CategoryEntity, Long> {
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/repo/ISubscriptionCategoryRepository.java
package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.SubscriptionCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISubscriptionCategoryRepository extends JpaRepository<SubscriptionCategoryEntity, Long> {
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/repo/ISubscriptionRepository.java
package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ISubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {
    List<SubscriptionEntity> findByCategory(String category);
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/out/repo/TopSpendingViewRepository.java
package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.TopSpendingView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopSpendingViewRepository extends JpaRepository<TopSpendingView, String> {
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/in/web/SubscriptionController.java
package com.subrecommend.infra.in.web;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import com.subrecommend.biz.usecase.inport.ISubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "구독", description = "구독 서비스 관련 API")
public class SubscriptionController {
    private final ISubscriptionService subscriptionService;
    private final SubscriptionControllerHelper controllerHelper;

    public SubscriptionController(ISubscriptionService subscriptionService, SubscriptionControllerHelper controllerHelper) {
        this.subscriptionService = subscriptionService;
        this.controllerHelper = controllerHelper;
    }

    @GetMapping("/by-category")
    @Operation(summary = "카테고리별 구독 서비스 조회", description = "특정 카테고리의 구독 서비스 목록을 조회합니다.")
    public ResponseEntity<List<SubscriptionDTO>> getSubscriptionsByCategory(@RequestParam String category) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByCategory(category));
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "구독 서비스 상세 정보 조회", description = "특정 구독 서비스의 상세 정보를 조회합니다.")
    public ResponseEntity<SubscriptionDetailDTO> getSubscriptionDetails(@PathVariable String subscriptionId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionDetails(subscriptionId));
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/in/web/CategoryController.java
package com.subrecommend.infra.in.web;

import com.subrecommend.biz.dto.CategoryDTO;
import com.subrecommend.biz.usecase.inport.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "카테고리", description = "카테고리 관련 API")
public class CategoryController {
    private final ICategoryService categoryService;
    private final CategoryControllerHelper controllerHelper;

    public CategoryController(ICategoryService categoryService, CategoryControllerHelper controllerHelper) {
        this.categoryService = categoryService;
        this.controllerHelper = controllerHelper;
    }

    @GetMapping
    @Operation(summary = "모든 카테고리 조회", description = "모든 구독 카테고리를 조회합니다.")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/in/web/RecommendationControllerHelper.java
package com.subrecommend.infra.in.web;

import com.subrecommend.infra.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = RecommendationController.class)
public class RecommendationControllerHelper {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), "RECOMMENDATION_ERROR");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/in/web/RecommendationController.java
package com.subrecommend.infra.in.web;

import com.subrecommend.biz.dto.RecommendedCategoryDTO;
import com.subrecommend.biz.usecase.inport.IRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "추천", description = "추천 관련 API")
public class RecommendationController {
    private final IRecommendationService recommendationService;
    private final RecommendationControllerHelper controllerHelper;

    public RecommendationController(IRecommendationService recommendationService, RecommendationControllerHelper controllerHelper) {
        this.recommendationService = recommendationService;
        this.controllerHelper = controllerHelper;
    }

    @GetMapping("/category")
    @Operation(summary = "추천 구독 카테고리 조회", description = "사용자의 지출 패턴을 기반으로 추천 구독 카테고리를 조회합니다.")
    public ResponseEntity<RecommendedCategoryDTO> getRecommendedCategory(@RequestParam String userId) {
        return ResponseEntity.ok(recommendationService.getRecommendedCategory(userId));
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/in/web/SubscriptionControllerHelper.java
package com.subrecommend.infra.in.web;

import com.subrecommend.infra.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = SubscriptionController.class)
public class SubscriptionControllerHelper {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), "SUBSCRIPTION_ERROR");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/in/web/CategoryControllerHelper.java
package com.subrecommend.infra.in.web;

import com.subrecommend.infra.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = CategoryController.class)
public class CategoryControllerHelper {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), "CATEGORY_ERROR");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/in/consumer/SpendingEventListener.java
package com.subrecommend.infra.in.consumer;

import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.infra.out.entity.TopSpendingView;
import com.subrecommend.infra.out.repo.TopSpendingViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpendingEventListener {
    private final TopSpendingViewRepository topSpendingViewRepository;

    @Autowired
    public SpendingEventListener(TopSpendingViewRepository topSpendingViewRepository) {
        this.topSpendingViewRepository = topSpendingViewRepository;
    }

    @RabbitListener(queues = "spending-updated-queue")
    public void handleSpendingUpdatedEvent(TopSpendingDTO topSpending) {
        TopSpendingView topSpendingView = topSpendingViewRepository.findById(topSpending.getUserId())
                .orElse(new TopSpendingView());

        topSpendingView.setUserId(topSpending.getUserId());
        topSpendingView.setTopCategory(topSpending.getTopCategory());
        topSpendingView.setTotalSpending(topSpending.getTotalSpending());

        topSpendingViewRepository.save(topSpendingView);
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/common/config/DatabaseConfig.java
package com.subrecommend.infra.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.subrecommend.infra.out.repo")
@EnableTransactionManagement
public class DatabaseConfig {
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/common/config/RabbitMQConfig.java
package com.subrecommend.infra.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue spendingUpdatedQueue() {
        return QueueBuilder.durable("spending-updated-queue")
                .withArgument("x-dead-letter-exchange", "spending-dlx-exchange")
                .withArgument("x-dead-letter-routing-key", "spending-updated.dlx")
                .build();
    }

    @Bean
    public Queue spendingUpdatedDlxQueue() {
        return QueueBuilder.durable("spending-updated-dlx-queue").build();
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/common/config/DataInitializer.java
package com.subrecommend.infra.common.config;

import com.subrecommend.infra.out.entity.*;
import com.subrecommend.infra.out.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Profile("dev")
public class DataInitializer {

    private static final String HIGHEST_SPENDING_CATEGORY = "식비";

    @Bean
    @Transactional
    public CommandLineRunner initData(ICategoryRepository categoryRepository,
                                      ISubscriptionRepository subscriptionRepository,
                                      ISubscriptionCategoryRepository subscriptionCategoryRepository) {
        return args -> {
            // 모든 데이터 삭제
            deleteAllData(subscriptionRepository, categoryRepository, subscriptionCategoryRepository);

            // 데이터 재생성
            List<CategoryEntity> categories = initCategories(categoryRepository);
            List<SubscriptionCategoryEntity> subscriptionCategories = initSubscriptionCategories(subscriptionCategoryRepository);

            Map<String, SubscriptionCategoryEntity> categoryMap = subscriptionCategories.stream()
                    .collect(Collectors.toMap(SubscriptionCategoryEntity::getName, category -> category));
            initSubscriptions(subscriptionRepository, categoryMap);
        };
    }

    private void deleteAllData(ISubscriptionRepository subscriptionRepository,
                               ICategoryRepository categoryRepository,
                               ISubscriptionCategoryRepository subscriptionCategoryRepository) {
        subscriptionRepository.deleteAll();
        categoryRepository.deleteAll();
        subscriptionCategoryRepository.deleteAll();
    }

    private List<CategoryEntity> initCategories(ICategoryRepository categoryRepository) {
        List<CategoryEntity> categories = Arrays.asList(
                createCategory("식비", "food.png"),
                createCategory("엔터테인먼트", "entertainment.png"),
                createCategory("쇼핑", "shopping.png"),
                createCategory("뷰티", "beauty.png")
        );
        return categoryRepository.saveAll(categories);
    }

    private CategoryEntity createCategory(String name, String image) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        category.setImage(image);
        return category;
    }

    private List<SubscriptionCategoryEntity> initSubscriptionCategories(ISubscriptionCategoryRepository repository) {
        List<SubscriptionCategoryEntity> categories = Arrays.asList(
                createSubscriptionCategory("식비", "food_subscription.png"),
                createSubscriptionCategory("엔터테인먼트", "entertainment_subscription.png"),
                createSubscriptionCategory("쇼핑", "shopping_subscription.png"),
                createSubscriptionCategory("뷰티", "beauty_subscription.png")
        );
        return repository.saveAll(categories);
    }

    private SubscriptionCategoryEntity createSubscriptionCategory(String name, String image) {
        SubscriptionCategoryEntity category = new SubscriptionCategoryEntity();
        category.setName(name);
        category.setImage(image);
        return category;
    }

    private void initSubscriptions(ISubscriptionRepository repository, Map<String, SubscriptionCategoryEntity> categoryMap) {
        List<SubscriptionEntity> subscriptions = Arrays.asList(
                // 식비 카테고리 (최고 지출 카테고리)에 해당하는 구독 서비스
                createSubscription("sub1", "푸드박스", categoryMap.get("식비"), "주간 신선 식재료 배송", new BigDecimal("59900"), "foodbox.png", 1),
                createSubscription("sub2", "쿠킹클래스", categoryMap.get("식비"), "월간 온라인 요리 강좌", new BigDecimal("29900"), "cookingclass.png", 1),

                // 다른 카테고리의 구독 서비스
                createSubscription("sub3", "넷플릭스", categoryMap.get("엔터테인먼트"), "영화 및 드라마 스트리밍", new BigDecimal("14900"), "netflix.png", 4),
                createSubscription("sub4", "와이즐리", categoryMap.get("뷰티"), "면도날 구독 서비스", new BigDecimal("14900"), "wisely.png", 1),
                createSubscription("sub5", "스타일쉐어", categoryMap.get("쇼핑"), "월간 패션 아이템 구독", new BigDecimal("39900"), "styleshare.png", 1)
        );
        repository.saveAll(subscriptions);
    }

    private SubscriptionEntity createSubscription(String id, String name, SubscriptionCategoryEntity category, String description, BigDecimal price, String logo, int maxSharing) {
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(id);
        subscription.setName(name);
        subscription.setCategory(category.getName());
        subscription.setDescription(description);
        subscription.setPrice(price);
        subscription.setLogo(logo);
        subscription.setMaxSharing(maxSharing);
        return subscription;
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/common/config/SwaggerConfig.java
package com.subrecommend.infra.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("구독추천 API")
                        .version("1.0")
                        .description("구독추천 서비스의 API 문서입니다."));
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/common/config/CorsConfig.java
package com.subrecommend.infra.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 오리진 설정
        config.addAllowedOrigin("http://localhost:3000");

        // 허용할 헤더 설정
        config.addAllowedHeader("*");

        // 허용할 HTTP 메서드 설정
        config.addAllowedMethod("*");

        // 자격 증명 허용 (쿠키 등)
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

// File: subrecommend-infra/src/main/java/com/subrecommend/infra/exception/InfraException.java
package com.subrecommend.infra.exception;

public class InfraException extends RuntimeException {
    private final String errorCode;

    public InfraException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

// File: /Users/ondal/home/workspace/subrecommend/settings.gradle
rootProject.name = 'subrecommend'
include 'subrecommend-biz'
include 'subrecommend-infra'
include 'spending'



// File: /Users/ondal/home/workspace/subrecommend/build.gradle
plugins {
 //   id 'org.springframework.boot' version '3.1.0' apply false
 //   id 'io.spring.dependency-management' version '1.1.0' apply false
    id 'org.springframework.boot' version '3.2.6' apply false
    id 'io.spring.dependency-management' version '1.1.5' apply false
    id 'java'
}

allprojects {
    group = 'com.subrecommend'
    version = '0.0.1-SNAPSHOT'
    sourceCompatibility = '17'

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    dependencies {
        implementation 'org.springframework.boot:spring-boot-starter'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }

    //-- for spring cloud
    dependencyManagement {
        imports {
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:2023.0.2"
        }
    }

    test {
        useJUnitPlatform()
    }
}

