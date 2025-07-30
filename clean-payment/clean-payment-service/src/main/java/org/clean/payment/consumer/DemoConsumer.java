package org.clean.payment.consumer;

import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.Linq;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DemoConsumer {
    class Person {
        String name;
        int age;
        String city;

        public Person(String name, int age, String city) {
            this.name = name;
            this.age = age;
            this.city = city;
        }
    }

    @Test
    public void test1() {
        List<Person> people = Arrays.asList(
                new Person("Alice", 25, "New York"),
                new Person("Bob", 30, "London"),
                new Person("Charlie", 25, "New York"),
                new Person("David", 35, "Paris"),
                new Person("Eve", 30, "London")
        );

        // Linq方式 (创建匿名类型)
        var itemList = Linq.of(people)
                .where(p -> p.age > 25)
                .select(p -> new Object() {
                    String name = p.name;
                    String city = p.city;
                    boolean isAdult = p.age >= 20;
                });

        for (var item : itemList) {
            System.out.printf("  %s (%s) - %s%n", item.name, item.city, item.isAdult ? "Adult" : "Minor");
        }


    }
}
