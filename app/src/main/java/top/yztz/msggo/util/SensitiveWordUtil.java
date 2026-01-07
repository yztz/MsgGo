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

import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.sensitive.word.api.ISensitiveWordCharIgnore;
import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.api.IWordResultCondition;
import com.github.houbb.sensitive.word.api.context.InnerSensitiveWordContext;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.check.WordChecks;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions;
import com.github.houbb.sensitive.word.support.tag.WordTags;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utility class for sensitive word detection.
 * Wraps the houbb/sensitive-word library for easy use.
 */
public class SensitiveWordUtil implements IWordAllow, IWordDeny, ISensitiveWordCharIgnore {
    private static final String SPECIAL = "`-=~!@#$%^&*()_+[]{}\\|;:'\",./<>?！？；…：‘“”’（）";
    private static final Set<Character> SET;

    static {
        SET = StringUtil.toCharSet(SPECIAL);
    }
    private static final SensitiveWordUtil INSTANCE = new SensitiveWordUtil();
    private static final SensitiveWordBs wordBs = SensitiveWordBs.newInstance()
            .ignoreCase(true)
            .ignoreWidth(true)
            .ignoreNumStyle(true)
            .ignoreChineseStyle(true)
            .ignoreEnglishStyle(true)
            .charIgnore(INSTANCE)
            .ignoreRepeat(false)
            .enableNumCheck(false)
            .enableEmailCheck(false)
            .enableUrlCheck(false)
            .enableIpv4Check(false)
            .enableWordCheck(true)
            .wordFailFast(true)
            .wordCheckNum(WordChecks.num())
            .wordCheckEmail(WordChecks.email())
            .wordCheckUrl(WordChecks.url())
            .wordCheckIpv4(WordChecks.ipv4())
            .wordCheckWord(WordChecks.word())
            .wordTag(WordTags.none())
            .wordResultCondition(WordResultConditions.alwaysTrue())
            .wordAllow(WordAllows.chains(
                    WordAllows.defaults(),
                    INSTANCE
            ))
            .wordDeny(WordDenys.chains(
                    WordDenys.defaults(),
                    INSTANCE
            ))
//            .wordTag(WordTags.defaults())
            .init();
    public static boolean contains(String text) {
        return wordBs.contains(text);
    }


    public static List<String> findAll(String text) {
        return wordBs.findAll(text);
    }

    @Override
    public List<String> allow() {
        return Arrays.asList(
            "近日入职", "联系方式"
        );
    }

    @Override
    public List<String> deny() {
        return Arrays.asList(
            "luo聊", "衣果聊", "衣果耳卯", "贷款", "拖欠", "催收", "涉嫌", "滞纳金", "还款", "违约", "法务"
        );
    }

    @Override
    public boolean ignore(int ix, String text, InnerSensitiveWordContext innerContext) {
        return SET.contains(text.charAt(ix));
    }


//    public static String findFirst(String text) {
//        return SensitiveWordHelper.findFirst(text);
//    }


}
