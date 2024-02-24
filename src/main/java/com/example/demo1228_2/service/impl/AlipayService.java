package com.example.demo1228_2.config;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j // 自动生成log对象
public class AlipayService {

    @Autowired
    private AlipayProperties alipayProperties;

    public String pay(AlipayTradePagePayModel model) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayProperties.getServerUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
        );
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 设置异步通知地址
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        log.info(alipayProperties.getNotifyUrl());
        // 设置同步跳转地址
        request.setReturnUrl(alipayProperties.getReturnUrl());
        log.info(alipayProperties.getReturnUrl());

        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", model.getOutTradeNo());
        log.info(model.getOutTradeNo());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", model.getTotalAmount());
        log.info(model.getTotalAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", model.getSubject());
        log.info(model.getSubject());
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        /******可选参数******/
        //bizContent.put("time_expire", "2022-08-01 22:00:00");

        //// 商品明细信息，按需传入
        //JSONArray goodsDetail = new JSONArray();
        //JSONObject goods1 = new JSONObject();
        //goods1.put("goods_id", "goodsNo1");
        //goods1.put("goods_name", "子商品1");
        //goods1.put("quantity", 1);
        //goods1.put("price", 0.01);
        //goodsDetail.add(goods1);
        //bizContent.put("goods_detail", goodsDetail);

        //// 扩展信息，按需传入
        //JSONObject extendParams = new JSONObject();
        //extendParams.put("sys_service_provider_id", "2088511833207846");
        //bizContent.put("extend_params", extendParams);

        request.setBizContent(bizContent.toString());

        // 发起GET请求
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");

        if (response.isSuccess()) {
            System.out.println("调用成功");
            // 返回整个页面的HTML内容
            return response.getBody();
        } else {
            System.out.println("调用失败");
            throw new AlipayApiException("支付失败：" + response.getSubMsg()); // 返回错误提示
        }
    }
}
