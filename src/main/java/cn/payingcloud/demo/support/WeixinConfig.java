package cn.payingcloud.demo.support;

import cn.payingcloud.commons.weixin.basic.WxBasicApi;
import cn.payingcloud.commons.weixin.http.WxHttpClient;
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
@EnableConfigurationProperties(WeixinConfig.Properties.class)
public class WeixinConfig {

    private final Properties weixin;
    private final WxHttpClient wxHttpClient;

    public WeixinConfig(Properties weixin) {
        this.weixin = weixin;
        WeixinCallback.token = weixin.getToken();
        this.wxHttpClient = new WxHttpClient();
    }

    @Bean
    WxBasicApi wxBasicApi() {
        return new WxBasicApi(weixin.getAppId(), weixin.getAppSecret(), wxHttpClient);
    }

    @Validated
    @ConfigurationProperties(prefix = "weixin")
    public static class Properties {
        @NotBlank
        private String appId;
        @NotBlank
        private String appSecret;

        private String token;

        public Properties() {
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
