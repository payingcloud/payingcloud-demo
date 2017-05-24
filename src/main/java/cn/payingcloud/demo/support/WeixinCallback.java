package cn.payingcloud.demo.support;

import cn.payingcloud.commons.weixin.WxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YQ.Huang
 */
@RestController
@RequestMapping("/callback/weixin")
public class WeixinCallback {

    private static final Logger logger = LoggerFactory.getLogger(WeixinCallback.class);

    static String token;

    @GetMapping
    public String validateToken(String signature, String timestamp, String nonce, String echostr) {
        logger.debug("Received a token validation request from Weixin");
        WxUtils.validateToken(token, signature, timestamp, nonce);
        logger.debug("Token validation passed!");
        return echostr;
    }

}
