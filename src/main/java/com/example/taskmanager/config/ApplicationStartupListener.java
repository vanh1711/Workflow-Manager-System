package com.example.taskmanager.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lắng nghe sự kiện {@link ApplicationReadyEvent} khi ứng dụng Spring Boot đã khởi động hoàn tất.
 * 
 * Mục đích:
 * 1. In khối banner rõ ràng với đầy đủ đường dẫn truy cập (Clickable URLs) ngay trên Terminal.
 * 2. Cho phép người dùng nhấn [Ctrl + Click] để mở trực tiếp hoặc sao chép nhanh chóng.
 * 3. Tự động kích hoạt mở trình duyệt đến trang Bảng điều khiển (Dashboard) khi ở môi trường phát triển (Dev).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupListener {

    private static final AtomicBoolean BROWSER_OPENED = new AtomicBoolean(false);

    private final Environment environment;

    @Value("${app.browser.auto-open:true}")
    private boolean autoOpenBrowser;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("local.server.port", environment.getProperty("server.port", "8080"));
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        if (contextPath == null || "/".equals(contextPath)) {
            contextPath = "";
        }

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ignored) {
        }

        String baseUrl = "http://localhost:" + port + contextPath;
        String lanBaseUrl = "http://" + hostAddress + ":" + port + contextPath;

        String dashboardUrl = baseUrl + "/dashboard";
        String kanbanUrl = baseUrl + "/tasks/kanban";
        String calendarUrl = baseUrl + "/tasks/calendar";
        String taskListUrl = baseUrl + "/tasks";
        String categoriesUrl = baseUrl + "/categories";
        String apiUrl = baseUrl + "/api/v1/tasks";

        printStartupBanner(dashboardUrl, kanbanUrl, calendarUrl, taskListUrl, categoriesUrl, apiUrl, lanBaseUrl);

        tryOpenBrowser(dashboardUrl);
    }

    private void printStartupBanner(
        String dashboardUrl,
        String kanbanUrl,
        String calendarUrl,
        String taskListUrl,
        String categoriesUrl,
        String apiUrl,
        String lanBaseUrl
    ) {
        String border = "=".repeat(86);
        String divider = "-".repeat(86);

        String banner = "\n" +
            border + "\n" +
            "  🚀 TASKFLOW - HỆ THỐNG QUẢN LÝ TIẾN ĐỘ & DỰ ÁN ĐÃ KHỞI ĐỘNG THÀNH CÔNG!\n" +
            border + "\n" +
            "  👉 Bảng điều khiển (Dashboard):  " + dashboardUrl + "\n" +
            "  👉 Bảng Kanban trực quan:        " + kanbanUrl + "\n" +
            "  👉 Lịch công việc (Calendar):    " + calendarUrl + "\n" +
            "  👉 Danh sách công việc (List):   " + taskListUrl + "\n" +
            "  👉 Danh mục dự án (Categories):  " + categoriesUrl + "\n" +
            "  👉 REST API Endpoint:            " + apiUrl + "\n" +
            divider + "\n" +
            "  🌐 Truy cập mạng nội bộ (LAN):   " + lanBaseUrl + "/dashboard\n" +
            divider + "\n" +
            "  💡 Mẹo thao tác trong Antigravity IDE / VS Code:\n" +
            "     • Giữ phím [Ctrl] và Click chuột trái vào link bất kỳ ở trên để mở ngay lập tức!\n" +
            "     • Hoặc quét chọn đường link để sao chép (Ctrl + C).\n" +
            border + "\n";

        System.out.println(banner);
    }

    private void tryOpenBrowser(String url) {
        if (!autoOpenBrowser || isTestEnvironment()) {
            return;
        }

        // Đảm bảo chỉ mở trình duyệt 1 lần khi tiến trình khởi động, không mở lặp lại khi DevTools reload
        if (!BROWSER_OPENED.compareAndSet(false, true)) {
            return;
        }

        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            log.debug("Không thể tự động mở trình duyệt: {}", ex.getMessage());
        }
    }

    private boolean isTestEnvironment() {
        return System.getProperty("surefire.test.class.path") != null
            || System.getProperty("sun.java.command", "").contains("surefire")
            || System.getProperty("test") != null;
    }
}
