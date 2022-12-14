package cn.wenhe9.ggkt.wechat.service.impl;

import cn.wenhe9.ggkt.vod.entity.Course;
import cn.wenhe9.ggkt.wechat.feign.CourseFeignClient;
import cn.wenhe9.ggkt.wechat.service.MessageService;
import cn.wenhe9.ggkt.wechat.utils.SHA1;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;

/**
 * @author DuJinliang
 * 2022/08/22
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    private static final String TOKEN = "ggkt";

    @Resource
    private CourseFeignClient courseFeignClient;

    @Resource
    private WxMpService wxMpService;

    @Override
    public String verifyToken(HttpServletRequest request) {
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        log.info("signature: {} nonce: {} echostr: {} timestamp: {}", signature, nonce, echostr, timestamp);
        if (this.checkSignature(signature, timestamp, nonce)) {
            log.info("token ok");
            return echostr;
        }
        return echostr;
    }

    @Override
    public String receiveMessage(HttpServletRequest request) throws Exception {
        Map<String, String> param = this.parseXml(request);
        String content = "";
        try {
            String msgType = param.get("MsgType");
            switch(msgType){
                case "text" :
                    content = this.search(param);
                    break;
                case "event" :
                    String event = param.get("Event");
                    String eventKey = param.get("EventKey");
                    if("subscribe".equals(event)) {//???????????????
                        content = this.subscribe(param);
                    } else if("unsubscribe".equals(event)) {//?????????????????????
                        content = this.unsubscribe(param);
                    } else if("CLICK".equals(event) && "aboutUs".equals(eventKey)){
                        content = this.aboutUs(param);
                    } else {
                        content = "success";
                    }
                    break;
                default:
                    content = "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
            content = this.text(param, "????????????????????????????????????????????????????????????").toString();
        }
        return content;
    }

    @Override
    public void pushPayMessage(long id) throws WxErrorException {
        String openid = "o3oBv5_WPoWj5y_1nyPejhN66EOc";
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//??????????????????openid
                .templateId("vZ4QM9ZhWDbrZqWuLEsRJXnBrbZ37u56HTd50txNMRs")//??????id
                .url("http://mobile.wenhe9.cn/#/pay/"+id)//????????????????????????????????????
                .build();
        //3,??????????????????????????????????????????????????????????????????
        templateMessage.addData(new WxMpTemplateData("first", "???????????????????????????????????????????????????", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", "1314520", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", "java????????????", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", "2022-01-11", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", "100", "#272727"));
        templateMessage.addData(new WxMpTemplateData("remark", "??????????????????????????????????????????????????????", "#272727"));
        String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        System.out.println(msg);
    }


    private Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        InputStream inputStream = request.getInputStream();
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();
        for (Element e : elementList) {
            map.put(e.getName(), e.getText());
        }
        inputStream.close();
        inputStream = null;
        return map;
    }

    private boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] str = new String[]{TOKEN, timestamp, nonce};
        //??????
        Arrays.sort(str);
        //???????????????
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            buffer.append(str[i]);
        }
        //??????sha1??????
        String temp = SHA1.encode(buffer.toString());
        //??????????????????signature????????????
        return signature.equals(temp);
    }

    /**
     * ???????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????1?????????????????????????????????????????????8???????????????
     */
    private String search(Map<String, String> param) {
        String fromusername = param.get("FromUserName");
        String tousername = param.get("ToUserName");
        String content = param.get("Content");
        //???????????????????????????
        Long createTime = new Date().getTime() / 1000;
        StringBuffer text = new StringBuffer();
        List<Course> courseList = courseFeignClient.findCourseByKeyword(content);
        if(CollectionUtils.isEmpty(courseList)) {
            text = this.text(param, "????????????????????????????????????????????????????????????");
        } else {
            //????????????????????????
            Random random = new Random();
            int num = random.nextInt(courseList.size());
            Course course = courseList.get(num);
            StringBuffer articles = new StringBuffer();
            articles.append("<item>");
            articles.append("<Title><![CDATA["+course.getTitle()+"]]></Title>");
            articles.append("<Description><![CDATA["+course.getTitle()+"]]></Description>");
            articles.append("<PicUrl><![CDATA["+course.getCover()+"]]></PicUrl>");
            articles.append("<Url><![CDATA[http://glkt.atguigu.cn/#/liveInfo/"+course.getId()+"]]></Url>");
            articles.append("</item>");

            text.append("<xml>");
            text.append("<ToUserName><![CDATA["+fromusername+"]]></ToUserName>");
            text.append("<FromUserName><![CDATA["+tousername+"]]></FromUserName>");
            text.append("<CreateTime><![CDATA["+createTime+"]]></CreateTime>");
            text.append("<MsgType><![CDATA[news]]></MsgType>");
            text.append("<ArticleCount><![CDATA[1]]></ArticleCount>");
            text.append("<Articles>");
            text.append(articles);
            text.append("</Articles>");
            text.append("</xml>");
        }
        return text.toString();
    }

    /**
     * ????????????
     */
    private StringBuffer text(Map<String, String> param, String content) {
        String fromusername = param.get("FromUserName");
        String tousername = param.get("ToUserName");
        //???????????????????????????
        Long createTime = new Date().getTime() / 1000;
        StringBuffer text = new StringBuffer();
        text.append("<xml>");
        text.append("<ToUserName><![CDATA["+fromusername+"]]></ToUserName>");
        text.append("<FromUserName><![CDATA["+tousername+"]]></FromUserName>");
        text.append("<CreateTime><![CDATA["+createTime+"]]></CreateTime>");
        text.append("<MsgType><![CDATA[text]]></MsgType>");
        text.append("<Content><![CDATA["+content+"]]></Content>");
        text.append("</xml>");
        return text;
    }

    /**
     * ????????????
     */
    private String aboutUs(Map<String, String> param) {
        return this.text(param, "?????????????????????Java???HTML5??????+??????????????????????????????UI/UE???????????????????????????????????????+Python????????????Android+HTML5?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????IT???????????????").toString();
    }

    /**
     * ??????????????????
     */
    private String subscribe(Map<String, String> param) {
        //????????????
        return this.text(param, "????????????????????????????????????????????????????????????????????????????????????????????????JAVA?????????Spring boot???????????????").toString();
    }

    /**
     * ????????????????????????
     */
    private String unsubscribe(Map<String, String> param) {
        //????????????
        return "success";
    }
}
