package com.subrecommend.infra.config;

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
                                      ISpendingRepository spendingRepository,
                                      ISubscriptionRepository subscriptionRepository,
                                      ISubscriptionCategoryRepository subscriptionCategoryRepository) {
        return args -> {
            // 모든 데이터 삭제
            deleteAllData(spendingRepository, subscriptionRepository, categoryRepository, subscriptionCategoryRepository);

            // 데이터 재생성
            List<CategoryEntity> categories = initCategories(categoryRepository);
            List<SubscriptionCategoryEntity> subscriptionCategories = initSubscriptionCategories(subscriptionCategoryRepository);
            initSpendings(spendingRepository, categories);

            Map<String, SubscriptionCategoryEntity> categoryMap = subscriptionCategories.stream()
                    .collect(Collectors.toMap(SubscriptionCategoryEntity::getName, category -> category));
            initSubscriptions(subscriptionRepository, categoryMap);
        };
    }

    private void deleteAllData(ISpendingRepository spendingRepository,
                               ISubscriptionRepository subscriptionRepository,
                               ICategoryRepository categoryRepository,
                               ISubscriptionCategoryRepository subscriptionCategoryRepository) {
        spendingRepository.deleteAll();
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

    private void initSpendings(ISpendingRepository spendingRepository, List<CategoryEntity> categories) {
        String userId = "user1";
        LocalDate baseDate = LocalDate.of(2024, 9, 1);

        List<SpendingEntity> spendings = new ArrayList<>();
        for (CategoryEntity category : categories) {
            BigDecimal amount = category.getName().equals(HIGHEST_SPENDING_CATEGORY)
                    ? new BigDecimal("580000")
                    : new BigDecimal(String.valueOf((100000 + new Random().nextInt(200000))));
            spendings.add(createSpending(userId, category.getName(), amount, baseDate.plusDays(new Random().nextInt(30))));
        }
        spendingRepository.saveAll(spendings);
    }

    private SpendingEntity createSpending(String userId, String category, BigDecimal amount, LocalDate date) {
        SpendingEntity spending = new SpendingEntity();
        spending.setUserId(userId);
        spending.setCategory(category);
        spending.setAmount(amount);
        spending.setDate(date);
        return spending;
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
