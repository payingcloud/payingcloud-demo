package cn.payingcloud.demo.controller;

import cn.payingcloud.PayingCloud;
import cn.payingcloud.commons.weixin.basic.WxBasicApi;
import cn.payingcloud.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author YQ.Huang
 */
@Api(tags = "order", description = " ")
@Controller
@RequestMapping("/order")
class OrderController {
    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final PayingCloud payingcloud;
    private final WxBasicApi wxBasicApi;

    private static final String rsaPubilcKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCPymtLbRkHgvVfUT933LrwWns6YZHLPpT1pP9TKJ+cgIZiQwZ4mtqoqPHSVtiT5HA8fwFzWuJ/6qWaQhER7TOISUFUHZlHyBjNK/Z5px6PNB7rT4OrLP0KuZ7nuX5qdnOKuAbrj1MBLSinOHQ8tDJhPrPKxuZlKw3SeL5auHlKWwIDAQAB";

    @Autowired
    public OrderController(PayingCloud payingcloud, WxBasicApi wxBasicApi) {
        this.payingcloud = payingcloud;
        this.wxBasicApi = wxBasicApi;
    }

    @ApiOperation("收款")
    @GetMapping("/charge")
    public void charge(@RequestParam PcChannelType channel, HttpServletResponse servletResponse) throws Exception {
        switch (channel) {
            // 微信公众号支付需要先获取用户授权才可获取openId，因此单独处理
            case WXPAY_JSAPI:
            case CMBCPAY_T0_WX_JSAPI:
            case CMBCPAY_T1_WX_JSAPI:
            case WEBANKPAY_WX_JSAPI:
                String redirectUrl = ServletUriComponentsBuilder.fromCurrentRequestUri()
                        .path("/wx").build().toUriString();
                String url = wxBasicApi.getBaseAuthorizeUrl(redirectUrl, channel.toString());
                servletResponse.sendRedirect(url);
                return;
            case KFTPAY_CASHIER:
                servletResponse.sendRedirect("/pages/qrcode.html?codeUrl=https://jhpay.kftpay.com.cn/cloud/cloudplatform/payChannel/qrcodePay/000020010000000001.html");
                return;
        }
        // 其它渠道收款
        PcChargeRequest chargeRequest = buildChargeRequest(channel, buildChargeReturnUrl(), null);
        PcCharge charge = payingcloud.execute(chargeRequest);
        respondCharge(channel, charge, servletResponse);
    }

    @ApiOperation("微信公众号收款")
    @GetMapping("/charge/wx")
    public void WeixinCharge(@RequestParam String code, @RequestParam String state, HttpServletResponse servletResponse) throws Exception {
        // 微信渠道收款
        PcChannelType channel = PcChannelType.valueOf(state);
        String openId = wxBasicApi.getOauth2AccessToken(code).getOpenId();
        PcChargeRequest chargeRequest = buildChargeRequest(channel, buildChargeReturnUrl(), openId);
        PcCharge charge = payingcloud.execute(chargeRequest);
        respondCharge(channel, charge, servletResponse);
    }

    @RequestMapping("/charge/return")
    @ResponseBody
    public String onChargeReturn() {
        logger.info("跳转到支付成功页面");
        return "支付成功";
    }

    @ApiOperation("收款回调（通知）")
    @PostMapping("/charge/success")
    @ResponseBody
    public String onChargeSuccess(@RequestBody PcCharge charge, @RequestHeader String sign) {
        logger.info(sign);
        String chargeNo = charge.getChargeNo();
        logger.info("收到单号[{}]的收款成功通知", chargeNo);
        return "success";
    }

    @ApiOperation("退款")
    @GetMapping("/refund")
    public void refund(@RequestParam String chargeNo, HttpServletResponse servletResponse) throws Exception {
        String refundNo = new Date().getTime() + "";
        int amount = 1;
        PcRefundRequest refundRequest = new PcRefundRequest(chargeNo, refundNo, amount);
        refundRequest.setNotifyUrl(buildRefundNotifyUrl());
        PcRefund refund = payingcloud.execute(refundRequest);
        if (refund.getChannel() == PcChannelType.ALIPAY_DIRECT) {
            servletResponse.getWriter().println(refund.getCredentials().get("url"));
        } else {
            servletResponse.getWriter().println("退款请求已发出");
        }
        servletResponse.getWriter().flush();
    }

    @ApiOperation("退款回调")
    @PostMapping("/refund/success")
    @ResponseBody
    public String onRefundSuccess(@RequestBody PcRefund refund) {
        String refundNo = refund.getRefundNo();
        logger.info("收到单号[{}]的退款成功通知", refundNo);
        return "success";
    }

    @ApiOperation("查询收款列表")
    @GetMapping("/list/charge")
    @ResponseBody
    public PcChargeList queryChargeList(@RequestParam(required = false, defaultValue = "0") int page,
                                        @RequestParam(required = false, defaultValue = "20") int size) throws Exception {
        Date to = new Date();
        Date from = Date.from(to.toInstant().minus(30, ChronoUnit.DAYS));
        PcQueryChargeListRequest request = new PcQueryChargeListRequest(from, to, page, size);
        return payingcloud.execute(request);
    }

    @ApiOperation("查询退款列表")
    @GetMapping("/list/refund")
    @ResponseBody
    public PcRefundList queryRefundList(@RequestParam String chargeNo,
                                        @RequestParam(required = false, defaultValue = "0") int page,
                                        @RequestParam(required = false, defaultValue = "20") int size) throws Exception {
        PcQueryRefundListRequest request = new PcQueryRefundListRequest(chargeNo, page, size);
        return payingcloud.execute(request);
    }

    private PcChargeRequest buildChargeRequest(PcChannelType channel, String returnUrl, String openId) throws IOException {
        String chargeNo = new Date().getTime() + "";
        String subject = "测试商品-" + RandomStringUtils.randomNumeric(8);
        String remark = "备注";
        int amount = 1;
        String metadata = "元数据";
        PcChargeRequest request = new PcChargeRequest(chargeNo, subject, amount, channel)
                .setRemark(remark)
                .setMetadata(metadata);
        if (returnUrl != null)
            request.withExtra("returnUrl", buildChargeReturnUrl());
        if (openId != null) {
            request.withExtra("openId", openId);
        }
        request.setNotifyUrl(buildChargeNotifyUrl());
        return request;
    }

    private String buildChargeReturnUrl() {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/order/charge/return")
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private String buildChargeNotifyUrl() {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/order/charge/success")
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private String buildRefundNotifyUrl() {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/order/refund/success")
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    /**
     * 展示收银台
     */
    private void respondCharge(PcChannelType channel, PcCharge charge, HttpServletResponse servletResponse) throws IOException {
        switch (channel) {
            case BDPAY_WEB://已测通
            case BDPAY_WAP://已测通
                servletResponse.sendRedirect(charge.getCredentials().get("url"));
                break;
            case ALIPAY_APP:
                servletResponse.getWriter().write(charge.getCredentials().get("requestString"));
                break;
            case ALIPAY_DIRECT: // 已测通
            case BJPAY_WEB://已测通
            case CHINAPAY_WEB://已测通
            case JDPAY_WEB://已测通
            case JDPAY_WAP:
            case YEEPAY_WAP:
            case ALIPAY_WAP: // 已测通
            case UPACP_GATEWAY: // 已测通
                servletResponse.setContentType("text/html;charset=UTF-8");
                servletResponse.getWriter().write(charge.getCredentials().get("html"));
                break;
            case YEEPAY_WEB:
                servletResponse.setContentType("text/html;charset=gbk");
                servletResponse.getWriter().write(charge.getCredentials().get("html"));
                break;
            case BDPAY_QR://已测通
                servletResponse.sendRedirect("/pages/qrcode.html?codeImage=" + charge.getCredentials().get("url"));
                break;
            case WXPAY_APP:
                servletResponse.getWriter().write(charge.getCredentials().toString());
                break;
            case WXPAY_NATIVE: // 已测通
            case BJPAY_WX: // 已测通
            case ALIPAY_QR: // 已测通
            case KFTPAY_WX:
            case KFTPAY_ALI:
            case JDPAY_QR:
            case CMBCPAY_T1_ALI:
            case WEBANKPAY_WX_QR:
            case CMBCPAY_T0_ALI:
            case CMBCPAY_T0_WX_QR:
            case CMBCPAY_T1_WX_QR:
            case CMBCPAY_T0_QQ:
            case CMBCPAY_T1_QQ:
                servletResponse.sendRedirect("/pages/qrcode.html?codeUrl=" + charge.getCredentials().get("codeUrl"));
                break;
            case WXPAY_JSAPI: // 已测通
            case CMBCPAY_T0_WX_JSAPI:
            case CMBCPAY_T1_WX_JSAPI:
            case WEBANKPAY_WX_JSAPI:
                String params = "appId=" + charge.getCredentials().get("appId") + "&" +
                        "timeStamp=" + charge.getCredentials().get("timeStamp") + "&" +
                        "nonceStr=" + charge.getCredentials().get("nonceStr") + "&" +
                        "package=" + URLEncoder.encode(charge.getCredentials().get("package"), "UTF-8") + "&" +
                        "signType=" + charge.getCredentials().get("signType") + "&" +
                        "paySign=" + charge.getCredentials().get("paySign");
                servletResponse.sendRedirect("/weixin_jsapi_pay.html?" + params);
                break;
            case UPACP_APP: // 已测通
                servletResponse.getWriter().write(charge.getCredentials().get("tn"));
                break;
        }
    }

}
