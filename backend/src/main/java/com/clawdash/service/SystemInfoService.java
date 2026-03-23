package com.clawdash.service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * System information service - retrieves real system metrics via JVM ManagementFactory
 */
@Service
public class SystemInfoService {

    /**
     * Get comprehensive system information including CPU, memory, JVM metrics
     */
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        // Operating System metrics
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        info.put("osName", osBean.getName());
        info.put("osVersion", osBean.getVersion());
        info.put("osArch", osBean.getArch());
        info.put("availableProcessors", osBean.getAvailableProcessors());

        // CPU usage (system load average)
        double systemLoadAverage = osBean.getSystemLoadAverage();
        info.put("systemLoadAverage", systemLoadAverage);
        
        // CPU count for percentage calculation
        int processors = osBean.getAvailableProcessors();
        if (systemLoadAverage >= 0) {
            double cpuUsage = (systemLoadAverage / processors) * 100;
            info.put("cpuUsage", Math.min(100, Math.round(cpuUsage * 10) / 10.0));
        } else {
            // macOS doesn't support system load average, use a placeholder
            info.put("cpuUsage", -1);
        }

        // Memory metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        info.put("memoryTotal", totalMemory / (1024 * 1024)); // MB
        info.put("memoryUsed", usedMemory / (1024 * 1024)); // MB
        info.put("memoryFree", freeMemory / (1024 * 1024)); // MB
        info.put("memoryMax", maxMemory / (1024 * 1024)); // MB
        info.put("memoryUsagePercent", Math.round((double) usedMemory / totalMemory * 100 * 10) / 10.0);

        // JVM metrics
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        info.put("jvmUptime", runtimeBean.getUptime()); // milliseconds
        info.put("jvmUptimeFormatted", formatUptime(runtimeBean.getUptime()));
        info.put("jvmName", runtimeBean.getVmName());
        info.put("jvmVersion", runtimeBean.getVmVersion());
        info.put("jvmVendor", runtimeBean.getVmVendor());

        // Thread metrics
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        info.put("threadCount", threadBean.getThreadCount());
        info.put("peakThreadCount", threadBean.getPeakThreadCount());
        info.put("daemonThreadCount", threadBean.getDaemonThreadCount());

        // GC metrics
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalGcCount = 0;
        long totalGcTime = 0;
        for (GarbageCollectorMXBean gc : gcBeans) {
            totalGcCount += gc.getCollectionCount();
            totalGcTime += gc.getCollectionTime();
        }
        info.put("gcCount", totalGcCount);
        info.put("gcTime", totalGcTime); // milliseconds
        info.put("gcCountPerMinute", totalGcCount > 0 && runtimeBean.getUptime() > 0 
            ? Math.round((double) totalGcCount / (runtimeBean.getUptime() / 60000) * 10) / 10.0 
            : 0);

        // Disk info (workspace directory)
        String userHome = System.getProperty("user.home");
        File openclawDir = new File(userHome, ".openclaw");
        info.put("openclawDir", openclawDir.getAbsolutePath());
        info.put("openclawDirExists", openclawDir.exists());
        if (openclawDir.exists()) {
            info.put("openclawDirSize", getDirectorySize(openclawDir) / (1024 * 1024)); // MB
        }

        info.put("timestamp", java.time.LocalDateTime.now().toString());

        return info;
    }

    /**
     * Get simple system metrics for dashboard display
     */
    public Map<String, Object> getSimpleMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // CPU
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double systemLoadAverage = osBean.getSystemLoadAverage();
        int processors = osBean.getAvailableProcessors();
        if (systemLoadAverage >= 0) {
            double cpuUsage = (systemLoadAverage / processors) * 100;
            metrics.put("cpuUsage", Math.min(100, Math.round(cpuUsage * 10) / 10.0));
        } else {
            metrics.put("cpuUsage", -1);
        }

        // Memory
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        metrics.put("memoryUsed", usedMemory / (1024 * 1024)); // MB
        metrics.put("memoryTotal", totalMemory / (1024 * 1024)); // MB
        metrics.put("memoryUsagePercent", Math.round((double) usedMemory / totalMemory * 100 * 10) / 10.0);

        // JVM Uptime
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        metrics.put("jvmUptime", runtimeBean.getUptime());
        metrics.put("jvmUptimeFormatted", formatUptime(runtimeBean.getUptime()));

        return metrics;
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    private long getDirectorySize(File dir) {
        long size = 0;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    }
                }
            }
        }
        return size;
    }
}
