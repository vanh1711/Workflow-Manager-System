package com.example.taskmanager.config;

import com.example.taskmanager.entity.Category;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Thành phần nạp dữ liệu mẫu ban đầu (Data Seeder / Initializer).
 *
 * Tiêu chí thiết kế cho môi trường Doanh nghiệp / Ngân hàng:
 * - Chỉ kích hoạt ở profile "dev" (@Profile("dev")) để không bao giờ ghi đè dữ liệu trên Production.
 * - Cho phép tắt trong môi trường kiểm thử tự động qua thuộc tính "app.seeder.enabled=false".
 * - Kiểm tra tính toàn vẹn và thực hiện Idempotent: nếu đã có dữ liệu người dùng trong database thì bỏ qua.
 * - Cung cấp tập dữ liệu thực tế thuộc nghiệp vụ Ngân hàng / FinTech (Core Banking, Cổng thanh toán,
 *   Giám sát gian lận, Hạ tầng DevOps) bao phủ đủ 4 trạng thái vòng đời (TODO, IN_PROGRESS, REVIEW, DONE),
 *   các mức độ ưu tiên và các trường hợp công việc quá hạn để người phỏng vấn có thể kiểm tra trực tiếp.
 */
@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("[DataSeeder] Cơ sở dữ liệu đã có dữ liệu mẫu. Cập nhật mật khẩu chuẩn cho tất cả tài khoản về '123456'...");
            String standardPasswordHash = passwordEncoder.encode("123456");
            userRepository.findAll().forEach(u -> {
                u.setPassword(standardPasswordHash);
                userRepository.save(u);
                log.info("[DataSeeder] Đã cập nhật mật khẩu mã hóa BCrypt '123456' cho tài khoản: {}", u.getUsername());
            });
            return;
        }

        log.info("[DataSeeder] Đang tiến hành khởi tạo dữ liệu mẫu môi trường Phát triển (Dev)...");

        // 1. Khởi tạo 3 Người dùng (Users) với các vai trò thực tế kèm mật khẩu chuẩn '123456' (BCrypt)
        String defaultPasswordHash = passwordEncoder.encode("123456");

        User admin = User.builder()
            .username("admin")
            .email("admin@bank.corp")
            .fullName("Nguyễn Văn An")
            .password(defaultPasswordHash)
            .role(Role.ADMIN)
            .build();

        User devLead = User.builder()
            .username("dev_lead")
            .email("lead@bank.corp")
            .fullName("Trần Thị Bích")
            .password(defaultPasswordHash)
            .role(Role.MEMBER)
            .build();

        User developer = User.builder()
            .username("developer")
            .email("dev@bank.corp")
            .fullName("Lê Văn Cường")
            .password(defaultPasswordHash)
            .role(Role.MEMBER)
            .build();

        userRepository.saveAll(List.of(admin, devLead, developer));
        log.info("[DataSeeder] Đã tạo 3 người dùng mẫu với mật khẩu chuẩn '123456' (admin, dev_lead, developer).");

        // 2. Khởi tạo 4 Danh mục / Phân hệ dự án (Categories)
        Category catCoreBanking = Category.builder()
            .name("Core Banking Migration")
            .description("Dự án chuyển đổi và nâng cấp hệ thống kế toán lõi ngân hàng Core Banking")
            .colorHex("#4F46E5")
            .build();

        Category catPayment = Category.builder()
            .name("Payment Gateway Integration")
            .description("Tích hợp các cổng thanh toán liên ngân hàng NAPAS, VietQR và Ví điện tử")
            .colorHex("#10B981")
            .build();

        Category catFraud = Category.builder()
            .name("Fraud Detection & Security")
            .description("Hệ thống cảnh báo gian lận giao dịch và rà soát an toàn thông tin PCI-DSS")
            .colorHex("#EF4444")
            .build();

        Category catDevops = Category.builder()
            .name("DevOps & CI/CD Pipeline")
            .description("Tự động hóa triển khai Kubernetes, hạ tầng Cloud và giám sát APM Grafana")
            .colorHex("#F59E0B")
            .build();

        categoryRepository.saveAll(List.of(catCoreBanking, catPayment, catFraud, catDevops));
        log.info("[DataSeeder] Đã tạo 4 danh mục / phân hệ dự án mẫu.");

        // 3. Khởi tạo danh sách các Công việc mẫu (Tasks)
        LocalDate today = LocalDate.now();

        List<Task> initialTasks = List.of(
            // --- NHÓM 1: TRẠNG THÁI TODO ---
            Task.builder()
                .title("Nâng cấp giao thức bảo mật TLS 1.3 cho cổng thanh toán")
                .description("Rà soát cấu hình SSL/TLS trên Reverse Proxy Nginx và kiểm tra khả năng bắt tay bảo mật với các đối tác ví điện tử.")
                .dueDate(today.plusDays(5))
                .priority(Priority.HIGH)
                .status(TaskStatus.TODO)
                .category(catPayment)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Thiết kế kiến trúc Multi-datacenter Active-Active")
                .description("Lập tài liệu giải pháp High Availability cho cơ sở dữ liệu MySQL Cluster giữa Trung tâm chính và Trung tâm dự phòng DR.")
                .dueDate(today.plusDays(10))
                .priority(Priority.HIGH)
                .status(TaskStatus.TODO)
                .category(catCoreBanking)
                .assignee(admin)
                .build(),

            Task.builder()
                .title("Nghiên cứu áp dụng Kafka cho luồng giao dịch thời gian thực")
                .description("Thử nghiệm Benchmark hiệu năng Apache Kafka với 10,000 TPS để phục vụ luồng nhận tin nhắn SMS và Notification OTT.")
                .dueDate(today.plusDays(7))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.TODO)
                .category(catCoreBanking)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Cấu hình HashiCorp Vault quản lý bí mật mật khẩu cơ sở dữ liệu")
                .description("Loại bỏ hoàn toàn mật khẩu cố định trong file properties, chuyển sang cơ chế Dynamic Secret của Vault.")
                .dueDate(today.plusDays(12))
                .priority(Priority.HIGH)
                .status(TaskStatus.TODO)
                .category(catFraud)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Tối ưu hóa Dockerfile giảm kích thước Image xuống dưới 200MB")
                .description("Sử dụng Eclipse Temurin JRE Alpine và Multi-stage build để tối ưu dung lượng Container Image.")
                .dueDate(today.plusDays(15))
                .priority(Priority.LOW)
                .status(TaskStatus.TODO)
                .category(catDevops)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Xây dựng dashboard giám sát Grafana cho Kubernetes cluster")
                .description("Thiết lập các cảnh báo Alertmanager khi CPU pod vượt 80% hoặc tỷ lệ HTTP 5xx tăng đột biến.")
                .dueDate(today.plusDays(8))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.TODO)
                .category(catDevops)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Nghiên cứu giải pháp mã hóa dữ liệu nhạy cảm tại tầng JPA AttributeConverter")
                .description("Mã hóa các trường số tài khoản và CCCD theo tiêu chuẩn AES-256 trước khi lưu xuống cơ sở dữ liệu.")
                .dueDate(today.minusDays(2)) // Cố tình đặt quá hạn để kiểm tra cờ Overdue
                .priority(Priority.HIGH)
                .status(TaskStatus.TODO)
                .category(catFraud)
                .assignee(devLead)
                .build(),

            // --- NHÓM 2: TRẠNG THÁI IN_PROGRESS ---
            Task.builder()
                .title("Phát triển module đối soát giao dịch NAPAS cuối ngày")
                .description("Viết Batch Job đối chiếu dữ liệu giao dịch thẻ nội địa giữa tệp tin sao kê FTP từ NAPAS và cơ sở dữ liệu ngân hàng.")
                .dueDate(today.plusDays(3))
                .priority(Priority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .category(catPayment)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Refactor tầng Repository áp dụng EntityGraph chống N+1")
                .description("Rà soát các câu truy vấn findAll của TaskRepository, bổ sung EntityGraph nạp kèm Category và User.")
                .dueDate(today.plusDays(2))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.IN_PROGRESS)
                .category(catCoreBanking)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Triển khai thuật toán chấm điểm rủi ro gian lận theo hành vi")
                .description("Xây dựng service phân tích vị trí địa lý, thiết bị đăng nhập và giá trị giao dịch bất thường trong thời gian 5 phút.")
                .dueDate(today.plusDays(6))
                .priority(Priority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .category(catFraud)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Thiết lập pipeline SonarQube kiểm tra chất lượng mã nguồn tự động")
                .description("Cấu hình Quality Gate yêu cầu độ bao phủ Unit Test tối thiểu 80% và không có lỗ hổng bảo mật cấp độ High.")
                .dueDate(today.plusDays(4))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.IN_PROGRESS)
                .category(catDevops)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Tích hợp Webhook đồng bộ trạng thái thanh toán từ bên thứ ba")
                .description("Hiện thực cơ chế Idempotency Key để xử lý an toàn các gói tin Webhook gửi lại nhiều lần (Retry policy).")
                .dueDate(today.plusDays(5))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.IN_PROGRESS)
                .category(catPayment)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Xây dựng kịch bản kiểm thử tải Gatling cho API tra cứu số dư")
                .description("Mô phỏng 5,000 người dùng đồng thời truy cập trong thời gian cao điểm Tết Nguyên Đán.")
                .dueDate(today.plusDays(1))
                .priority(Priority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .category(catCoreBanking)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Bảo trì định kỳ máy chủ cơ sở dữ liệu môi trường Staging")
                .description("Thu dọn nhật ký binary log và chạy lệnh optimize table cho các bảng lịch sử giao dịch lớn.")
                .dueDate(today.minusDays(3)) // Cố tình đặt quá hạn để kiểm tra cờ Overdue
                .priority(Priority.MEDIUM)
                .status(TaskStatus.IN_PROGRESS)
                .category(catDevops)
                .assignee(developer)
                .build(),

            // --- NHÓM 3: TRẠNG THÁI REVIEW ---
            Task.builder()
                .title("Đặc tả kỹ thuật luồng chuyển tiền nhanh 24/7 NAPAS 247")
                .description("Kiểm duyệt tài liệu kiến trúc tích hợp hệ thống Core Banking qua chuẩn tin nhắn ISO 8583.")
                .dueDate(today.plusDays(2))
                .priority(Priority.HIGH)
                .status(TaskStatus.REVIEW)
                .category(catPayment)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Đánh chỉ mục Index cho bảng transaction_logs cải thiện tốc độ query")
                .description("Tạo Composite Index (user_id, created_at) giúp tăng tốc độ trích xuất sao kê lịch sử từ 1.2s xuống 45ms.")
                .dueDate(today.plusDays(3))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.REVIEW)
                .category(catCoreBanking)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Cấu hình quy tắc chặn IP đáng ngờ tại Web Application Firewall (WAF)")
                .description("Thử nghiệm kịch bản phòng thủ chống tấn công từ chối dịch vụ DDoS và chặn các dải IP nước ngoài nguy cơ cao.")
                .dueDate(today.plusDays(1))
                .priority(Priority.HIGH)
                .status(TaskStatus.REVIEW)
                .category(catFraud)
                .assignee(admin)
                .build(),

            Task.builder()
                .title("Triển khai hệ thống ghi log tập trung ELK Stack cho môi trường UAT")
                .description("Định dạng nhật ký JSON chuẩn cấu trúc, tích hợp Filebeat và Logstash đẩy dữ liệu về cụm Elasticsearch.")
                .dueDate(today.plusDays(4))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.REVIEW)
                .category(catDevops)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Kiểm tra tuân thủ tiêu chuẩn an toàn dữ liệu thẻ thanh toán PCI-DSS")
                .description("Phúc tra báo cáo rà quét mã nguồn SAST/DAST trước kỳ kiểm toán an ninh bảo mật hàng năm.")
                .dueDate(today.plusDays(2))
                .priority(Priority.HIGH)
                .status(TaskStatus.REVIEW)
                .category(catFraud)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Tối ưu hóa connection pool HikariCP cho cụm MySQL Master-Slave")
                .description("Thiết lập maximum-pool-size = 30, idle-timeout = 30000ms, leak-detection-threshold = 2000ms.")
                .dueDate(today.plusDays(5))
                .priority(Priority.HIGH)
                .status(TaskStatus.REVIEW)
                .category(catCoreBanking)
                .assignee(admin)
                .build(),

            Task.builder()
                .title("Viết tài liệu hướng dẫn vận hành sự cố (Runbook) cho đội trực ca 24/7")
                .description("Quy trình 5 bước ứng cứu khi nghẽn đường truyền mạng WAN hoặc lỗi timeout cổng thanh toán.")
                .dueDate(today.plusDays(7))
                .priority(Priority.LOW)
                .status(TaskStatus.REVIEW)
                .category(catDevops)
                .assignee(developer)
                .build(),

            // --- NHÓM 4: TRẠNG THÁI DONE ---
            Task.builder()
                .title("Khởi tạo cấu trúc dự án Task & Workflow Management System")
                .description("Thiết lập dự án Maven chuẩn 3 tầng Spring Boot 3.4, Java 17 và cấu hình UTF-8.")
                .dueDate(today.minusDays(5))
                .priority(Priority.HIGH)
                .status(TaskStatus.DONE)
                .category(catCoreBanking)
                .assignee(admin)
                .build(),

            Task.builder()
                .title("Cấu hình cơ sở dữ liệu MySQL 8 và bảng mã UTF8MB4")
                .description("Tạo schema task_manager, thiết lập cấu hình HikariCP và vô hiệu hóa Open Session In View.")
                .dueDate(today.minusDays(4))
                .priority(Priority.HIGH)
                .status(TaskStatus.DONE)
                .category(catCoreBanking)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Thiết kế sơ đồ cơ sở dữ liệu ERD cho 3 thực thể User, Category, Task")
                .description("Quy hoạch chỉ mục tối ưu, quan hệ một chiều LAZY loading và cơ chế khóa lạc quan @Version.")
                .dueDate(today.minusDays(3))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.DONE)
                .category(catCoreBanking)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Tích hợp giao diện quản trị Bootstrap 5 và chế độ Dark/Light mode")
                .description("Xây dựng layout chuẩn Enterprise với Sidebar, Navbar, Toast thông báo và bộ icon Bootstrap Icons.")
                .dueDate(today.minusDays(2))
                .priority(Priority.LOW)
                .status(TaskStatus.DONE)
                .category(catDevops)
                .assignee(developer)
                .build(),

            Task.builder()
                .title("Kiểm thử bảo mật chống SQL Injection và XSS trên toàn bộ endpoint")
                .description("Sử dụng Spring Data JPA Parameters Binding và Thymeleaf HTML escaping để triệt tiêu nguy cơ tiêm mã độc.")
                .dueDate(today.minusDays(2))
                .priority(Priority.HIGH)
                .status(TaskStatus.DONE)
                .category(catFraud)
                .assignee(devLead)
                .build(),

            Task.builder()
                .title("Xây dựng bộ quy tắc Finite State Machine cho vòng đời công việc")
                .description("Đóng gói logic chuyển trạng thái TODO -> IN_PROGRESS -> REVIEW -> DONE vào Enum TaskStatus.")
                .dueDate(today.minusDays(1))
                .priority(Priority.HIGH)
                .status(TaskStatus.DONE)
                .category(catCoreBanking)
                .assignee(admin)
                .build(),

            Task.builder()
                .title("Hoàn thành bộ tài liệu Postman Collection kiểm thử 100% REST API")
                .description("Xuất tệp tin postman_collection.json sẵn sàng cho việc kiểm thử tích hợp tự động.")
                .dueDate(today.minusDays(1))
                .priority(Priority.MEDIUM)
                .status(TaskStatus.DONE)
                .category(catDevops)
                .assignee(developer)
                .build()
        );

        taskRepository.saveAll(initialTasks);
        log.info("[DataSeeder] Đã khởi tạo thành công 28 công việc mẫu đại diện nghiệp vụ Enterprise/FinTech.");
    }
}
