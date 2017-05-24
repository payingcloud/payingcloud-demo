package cn.payingcloud.demo.controller;

import cn.payingcloud.PayingCloud;
import cn.payingcloud.model.PcChannelList;
import cn.payingcloud.model.PcQueryChannelListRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author YQ.Huang
 */
@Api(tags = "channel", description = " ")
@Controller
@RequestMapping("/channels")
public class ChannelController {

    private final PayingCloud payingcloud;

    public ChannelController(PayingCloud payingcloud) {
        this.payingcloud = payingcloud;
    }

    @ApiOperation("获取渠道列表")
    @GetMapping
    @ResponseBody
    public PcChannelList getChannels() {
        PcQueryChannelListRequest request = new PcQueryChannelListRequest();
        return payingcloud.execute(request);
    }
}
