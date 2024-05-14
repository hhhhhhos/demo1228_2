package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.GlobalProperties;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.dto.OpenAiJsonMessageObject;
import com.example.demo1228_2.dto.OpenAiSendJson;
import com.example.demo1228_2.entity.KefuChatHistory;
import com.example.demo1228_2.mapper.KefuChatHistoryMapper;
import com.example.demo1228_2.service.impl.HttpService;
import com.example.demo1228_2.service.impl.KefuChatHistoryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yjz
 * @since 2024-05-13
 */
@Slf4j
@RestController
@RequestMapping("/kefu")
public class KefuChatHistoryController {

    @Autowired
    HttpService httpService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GlobalProperties globalProperties;

    @Autowired
    KefuChatHistoryServiceImpl kefuChatHistoryService;

    /**
     * 查询有无已发起且未结束聊天 有则返回对话列表 chat_id 无则返回null
     * @return OpenAiSendJson
     */
    @GetMapping("/chat")
    public R<List<OpenAiJsonMessageObject>> testt2(HttpSession session){
        KefuChatHistory kefuChatHistory = Db.lambdaQuery(KefuChatHistory.class)
                .eq(KefuChatHistory::getUser_id, Tool.getUserSessionId(session))
                .eq(KefuChatHistory::getIs_finish,false)
                .one();
        // 不存在已发起且未结束聊天 返回null
        if(kefuChatHistory==null)
            return R.success(null);

        List<OpenAiJsonMessageObject> message = kefuChatHistory.getSend_json().getMessages();
        message.remove(0); // 删除系统提示词返回
        return R.success(message).add("chat_id",kefuChatHistory.getId().toString())
                .add("kefu_name",kefuChatHistory.getKefu_name());
    }

    /**
     * Openai 发起聊天请求
     * @param user_send_message_object 1
     * @return 1
     */

    @PostMapping("/chat")
    public R<Map<String, Object>> testt(@RequestBody OpenAiJsonMessageObject user_send_message_object,
                                        @RequestParam String chat_id, HttpSession session){
        // 这里！！记得限制每个用户的速率 请求次数。。 防止高频请求耗尽key
        // 创建空请求JSON
        OpenAiSendJson openAiSendJson = new OpenAiSendJson();
        // 选模型 gpt-3.5-turbo（如果没从数据库获取，新建的话）
        openAiSendJson.setModel("gpt-3.5-turbo");
        // 测试用的，获取客服名
        String kefu_name="";
        // 创建空对话列表
        List<OpenAiJsonMessageObject> openAiJsonMessageObjectList = new ArrayList<>();
        try {
            // 校验客户user_send_message_object输入
            if(!user_send_message_object.getRole().equals("user"))
                throw new CustomException("角色错误");
            if(user_send_message_object.getContent().length()>=50)
                throw new CustomException("内容过长");
            if(user_send_message_object.getContent().length()==0)
                throw new CustomException("内容为空");
            if(chat_id!=null && !chat_id.equals("null") &&kefuChatHistoryService.getById(Long.parseLong(chat_id)).getIs_finish())
                throw new CustomException("此轮对话已结束");

            // 查询有无已发起且未结束聊天
            KefuChatHistory kefuChatHistory = Db.lambdaQuery(KefuChatHistory.class)
                    .eq(KefuChatHistory::getUser_id, Tool.getUserSessionId(session))
                    .eq(KefuChatHistory::getIs_finish,0)
                    .one();

            // 不存在已发起且未结束聊天
            if(kefuChatHistory==null){
                // 构造kefuChatHistory
                kefuChatHistory = new KefuChatHistory();
                kefuChatHistory.setUser_id(Tool.getUserSessionId(session));
                // 构造系统提示词对象(获取随机客服提示词)
                Map<String,Object> random_kefu_resultMap = get_random_kefu();
                kefu_name = random_kefu_resultMap.get("kefu_name").toString();
                kefuChatHistory.setKefu_name(kefu_name);
                OpenAiJsonMessageObject openAiJsonMessageObject = objectMapper.convertValue(random_kefu_resultMap.get("openAiJsonMessageObject"), OpenAiJsonMessageObject.class);
                // 头插入系统提示词
                openAiJsonMessageObjectList.add(0,openAiJsonMessageObject);
                // 再插入用户输入
                openAiJsonMessageObjectList.add(user_send_message_object);
                // 请求体构造消息列表
                openAiSendJson.setMessages(openAiJsonMessageObjectList);

            // 存在已发起且未结束聊天
            }else{
                // 直接获取请求体
                openAiSendJson = kefuChatHistory.getSend_json();
                // 获取对话列表
                openAiJsonMessageObjectList = openAiSendJson.getMessages();
                // 大于等于10次抛异常(一般不可能出现Is_finish为true 但没设置)
                if(openAiJsonMessageObjectList.size()>=10)throw new CustomException("数据库数据异常");
                // 8次 + 这次问 + 返回答 = 10 所以 设置Is_finish为true(忘算提示词 8+1=9)
                if(openAiJsonMessageObjectList.size()==9)kefuChatHistory.setIs_finish(true);
                // 请求体构造消息列表
                openAiJsonMessageObjectList.add(user_send_message_object);

            }

            // 提交post请求 // 接收响应体
            log.info(""+openAiSendJson);
            Map<String,String> http_result = httpService.sendPostToOpenAI(globalProperties.OPENAI_KEY
                    ,objectMapper.writeValueAsString(openAiSendJson));
            // 返回码不为200 表示错误 抛异常
            if(!http_result.get("statusCode").equals("200"))
                throw new CustomException("openai返回错误:"+http_result.get("body"));
            Map<String,Object> body = objectMapper.readValue(http_result.get("body"),Map.class);
            // 解析响应体
            List<Object> list = (List<Object>) body.get("choices");
            Map<String, Object> map = (Map<String, Object>) list.get(0);
            Map<String, Object> answer = (Map<String, Object>) map.get("message");
            // sendjson加上gpt返回的对话Object
            openAiSendJson.getMessages().add(objectMapper.convertValue(answer, OpenAiJsonMessageObject.class));
            kefuChatHistory.setSend_json(openAiSendJson);

            // region数据库更新(或创建)kefuChatHistory
            // 创建
            if(kefuChatHistory.getId()==null){
                kefuChatHistory.setIs_finish(false);
                if(!kefuChatHistoryService.save(kefuChatHistory))
                    throw new CustomException("数据库创建kefuChatHistory失败");
            // 更新
            }else{
                if(!kefuChatHistoryService.updateById(kefuChatHistory))
                    throw new CustomException("数据库更新kefuChatHistory失败");
            }
            // endregion

            return R.success(answer).add("kefu_name",kefu_name).add("chat_id",kefuChatHistory.getId().toString());


        }catch (Exception e){
            log.info(e.getMessage());
            return R.error(e.getMessage());
        }
    }

    /**
     * 随机客服提示词 返回Map<String,Object> 1openAiJsonMessageObject 2kefu_name
     * @return 返回Map<String,Object> 1openAiJsonMessageObject 2kefu_name
     */
    private Map<String,Object> get_random_kefu(){
        // 结果集Map
        Map<String,Object> resultMap = new HashMap<>();
        // 生成m-n-1位随机数 [n,m)
        int randint = ThreadLocalRandom.current().nextInt(0, 3);
        // 数字对应的客服名字
        List<String> kefu_name = Arrays.asList("Doge客服", "猫娘客服", "天才客服");
        // 随机构造系统提示词对象
        OpenAiJsonMessageObject openAiJsonMessageObject = new OpenAiJsonMessageObject();

        switch (randint) {
            case 0 -> { // 混混电子商城客服
                openAiJsonMessageObject.setRole("system");
                openAiJsonMessageObject.setContent("你是一个Doge,不管别人问你什么，你只会回答‘汪汪汪！汪汪？汪!汪’诸如此类，还有emoji表情");
            }
            case 1 -> { // 猫娘电子商城客服
                openAiJsonMessageObject.setRole("system");
                openAiJsonMessageObject.setContent("你是一个猫娘电子商城客服，可爱并且每次对话结尾都会加上‘喵喵~’和自定义颜文字或者表情比如\uD83D\uDE04\uD83D\uDE0B\uD83D\uDE3C");
            }
            default -> { // 傻子电子商城客服
                openAiJsonMessageObject.setRole("system");
                openAiJsonMessageObject.setContent("你是一个问啥都只会傻笑，说话结巴，回答不知道的傻子电子商城客服");
            }
        }

        resultMap.put("openAiJsonMessageObject",openAiJsonMessageObject);
        resultMap.put("kefu_name",kefu_name.get(randint));

        return resultMap;
    }

}
