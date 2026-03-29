package com.clawdash.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentSelectorTest {

    private AgentSelector service;

    @BeforeEach
    void setUp() {
        service = new AgentSelector();
    }

    @Test
    void selectAgent_EngineeringKeywords() {
        assertEquals("gongbu", service.selectAgent("修一下登录bug"));
    }

    @Test
    void selectAgent_CodeKeywords() {
        assertEquals("gongbu", service.selectAgent("写一段代码"));
    }

    @Test
    void selectAgent_FinanceKeywords() {
        assertEquals("hubu", service.selectAgent("分析本月财务报表"));
    }

    @Test
    void selectAgent_MoneyKeywords() {
        assertEquals("hubu", service.selectAgent("计算预算"));
    }

    @Test
    void selectAgent_SecurityKeywords() {
        assertEquals("bingbu", service.selectAgent("检查系统安全漏洞"));
    }

    @Test
    void selectAgent_LegalKeywords() {
        assertEquals("xingbu", service.selectAgent("合规检查"));
    }

    @Test
    void selectAgent_DocumentKeywords() {
        assertEquals("libu3", service.selectAgent("编写文档"));
    }

    @Test
    void selectAgent_PersonnelKeywords() {
        assertEquals("libu4", service.selectAgent("处理人员信息"));
    }

    @Test
    void selectAgent_UnknownTask_Fallback() {
        assertEquals("gongbu", service.selectAgent("做点什么"));
    }
}
