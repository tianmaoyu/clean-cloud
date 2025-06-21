package org.clean.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.clean.inventory.entity.Category;
import org.clean.inventory.entity.Product;
import org.clean.inventory.mapper.CategoryMapper;
import org.clean.inventory.mapper.ProductMapper;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataGeneratorService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    // 使用固定线程池
    private final Executor executor = Executors.newFixedThreadPool(8);

    public DataGeneratorService(CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    /**
     * 生成类别数据
     * @param count 生成数量
     */
    @Transactional
    public void generateCategories(int count) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Category> categories = new ArrayList<>();
        String[] types = {"electronics", "clothing", "food", "furniture", "books"};

        for (int i = 1; i <= count; i++) {
            String type = types[RandomUtils.nextInt(0, types.length)];
            Category category = new Category();
            category.setName(type + "-category-" + i);
            category.setCategoryType(type);
            category.setDescription("Description for " + type + " category " + i);
            category.setAttributeSchema(generateAttributeSchema(type));
            category.setActiveFlag(true);
            category.setCreatedAt(OffsetDateTime.now());
            category.setUpdatedAt(OffsetDateTime.now());

            // 随机设置父类别(10%的概率)
            if (i > 10 && RandomUtils.nextDouble(0, 1) < 0.1) {
                category.setParentId((long) RandomUtils.nextInt(1, i));
            }

            categories.add(category);

            // 每100条批量插入一次
            if (i % 100 == 0) {
                categoryMapper.insertBatchSomeColumn(categories);
                categories.clear();
                log.info("Generated {} categories", i);
            }
        }

        // 插入剩余数据
        if (!categories.isEmpty()) {
            categoryMapper.insertBatchSomeColumn(categories);
        }

        stopWatch.stop();
        log.info("Generated {} categories in {} ms", count, stopWatch.getTotalTimeMillis());
    }

    /**
     * 生成商品数据
     * @param totalCount 总商品数量
     */

    public void generateProducts(long totalCount) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 获取所有类别ID
        List<Long> categoryIds = categoryMapper.selectList(new QueryWrapper<Category>().select("id"))
                .stream().map(Category::getId).collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            throw new RuntimeException("No categories found, please generate categories first");
        }

        int batchSize = 1000; // 每批处理数量
        long batchCount = totalCount / batchSize;

        // 单线程处理：移除了CompletableFuture和多线程池
        List<Product> products = new ArrayList<>(batchSize);

        for (int batch = 0; batch < batchCount; batch++) {
            // 清空产品列表
            products.clear();

            // 生成当前批次的所有产品
            for (int i = 0; i < batchSize; i++) {
                long index = batch * batchSize + i;
                products.add(generateRandomProduct(categoryIds, index));
            }

            // 批量插入当前批次
            productMapper.insertBatchSomeColumn(products);
            log.info("Generated batch {} ({} products)", batch, batchSize);
        }

        // 处理剩余产品（总数量不是batchSize的整数倍时）
        long remaining = totalCount % batchSize;
        if (remaining > 0) {
            products.clear();
            for (int i = 0; i < remaining; i++) {
                long index = batchCount * batchSize + i;
                products.add(generateRandomProduct(categoryIds, index));
            }
            productMapper.insertBatchSomeColumn(products);
            log.info("Generated last batch ({} products)", remaining);
        }

        stopWatch.stop();
        log.info("Generated {} products in {} ms", totalCount, stopWatch.getTotalTimeMillis());
    }

    public void generateProducts_threadPool(long totalCount) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 获取所有类别ID
        List<Long> categoryIds = categoryMapper.selectList(new QueryWrapper<Category>().select("id"))
                .stream().map(Category::getId).collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            throw new RuntimeException("No categories found, please generate categories first");
        }

        int batchSize = 5000; // 每批处理数量
        long batchCount = totalCount / batchSize;

        // 创建线程池 (根据CPU核心数调整)
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);

        // 用于跟踪所有异步任务
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 多线程处理每个批次
        for (int batch = 0; batch < batchCount; batch++) {
            final int currentBatch = batch;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<Product> products = new ArrayList<>(batchSize);

                // 生成当前批次的所有产品
                for (int i = 0; i < batchSize; i++) {
                    long index = currentBatch * batchSize + i;
                    products.add(generateRandomProduct(categoryIds, index));
                }

                // 批量插入当前批次
                productMapper.insertBatchSomeColumn(products);
                log.info("Generated batch {} ({} products) by thread {}",
                        currentBatch, batchSize, Thread.currentThread().getName());
            }, executor);

            futures.add(future);
        }

        // 处理剩余产品（总数量不是batchSize的整数倍时）
        long remaining = totalCount % batchSize;
        if (remaining > 0) {
            CompletableFuture<Void> lastFuture = CompletableFuture.runAsync(() -> {
                List<Product> products = new ArrayList<>((int)remaining);
                for (int i = 0; i < remaining; i++) {
                    long index = batchCount * batchSize + i;
                    products.add(generateRandomProduct(categoryIds, index));
                }
                productMapper.insertBatchSomeColumn(products);
                log.info("Generated last batch ({} products) by thread {}",
                        remaining, Thread.currentThread().getName());
            }, executor);

            futures.add(lastFuture);
        }

        // 等待所有任务完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error during product generation", e);
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        stopWatch.stop();
        log.info("Generated {} products in {} ms", totalCount, stopWatch.getTotalTimeMillis());
    }
    private Product generateRandomProduct(List<Long> categoryIds, long index) {
        Long categoryId = categoryIds.get(RandomUtils.nextInt(0, categoryIds.size()));
        String categoryType = categoryMapper.selectById(categoryId).getCategoryType();

        Product product = new Product();
        product.setCode("PROD-" + index);
        product.setName("Product " + index);
        product.setCategoryId(categoryId);
        product.setBrandId((long) RandomUtils.nextInt(1, 100));
        product.setPrice(RandomUtils.nextDouble(10, 10000));
        product.setCost(RandomUtils.nextDouble(5, 5000));
        product.setStockQuantity(RandomUtils.nextInt(0, 1000));
        product.setWeight(RandomUtils.nextDouble(0.1, 10));
        product.setActiveFlag(true);
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());
        product.setDescription("Description for product " + index);
        product.setImageUrl("https://example.com/images/" + index + ".jpg");
        product.setTags(generateTags());
        product.setAttributes(generateAttributes(categoryType));

        return product;
    }

    private String generateTags() {
        String[] tags = {"new", "popular", "featured", "sale", "limited"};
        int count = RandomUtils.nextInt(1, 4);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append(tags[RandomUtils.nextInt(0, tags.length)]);
        }
        return sb.toString();
    }

    @SneakyThrows
    private String generateAttributes(String categoryType) {
        JSONObject attributes = new JSONObject();

        switch (categoryType) {
            case "electronics":
                // 电子产品属性
                attributes.put("screen_size", RandomUtils.nextDouble(4, 10) + "英寸");
                attributes.put("resolution", RandomUtils.nextInt(1080, 3840) + "x" + RandomUtils.nextInt(720, 2160));
                attributes.put("processor", "CPU-" + RandomStringUtils.randomAlphanumeric(4));
                attributes.put("ram", RandomUtils.nextInt(2, 32) + "GB");
                attributes.put("storage", RandomUtils.nextInt(32, 1024) + "GB");
                attributes.put("battery_capacity", RandomUtils.nextInt(2000, 6000) + "mAh");
                attributes.put("os", RandomUtils.nextBoolean() ? "Android" : "iOS");
                attributes.put("camera_main", RandomUtils.nextInt(8, 108) + "MP");
                attributes.put("camera_front", RandomUtils.nextInt(5, 32) + "MP");
                attributes.put("network", RandomUtils.nextBoolean() ? "5G" : "4G");
                attributes.put("color", getRandomColor());
                attributes.put("weight", RandomUtils.nextInt(100, 300) + "g");
                attributes.put("waterproof", "IP" + RandomUtils.nextInt(0, 8) + RandomUtils.nextInt(0, 8));
                attributes.put("fingerprint_sensor", RandomUtils.nextBoolean());
                attributes.put("face_unlock", RandomUtils.nextBoolean());
                attributes.put("warranty_period", RandomUtils.nextInt(12, 36) + "个月");
                attributes.put("bluetooth_version", "v" + RandomUtils.nextDouble(4.0, 5.3));
                break;

            case "clothing":
                // 服装类属性
                String[] sizes = {"XS", "S", "M", "L", "XL", "XXL"};
                String[] materials = {"棉", "涤纶", "丝绸", "羊毛", "亚麻", "尼龙"};
                String[] styles = {"休闲", "商务", "运动", "街头", "复古", "时尚"};

                attributes.put("size", sizes[RandomUtils.nextInt(0, sizes.length)]);
                attributes.put("color", getRandomColor());
                attributes.put("material", materials[RandomUtils.nextInt(0, materials.length)]);
                attributes.put("style", styles[RandomUtils.nextInt(0, styles.length)]);
                attributes.put("sleeve_length", RandomUtils.nextBoolean() ? "长袖" : "短袖");
                attributes.put("collar_type", RandomUtils.nextBoolean() ? "有领" : "无领");
                attributes.put("season", getRandomSeason());
                attributes.put("care_instructions", getRandomCareInstructions());
                attributes.put("origin", getRandomCountry());
                attributes.put("fit", RandomUtils.nextBoolean() ? "修身" : "宽松");
                attributes.put("closure_type", RandomUtils.nextBoolean() ? "拉链" : "纽扣");
                attributes.put("pocket_count", RandomUtils.nextInt(0, 5));
                attributes.put("stretchable", RandomUtils.nextBoolean());
                attributes.put("thickness", RandomUtils.nextBoolean() ? "薄" : "厚");
                attributes.put("is_wrinkle_free", RandomUtils.nextBoolean());
                break;

            case "food":
                // 食品类属性
                String[] flavors = {"原味", "辣味", "甜味", "咸味", "酸味", "五香"};
                String[] allergens = {"无", "牛奶", "鸡蛋", "花生", "大豆", "小麦", "海鲜"};

                attributes.put("weight", RandomUtils.nextInt(50, 1000) + "g");
                attributes.put("flavor", flavors[RandomUtils.nextInt(0, flavors.length)]);
                attributes.put("ingredients", getRandomIngredients());
                attributes.put("allergens", getRandomAllergens(allergens));
                attributes.put("storage_conditions", RandomUtils.nextBoolean() ? "常温" : "冷藏");
                attributes.put("shelf_life", RandomUtils.nextInt(3, 36) + "个月");
                attributes.put("origin_country", getRandomCountry());
                attributes.put("organic", RandomUtils.nextBoolean());
                attributes.put("vegetarian", RandomUtils.nextBoolean());
                attributes.put("vegan", RandomUtils.nextBoolean());
                attributes.put("gluten_free", RandomUtils.nextBoolean());
                attributes.put("package_type", RandomUtils.nextBoolean() ? "袋装" : "盒装");
                attributes.put("nutrition", getRandomNutritionInfo());
                attributes.put("expiry_date", LocalDateTime.now().plusMonths(RandomUtils.nextInt(1, 12)).toString());
                break;

            case "furniture":
                // 家具类属性
                String[] furnitureMaterials = {"实木", "板材", "金属", "玻璃", "塑料", "皮革"};
                String[] furnitureStyles = {"现代", "古典", "北欧", "工业风", "乡村", "简约"};

                attributes.put("material", furnitureMaterials[RandomUtils.nextInt(0, furnitureMaterials.length)]);
                attributes.put("style", furnitureStyles[RandomUtils.nextInt(0, furnitureStyles.length)]);
                attributes.put("color", getRandomColor());
                attributes.put("dimensions", getRandomDimensions());
                attributes.put("weight", RandomUtils.nextInt(5, 100) + "kg");
                attributes.put("assembly_required", RandomUtils.nextBoolean());
                attributes.put("warranty", RandomUtils.nextInt(12, 60) + "个月");
                attributes.put("origin", getRandomCountry());
                attributes.put("is_waterproof", RandomUtils.nextBoolean());
                attributes.put("is_flame_retardant", RandomUtils.nextBoolean());
                attributes.put("load_capacity", RandomUtils.nextInt(50, 500) + "kg");
                attributes.put("package_count", RandomUtils.nextInt(1, 5));
                attributes.put("maintenance", getRandomMaintenanceInstructions());
                break;

            case "books":
                // 图书类属性
                String[] genres = {"小说", "传记", "科技", "历史", "艺术", "教育", "儿童"};
                String[] bindings = {"平装", "精装", "线装", "电子书"};

                attributes.put("author", getRandomAuthorName());
                attributes.put("publisher", getRandomPublisher());
                attributes.put("publication_date", getRandomPublicationDate());
                attributes.put("isbn", getRandomISBN());
                attributes.put("genre", genres[RandomUtils.nextInt(0, genres.length)]);
                attributes.put("pages", RandomUtils.nextInt(50, 1000));
                attributes.put("language", RandomUtils.nextBoolean() ? "中文" : "英文");
                attributes.put("binding", bindings[RandomUtils.nextInt(0, bindings.length)]);
                attributes.put("edition", RandomUtils.nextInt(1, 10) + "版");
                attributes.put("weight", RandomUtils.nextInt(100, 2000) + "g");
                attributes.put("dimensions", getRandomBookDimensions());
                attributes.put("is_illustrated", RandomUtils.nextBoolean());
                attributes.put("age_range", getRandomAgeRange());
                break;

            default:
                // 默认属性（通用商品）
                attributes.put("color", getRandomColor());
                attributes.put("weight", RandomUtils.nextInt(100, 5000) + "g");
                attributes.put("material", "未知材质");
                attributes.put("origin", getRandomCountry());
                attributes.put("warranty", RandomUtils.nextInt(0, 24) + "个月");
                break;
        }

        return attributes.toString();
    }

    // 辅助方法
    private String getRandomColor() {
        String[] colors = {"红色", "蓝色", "绿色", "黄色", "黑色", "白色", "灰色", "粉色", "紫色", "橙色"};
        return colors[RandomUtils.nextInt(0, colors.length)];
    }

    private String getRandomCountry() {
        String[] countries = {"中国", "美国", "日本", "德国", "法国", "英国", "意大利", "韩国", "澳大利亚", "加拿大"};
        return countries[RandomUtils.nextInt(0, countries.length)];
    }

    private String getRandomSeason() {
        String[] seasons = {"春", "夏", "秋", "冬", "四季"};
        return seasons[RandomUtils.nextInt(0, seasons.length)];
    }

    private String getRandomCareInstructions() {
        String[] instructions = {"机洗", "手洗", "干洗", "不可水洗", "低温熨烫", "不可漂白"};
        return instructions[RandomUtils.nextInt(0, instructions.length)];
    }

    private List<String> getRandomIngredients() {
        String[] possibleIngredients = {"水", "盐", "糖", "面粉", "植物油", "食品添加剂", "防腐剂",
                "香料", "色素", "蛋白质", "碳水化合物", "维生素"};
        List<String> ingredients = new ArrayList<>();
        int count = RandomUtils.nextInt(3, 8);
        for (int i = 0; i < count; i++) {
            ingredients.add(possibleIngredients[RandomUtils.nextInt(0, possibleIngredients.length)]);
        }
        return ingredients;
    }

    private List<String> getRandomAllergens(String[] possibleAllergens) {
        List<String> allergens = new ArrayList<>();
        if (RandomUtils.nextDouble(0, 1) < 0.3) { // 30%概率有过敏原
            int count = RandomUtils.nextInt(1, 4);
            for (int i = 0; i < count; i++) {
                allergens.add(possibleAllergens[RandomUtils.nextInt(0, possibleAllergens.length)]);
            }
        } else {
            allergens.add("无");
        }
        return allergens;
    }

    @SneakyThrows
    private JSONObject getRandomNutritionInfo() {
        JSONObject nutrition = new JSONObject();
        nutrition.put("calories", RandomUtils.nextInt(50, 500) + "kcal");
        nutrition.put("protein", RandomUtils.nextInt(0, 50) + "g");
        nutrition.put("carbohydrate", RandomUtils.nextInt(0, 100) + "g");
        nutrition.put("fat", RandomUtils.nextInt(0, 30) + "g");
        return nutrition;
    }

    private String getRandomDimensions() {
        return RandomUtils.nextInt(30, 200) + "x" +
                RandomUtils.nextInt(30, 200) + "x" +
                RandomUtils.nextInt(30, 200) + "cm";
    }

    private String getRandomBookDimensions() {
        return RandomUtils.nextInt(10, 30) + "x" +
                RandomUtils.nextInt(15, 25) + "cm";
    }

    private String getRandomAuthorName() {
        String[] firstNames = {"张", "王", "李", "赵", "刘", "陈", "杨", "周", "吴", "黄"};
        String[] lastNames = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军"};
        return firstNames[RandomUtils.nextInt(0, firstNames.length)] +
                lastNames[RandomUtils.nextInt(0, lastNames.length)];
    }

    private String getRandomPublisher() {
        String[] publishers = {"人民文学出版社", "商务印书馆", "中华书局", "清华大学出版社",
                "机械工业出版社", "电子工业出版社", "中信出版社", "上海译文出版社"};
        return publishers[RandomUtils.nextInt(0, publishers.length)];
    }

    private String getRandomPublicationDate() {
        return LocalDateTime.now()
                .minusYears(RandomUtils.nextInt(0, 10))
                .minusMonths(RandomUtils.nextInt(0, 12))
                .toString();
    }

    private String getRandomISBN() {
        return "978-" + RandomUtils.nextInt(1, 10) + "-" +
                RandomUtils.nextInt(1000, 9999) + "-" +
                RandomUtils.nextInt(1000, 9999) + "-" +
                RandomUtils.nextInt(0, 10);
    }

    private String getRandomAgeRange() {
        String[] ranges = {"0-3岁", "4-6岁", "7-12岁", "13-18岁", "成人", "全年龄段"};
        return ranges[RandomUtils.nextInt(0, ranges.length)];
    }

    private String getRandomMaintenanceInstructions() {
        String[] instructions = {"定期清洁", "避免阳光直射", "使用专用清洁剂", "勿用湿布擦拭",
                "定期上油保养", "避免重物压迫", "专业维护"};
        return instructions[RandomUtils.nextInt(0, instructions.length)];
    }

    private String generateAttributeSchema(String type) {
        // 与之前相同的实现
        switch (type) {
            case "electronics":
                return "{\"properties\":{\"screen_size\":{\"type\":\"string\"},\"resolution\":{\"type\":\"string\"},\"processor\":{\"type\":\"string\"},\"ram\":{\"type\":\"string\"},\"storage\":{\"type\":\"string\"},\"battery_capacity\":{\"type\":\"string\"},\"os\":{\"type\":\"string\"},\"camera_main\":{\"type\":\"string\"},\"camera_front\":{\"type\":\"string\"},\"network\":{\"type\":\"string\"},\"color\":{\"type\":\"string\"},\"weight\":{\"type\":\"string\"},\"waterproof\":{\"type\":\"string\"},\"fingerprint_sensor\":{\"type\":\"boolean\"},\"face_unlock\":{\"type\":\"boolean\"}}}";
            case "clothing":
                return "{\"properties\":{\"size\":{\"type\":\"string\"},\"color\":{\"type\":\"string\"},\"material\":{\"type\":\"string\"},\"style\":{\"type\":\"string\"},\"sleeve_length\":{\"type\":\"string\"},\"collar_type\":{\"type\":\"string\"},\"pattern\":{\"type\":\"string\"},\"season\":{\"type\":\"string\"},\"care_instructions\":{\"type\":\"string\"},\"origin\":{\"type\":\"string\"},\"fit\":{\"type\":\"string\"},\"closure_type\":{\"type\":\"string\"},\"pocket_count\":{\"type\":\"integer\"},\"stretchable\":{\"type\":\"boolean\"},\"thickness\":{\"type\":\"string\"}}}";
            case "food":
                return "{\"properties\":{\"weight\":{\"type\":\"string\"},\"flavor\":{\"type\":\"string\"},\"ingredients\":{\"type\":\"array\"},\"allergens\":{\"type\":\"array\"},\"storage_conditions\":{\"type\":\"string\"},\"shelf_life\":{\"type\":\"string\"},\"origin_country\":{\"type\":\"string\"},\"organic\":{\"type\":\"boolean\"},\"vegetarian\":{\"type\":\"boolean\"},\"vegan\":{\"type\":\"boolean\"},\"gluten_free\":{\"type\":\"boolean\"},\"nutrition\":{\"type\":\"object\"},\"package_type\":{\"type\":\"string\"},\"certifications\":{\"type\":\"array\"}}}";
            default:
                return "{}";
        }
    }
}