package cn.payingcloud.demo.support;

import cn.payingcloud.PayingCloud;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author YQ.Huang
 */
@Configuration
@EnableConfigurationProperties(ApiConfig.Properties.class)
public class ApiConfig {

    private final Properties api;

    public ApiConfig(Properties api) {
        this.api = api;
    }

    @Bean
    PayingCloud payingcloud() {
        return new PayingCloud(api.getAccessKeyId(),
                api.getAccessKeySecret(),
                api.getEndpoint());
    }

    @Validated
    @ConfigurationProperties(prefix = "api")
    public static class Properties {

        @NotBlank
        private String endpoint;

        @NotBlank
        private String accessKeyId;

        @NotBlank
        private String accessKeySecret;

        public String getEndpoint() {
            return endpoint;
        }

        public Properties setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public Properties setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
            return this;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public Properties setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
            return this;
        }

    }
}
