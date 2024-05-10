package com.example.demo1228_2.controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1228_2.config.*;
import com.example.demo1228_2.dto.DelayedTaskDto;
import com.example.demo1228_2.entity.Comment;
import com.example.demo1228_2.entity.Test;
import com.example.demo1228_2.mapper.CommentMapper;
import com.example.demo1228_2.service.impl.*;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 *  专门接受外部请求 比如支付宝 微信 不被拦截器拦截
 * </p>
 *
 * @author yjz
 * @since 2024-01-17
 */
@RestController
@Slf4j // 自动生成log对象
@RequestMapping("/test")
public class TestController {

    /*
    @Autowired
    TestServiceImpl testService;

    @Autowired
    UserServiceImpl userService;

    @PostMapping("/update") //无锁
    public Boolean TestUpdate(@RequestBody Test test){
        return testService.updateById(test);
    }

    @PostMapping("/update/ids") //乐观锁 //再listByIds不生效
    public R<String> TestUpdate2(@RequestBody List<Long> testids){
        int attempt = 0;
        int maxAttempts = 1; // 最大重试次数，根据需要调整

        long threadId = Thread.currentThread().getId(); // 获取当前线程的ID

        while(attempt < maxAttempts){
            List<Test> test = testService.listByIds(testids);
            if(true){
                test.forEach(item ->item.setNum(item.getNum()-1));

                Test t = testService.getById(1759856858962604037L);
                t.setId(1759856858962604037L);
                t.setNum(99);
                // 打乱一个版本号 测试BatchById
                log.info("搅屎棍结果：{}",testService.updateById(t));
                //

                if(testService.updateBatchById(test)){
                    // 在日志中包含线程ID和版本号
                    log.info("线程ID: {}, 成功更改N条", threadId);
                    return R.success("成功更改N条");
                }else{
                    // 更新失败，增加重试次数
                    attempt++;
                    // 在日志中包含线程ID和版本号
                    log.info("线程ID: {}, 尝试重试更新...", threadId);
                    continue; // 继续下一次循环尝试更新
                }
            } else {
                // 在日志中包含线程ID和版本号
                log.info("线程ID: {}, 库存不足", threadId);
                return R.error("库存不足");
            }
        }

        // 在日志中包含线程ID和版本号
        log.info("线程ID: {}, 更新失败，达到最大重试次数", threadId);
        return R.error("更新失败，请稍后再试");
    }

    @PostMapping("/update/list") //乐观锁 //回滚测试
    public R<String> TestUpdate3(@RequestBody List<Long> testids){
        int attempt = 0;
        int maxAttempts = 3; // 最大重试次数，根据需要调整
        long threadId = Thread.currentThread().getId(); // 获取当前线程的ID

        while(attempt < maxAttempts){

            List<Test> testList = new ArrayList<>();
            for(Long item : testids) {
                Test t = testService.getById(item);
                if (t.getNum() == 0) {
                    log.info("线程{}：卖完啦",threadId);
                    return R.error("卖完了");
                }
                t.setNum(t.getNum() - 1);
                testList.add(t);
            }
            //
            //Test t = testService.getById(1759856858962604037L);
            //t.setId(1759856858962604037L);
            //t.setNum(99);
            // 打乱一个版本号 测试BatchById
            //log.info("搅屎棍结果：{}",testService.updateById(t));
            //

            // 事物回滚测试
            try{
                testService.updateByList(testList);
                log.info("线程{}：List更新成功",threadId);
                return R.success("1");
            }catch (Exception e){
                log.info("线程{}：List更新失败:{}，重新{}次尝试",threadId,e.getMessage(),attempt);
                attempt++;
            }

        }
        log.info("线程{}：尝试到最大次数",threadId);
        return R.success("0");


    }

    @PostMapping("/saveone")
    public void TestUpdate4(@RequestBody Test test){
        log.info("{}",test);
        log.info("{}",testService.save(test));
    }

    @GetMapping("/reduce/one/num")
    public Boolean TestDelete(@RequestParam Long id){
        long threadId = Thread.currentThread().getId(); // 获取当前线程的ID
        Test test = testService.getById(id);
        log.info("线程{}:数量{}版本号{}",threadId,test.getNum() , test.getVersion());
        test.setNum(test.getNum()-1);
        int attempt = 5;
        Boolean re = false;
        while(attempt>0){
            log.info("线程{}第{}次:数量{},减一结果{}，版本号{},未执行update",threadId,attempt,test.getNum(),re , test.getVersion());
            re = testService.updateById(test);
            if(re)break;
            log.info("线程{}第{}次:数量{},减一结果{}，版本号{},已执行update",threadId,attempt,test.getNum(),re , test.getVersion());
            attempt--;
        }
        log.info("{}",test.getVersion());
        test = testService.getById(id);
        if(attempt==0)log.info("线程{}:放弃尝试",threadId);
        else log.info("线程{}:成功,数量{},版本号{}",threadId,test.getNum(),test.getVersion());
        return re;
    }

    @GetMapping("/reduce/one/num2")
    public void TestDelete2(@RequestParam Long id){
        Test test = testService.getById(id);
        log.info("{}",test);
        test.setNum(test.getNum()+1);
        test.setVersion(test.getVersion()+1);
        testService.updateById(test);
        log.info("{}",test);
        test = testService.getById(id);
        log.info("{}",test);
    }

    // 这算一个比较好的
    // 乐观锁高并发范例（随机时间前置延迟）
    @GetMapping("/reduce/one/num3")
    public void TestDelete3(@RequestParam Long id){
        long threadId = Thread.currentThread().getId(); // 获取当前线程的ID
        int attempt = 3;
        Random random = new Random(); // 创建一个Random实例来生成随机数
        while(attempt>0){
            try {
                // 生成一个随机等待时间，例如在100到500毫秒之间
                int sleepTime = 100 + random.nextInt(400);
                Thread.sleep(sleepTime); // 使当前线程休眠随机时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 重新设置中断状态
                log.error("线程休眠被中断", e);
            }

            Test test = testService.getById(id);
            if(test.getNum()>0){
                test.setNum(test.getNum()-1);
                if(testService.updateById(test)){
                    log.info("线程{}次数{}：买到了G",threadId,attempt);
                    return;
                }else{
                    log.info("线程{}次数{}：被打断",threadId,attempt);
                    attempt--;
                }

            }else{
                log.info("线程{}次数{}：卖光了G",threadId,attempt);
                return;
            }
        }
        log.info("线程{}次数{}：用完了G",threadId,attempt);
    }

    @GetMapping("/getbyid")
    public Test getbyid(@RequestParam Long id){
        Test test = testService.getById(id);
        log.info("{}",test);
        return test;
    }
    */

    @Autowired
    DelayQueueService delayQueueService;

    @PostMapping("/delaytask/add") // 延迟队列测试
    public void delaytask(@RequestBody DelayedTask task){
        log.info("{}",task);
        task.setStartTime(System.currentTimeMillis()+ task.getStartTime()*1000);
        delayQueueService.addTask(task);
    }

    @GetMapping("/delaytask/get")
    public List<DelayedTaskDto> delaytask2(){
        List<DelayedTaskDto> d = delayQueueService.getTasks();
        log.info("{}",d);
        return d;
    }

    @GetMapping("/delaytask/getraw")
    public List<DelayedTask> delaytask4(){
        List<DelayedTask> d = delayQueueService.getRawTasks();
        log.info("{}",d);
        return d;
    }

    @DeleteMapping("/delaytask/delete")
    public boolean delaytask3(@RequestBody DelayedTask task){
        return delayQueueService.removeTask(task);
    }

    @Autowired
    CommentMapper commentMapper;

    @GetMapping("/fuck")
    public IPage testt() throws IOException {
        Long product_id=1L;
        Page page = new Page<>(1, 5);
        return commentMapper.selectByProductIdLeftJoinOrderByLike(page,product_id);
    }

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private AlipayProperties alipayProperties;

    /**
     * 这里好像接收前端get请求之后 返回一个重定向html 会跳转阿里支付页
     * @param model 参数
     * @return 返回一个重定向html 会跳转阿里支付页
     */
    @GetMapping("/pay")
    public String pay(AlipayTradePagePayModel model) {
        try {
            log.info("ProductCode:{}",model.getProductCode());
            log.info("Subject:{}",model.getSubject());
            // 电脑
            if(model.getProductCode().equals("FAST_INSTANT_TRADE_PAY"))
                return alipayService.pay(model);
            // 手机
            else if(model.getProductCode().equals("QUICK_WAP_WAY")) {
                AlipayTradeWapPayModel model2 = new AlipayTradeWapPayModel();
                model2.setOutTradeNo(model.getOutTradeNo());
                model2.setTotalAmount(model.getTotalAmount());
                model2.setSubject(model.getSubject());
                model2.setProductCode(model.getProductCode()); // 通常对于移动支付，产品代码设置为QUICK_WAP_WAY
                return alipayService.pay2(model2);
            }
            else{
                return "你给的什么几把参数";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "支付失败";
        }
    }



    @Autowired
    OrderServiceImpl orderService;

    @PostMapping("/notifyUrl")
    public String receiveAlipayNotification(@RequestParam Map<String, String> params) {
        log.info("接收到支付宝通知");
        // 打印所有接收到的参数
        params.forEach((key, value) -> log.info(key + ": " + value));

        try{
            // 在这里添加处理逻辑，例如验证签名、更新订单状态等
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(), alipayProperties.getSignType()); //调用SDK验证签名
            if(signVerified){
                // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                log.info("支付宝验签成功");
                // 对应order订单调成已支付
                try{
                    orderService.updateByAliPay(params);
                    log.info("商家验证订单信息，支付金额成功，确认无误");
                    return "success";
                }catch (Exception e){
                    log.info("异常:{}，返回failure",e.getMessage());
                    return "failure";
                }
            }else{
                // TODO 验签失败则记录异常日志，并在response中返回failure.
                log.info("支付宝验签失败");
                return "failure";
            }

            // 根据支付宝要求返回响应，例如"success"

        }catch (Exception e){
            log.info("发生异常：{}"+e.getMessage());
            return "failure";
        }

    }

    /**
     * 微信登录回调测试1
     */
    @GetMapping("/wechat_login")
    public Map<String, String> pay(@RequestParam Map<String, String> params) {
        log.info("测试微信！！！！！");
        // 遍历Map
        for (Map.Entry<String, String> entry : params.entrySet()) {
            log.info("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        return params;
    }


}
