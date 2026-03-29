package com.clawdash.service;

import com.clawdash.dto.ParsedTask;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskCommandParser {

    private static final Pattern TASK_PREFIX = Pattern.compile("^#task\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Set<String> URGENT_KEYWORDS = new HashSet<>(Arrays.asList(
            "挂了", "崩了", "挂", "崩", "urgent", "紧急", "严重", "宕机"
    ));
    
    private static final Set<String> HIGH_KEYWORDS = new HashSet<>(Arrays.asList(
            "一直", "总是", "经常", "频繁", "尽快", "抓紧", "快点", "重要"
    ));
    
    private static final Set<String> DECOMPOSE_KEYWORDS = new HashSet<>(Arrays.asList(
            "拆解", "分解", "拆分", "细分", "拆成"
    ));

    public boolean isTaskCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return TASK_PREFIX.matcher(input.trim()).find();
    }

    public ParsedTask parse(String input) {
        if (!isTaskCommand(input)) {
            return null;
        }

        ParsedTask result = new ParsedTask();
        result.setRawInput(input.trim());

        String content = TASK_PREFIX.matcher(input.trim()).replaceFirst("").trim();
        
        result.setExplicitDecomposeRequested(detectDecomposeRequest(content));
        result.setPriority(detectPriority(content));
        
        String cleanedContent = content;
        for (String keyword : DECOMPOSE_KEYWORDS) {
            cleanedContent = cleanedContent.replace(keyword, "");
        }
        cleanedContent = cleanedContent.replaceAll("[,，。！!？?]", " ").trim();
        cleanedContent = cleanedContent.replaceAll("\\s+", " ").trim();
        
        int titleEnd = findTitleEnd(cleanedContent);
        if (titleEnd > 0 && titleEnd < cleanedContent.length()) {
            result.setTitle(cleanedContent.substring(0, titleEnd).trim());
            result.setDescription(cleanedContent.substring(titleEnd).trim());
        } else {
            result.setTitle(cleanedContent);
            result.setDescription("");
        }

        return result;
    }

    private String detectPriority(String content) {
        String lowerContent = content.toLowerCase();
        
        for (String keyword : URGENT_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                return "urgent";
            }
        }
        
        for (String keyword : HIGH_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                return "high";
            }
        }
        
        return "medium";
    }

    private boolean detectDecomposeRequest(String content) {
        for (String keyword : DECOMPOSE_KEYWORDS) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int findTitleEnd(String content) {
        int[] delimiters = {content.indexOf("，"), content.indexOf(","), content.indexOf("。")};
        
        int minPos = -1;
        for (int pos : delimiters) {
            if (pos > 0 && (minPos == -1 || pos < minPos)) {
                minPos = pos;
            }
        }
        
        return minPos;
    }
}
