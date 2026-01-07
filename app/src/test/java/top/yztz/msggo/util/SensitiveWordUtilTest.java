/*
 * Copyright (C) 2026 yztz
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package top.yztz.msggo.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

import com.github.houbb.sensitive.word.core.SensitiveWordHelper;

/**
 * Unit tests for SensitiveWordUtil.
 */
public class SensitiveWordUtilTest {

    @Test
    public void testContains_withSensitiveWord_returnsTrue() {
        String[] texts = {
            "日入10w", "luo聊", "衣果聊", "衣果耳卯", "贷款", "簧片", "杀人放火", "你妈死了",
                "傻逼", "色~@!情", "色~@!！情", "黄色视频", "枪支", "弹药", "草他妈的", "傻子", "笨蛋"
        };
        for (String text : texts) {
            assertTrue("Should detect sensitive words in text:\n\n"
                            + text + "\n\ncontains: \n\n"
                            + SensitiveWordUtil.findAll(text),
                    SensitiveWordUtil.contains(text));
        }
    }

    @Test
    public void testContains_withNormalText_returnsFalse() {
        String[] texts = {
                "have",
                "哈喽[名字]，这周末有空吗？想约你一起去[地点][做某事]，有兴趣的话跟我说声哈！",
                "[领导姓名]您好，我因[身体不适/家中有事]，今日想请假一天。工作上的急事可以随时微信/电话联系我，请批准。",
                "[姓名]您好，提醒一下我们约了[时间]开会。会议链接/地点是[信息]，期待您的参与。",
                "谢谢你的邀请！真的很想去，但我那天正好有其他安排了。下次有机会我们再约，祝你们玩得开心！",
                "[姓名]，平时忙碌少联系，但在[节日名]这一天特别想送个祝福。祝你和家人节日快乐，万事顺意！",
                "[姓名]，祝你[节日名]快乐！希望你新的一年/这个月 每天都有好心情，工作顺利，平安喜乐。",
                "谁说明信片才算心意？这条短信装满了我的祝福。祝[姓名][节日名]快乐，吃好喝好，不长胖！",
                "%s您好，我是[你的名字]。感谢您一直以来的信任。值此佳节，祝您生意兴隆，身体健康。有任何需求随时联系。",
                "各位好，很高兴能在[活动名]上认识大家。附件是本次会议的资料汇总，希望能对大家有所帮助。期待未来合作。",
                "嘿 %s，我是[你的名字]。这是我的新手机号，原号码即将停用。方便的话请存一下，保持联系哈！",
                "各位好，我搬新家啦！打算在[日期]举办个小型暖房聚会，诚邀大家来坐坐。地点：[地址]，收到的请回复一下。",
                "各位朋友好，我已于近日入职[公司名]担任[职位]。以后还请多多关照，有空常聚！",
                "%s您好，之前跟您沟通的[事项/合同]不知进度如何了？如果有需要我配合的地方请随时告知。",
                "【提醒】%s您好，明天就是[截止日期]了，关于[项目名]的资料还没收到，麻烦您抽空处理下哈。",
                "%s您好，很抱歉打扰，请问上次提到的款项是否已经安排入账？以便我们这边财务对账。",
                "【离职感谢】各位同事，今天是我在[公司名]的最后一天。感谢大家这段时间对%s的照顾，青山不改，绿水长流，江湖再见！",
                "【晋升答谢】感谢%s这段时间对我的指导和提拔，没有您的帮助我无法取得今天的进步。今后我会更加努力！",
                "各位伙伴，我已正式入职[新公司]，负责[业务]。希望能与各位展开新的合作，联系方式不变。",
                "一点小小心意，祝%s节日快乐，每天都要开心呀！",
                "【入伙礼】祝贺%s乔迁新居！祝新家新气象，日子红红火火！",
                "恭喜%s喜添贵子/千金！祝小宝贝健康成长，聪明伶俐。",
                "%s，好久没聚了，这周末有空一起出来吃个饭吗？位置你挑，我买单！",
                "呼叫%s！最近发现一家超赞的[餐厅类型]，有没有兴趣去打个卡？",
                "%s，今晚我们几个老伙计打算小聚一下，在[地点]，能来不？等你好消息。",
                "尊敬的（${姓名}同学）家长：我是江苏XXXXXXX学校XXXXXX年级3班班主任张三，您可以通过短信或者电话与我们联系了解学生在校情况。班主任电：张三  15111111111    办公室电话：0111-22222222 期待通过我们的共同努力，家校携手，为孩子的成长保驾护航。祝您和您的家庭身体健康，生活如意！收到请回复一下，谢谢！",
                "【%s社团】同学你好！恭喜你通过初筛，请于%s前往%s参加面试，期待你的表现！",
                "【%s】终于等到你！恭喜你正式成为我们的一员。请准时参加今晚%s的迎新会，不见不散！",
                "嘿！想让大学生活更精彩吗？%s招新倒计时最后24小时！报名地点：%s，等一个有才华的你。",
                "【会议通知】请%s全体成员于%s在%s准时开会，讨论%s相关事宜，收到请回复。",
                "【重要通知】关于%s活动的策划案已发至群文件，请各位于%s前查看并反馈意见。——%s负责人",
                "提醒：各部门部长请于今日%s到%s领取活动物资，收到请回复确认。",
                "【紧急变动】各位同学，原定于%s举办的%s活动因天气/场地原因改至%s举行，请互相转告！",
                "【提醒】%s活动即将于一小时后在%s开始，请工作人员提前20分钟到场签到。",
                "不好意思打扰大家，由于临时状况，今晚的%s取消，具体补办时间另行通知，请见谅。",
                "【感谢】本次%s活动圆满结束！感谢%s每位小伙伴的辛苦付出，大家辛苦了，早点休息！",
                "活动复盘提醒：请各位成员在%s前将%s总结发送至邮箱，大家的反馈对我们很重要。",
                "精彩瞬间：%s活动的合影已上传，感谢%s的参与，期待下一次更精彩的相遇！",
        };
        for (String text : texts) {
            assertFalse("Should not detect sensitive words in normal text:\n\n"
                            + text + "\n\ncontains: \n\n"
                            + SensitiveWordUtil.findAll(text),
                    SensitiveWordUtil.contains(text));
        }
    }

    @Test
    public void testContains_withEmptyString_returnsFalse() {
        assertFalse("Empty string should return false", SensitiveWordUtil.contains(""));
    }

    @Test
    public void testContains_withNull_returnsFalse() {
        assertFalse("Null should return false", SensitiveWordHelper.contains(null));
    }

    @Test
    public void testFindAll_withMultipleSensitiveWords_returnsAll() {
        String text = "中奖";
        List<String> words = SensitiveWordUtil.findAll(text);
        
        assertNotNull("Result should not be null", words);
        assertFalse("Should find sensitive words", words.isEmpty());
        System.out.println("Found sensitive words: " + words);
    }

    @Test
    public void testFindAll_withNormalText_returnsEmptyList() {
        String text = "[对方姓名/称呼]您好，衷心感谢您一直以来的支持与信任。祝您及家人节日快乐，事业蒸蒸日上，阖家幸福安康！";
        List<String> words = SensitiveWordUtil.findAll(text);
        
        assertNotNull("Result should not be null", words);
        assertTrue("Should return empty list for normal text", words.isEmpty());
    }

//    @Test
//    public void testFindFirst_withSensitiveWord_returnsFirst() {
//        String text = "中奖";
//        String first = SensitiveWordUtil.findFirst(text);
//
//        assertNotNull("Should find first sensitive word", first);
//        System.out.println("First sensitive word: " + first);
//    }

//    @Test
//    public void testFindFirst_withNormalText_returnsNull() {
//        String text = "普通文本内容";
//        String first = SensitiveWordUtil.findFirst(text);
//
//        // findFirst returns empty string when no match, not null
//        assertTrue("Should return null or empty for normal text",
//                   first == null || first.isEmpty());
//    }
}
