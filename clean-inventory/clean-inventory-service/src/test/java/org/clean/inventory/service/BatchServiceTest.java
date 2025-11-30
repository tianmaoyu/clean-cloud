package org.clean.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.clean.inventory.entity.Batch;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
 public class BatchServiceTest {

    @Autowired
    private  BatchService batchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveBatch() {
        Batch batch = batchService.getById(1);
        assertNotNull(batch);

    }

//    @Test
    void generateTestData() {
        int total = 100000;
        int batchSize = 2000;
        List<Batch> list = new ArrayList<>();
        for (int i = 0; i <= total; i++) {
            Batch batch = new Batch();
            Long materialId = (long) ((i % 1000) + 1);
            batch.setMaterialId(materialId);
            batch.setBatchCode("BATCH-" + i);
            batch.setProductionDate(new Date());
            batch.setExpirationDate(new Date());
            batch.setSupplierId((long) (i % 10 + 1));
            batch.setAttributeJson(generateAttributeJson_2(materialId));
            list.add(batch);

            if (i % batchSize == 0) {
                batchService.insertBatchSomeColumn(list, batchSize);
                list.clear();
                log.info("-----{}----",i);
            }
        }
        if (!list.isEmpty()) {
            batchService.insertBatchSomeColumn(list, batchSize);
        }
    }

    @SneakyThrows
    private String generateAttributeJson(Long materialId) {
        Map<String, String> attributeMap = new HashMap<>();
        // 使用materialId作为种子保证可重复性
        Random random = new Random(materialId); 
        // 基础属性
        attributeMap.put("color", materialId % 2 == 0 ? "blue" : "red");
        attributeMap.put("type", "type-" + (materialId % 5 + 1));
        attributeMap.put("size", "size-" + (materialId % 3 + 1));

        // 随机添加5-9个额外属性
        int extraAttributes = 5 + random.nextInt(5); // 生成5到9之间的随机数
        for (int i = 0; i < extraAttributes; i++) {
            String key = "attr_" + String.format("%02d", i + 1);
            String value = "value-" + (random.nextInt(20) + 1);
            attributeMap.put(key, value);
        }

        return objectMapper.writeValueAsString(attributeMap);
    }

    @SneakyThrows
    private Map generateMap(Long materialId) {
        Map<String, String> attributeMap = new HashMap<>();
        // 使用materialId作为种子保证可重复性
        Random random = new Random(materialId);
        // 基础属性
        attributeMap.put("color", materialId % 2 == 0 ? "blue" : "red");
        attributeMap.put("type", "type-" + (materialId % 5 + 1));
        attributeMap.put("size", "size-" + (materialId % 3 + 1));

        // 随机添加5-9个额外属性
        int extraAttributes = 5 + random.nextInt(5); // 生成5到9之间的随机数
        for (int i = 0; i < extraAttributes; i++) {
            String key = "attr_" + String.format("%02d", i + 1);
            String value = "value-" + (random.nextInt(20) + 1);
            attributeMap.put(key, value);
        }

        return attributeMap;
    }

    @SneakyThrows
    private String generateAttributeJson_2(Long materialId) {

        Random random = new Random(materialId);
        // 随机添加0-5个额外属性
        int extraAttributes = random.nextInt(5); // 生成5到9之间的随机数

        return generateAttributes(extraAttributes);
    }
    @SneakyThrows
    private String generateAttributes(Integer categoryType) {
        JSONObject attributes = new JSONObject();

        switch (categoryType) {
            case 0:
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

            case 1:
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

            case 2:
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

            case 3:
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

            case 4:
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

}