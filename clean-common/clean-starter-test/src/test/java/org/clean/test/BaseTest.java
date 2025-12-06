package org.clean.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(AuthorTestExtension.class)
public abstract class BaseTest {
    // BaseTest 内容
}