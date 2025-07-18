package com.eaglebank

import com.eaglebank.config.TestJwtDecoderConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestJwtDecoderConfig::class)
@SpringBootTest
class EaglebankApplicationTests {

    @Test
    fun contextLoads() {
        // Test to ensure the application context loads successfully
    }
}
