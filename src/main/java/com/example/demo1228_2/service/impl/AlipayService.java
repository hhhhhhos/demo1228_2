package com.example.demo1228_2.service.impl;

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
import com.example.demo1228_2.config.AlipayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j // 自动生成log对象
public class AlipayService {

    @Autowired
    private AlipayProperties alipayProperties;

    /**
     * 网页支付
     * @param model 1
     * @return 1
     * @throws AlipayApiException 1
     */
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
        //手机网站支付默认传值QUICK_WAP_WAY
        //bizContent.put("product_code", "QUICK_WAP_WAY");

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
        log.info("bizContent:{}",bizContent);

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

    /**
     * 手机支付测试
     * @param model 1
     * @return 1
     * @throws AlipayApiException 1
     */
    public String pay2(AlipayTradeWapPayModel model) throws AlipayApiException{
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayProperties.getServerUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
                );
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        //异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        //同步跳转地址，仅支持http/https
        request.setReturnUrl(alipayProperties.getReturnUrl());
        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", model.getOutTradeNo());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", model.getTotalAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", model.getSubject());

        /******可选参数******/
        //手机网站支付默认传值QUICK_WAP_WAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
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
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request,"POST");
        // 如果需要返回GET请求，请使用
        // AlipayTradeWapPayResponse response = alipayClient.pageExecute(request,"GET");
        //String pageRedirectionData = response.getBody();
        //System.out.println(pageRedirectionData);

        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            throw new AlipayApiException("支付失败：" + response.getSubMsg()); // 返回错误提示
        }

        return response.getBody();
    }
}
