/**
 * TaskFlow - Hệ thống thông báo thời gian thực (Notification Center)
 */
(function () {
    'use strict';

    const API_BASE = '/api/v1/notifications';
    let cachedNotifications = [];

    // Khởi chạy khi DOM đã sẵn sàng
    document.addEventListener('DOMContentLoaded', function () {
        initNotifications();
        // Polling định kỳ mỗi 60 giây để cập nhật thông báo mới
        setInterval(fetchNotifications, 60000);
    });

    function initNotifications() {
        const dropdownBtn = document.getElementById('notificationDropdownBtn');
        const markAllBtn = document.getElementById('markAllReadBtn');

        if (dropdownBtn) {
            dropdownBtn.addEventListener('click', function () {
                // Tải lại khi người dùng mở dropdown
                fetchNotifications();
            });
        }

        if (markAllBtn) {
            markAllBtn.addEventListener('click', function (e) {
                e.stopPropagation();
                markAllAsRead();
            });
        }

        // Tải dữ liệu ban đầu
        fetchNotifications();
    }

    async function fetchNotifications() {
        try {
            const response = await fetch(API_BASE, {
                headers: { 'Accept': 'application/json' }
            });
            if (!response.ok) return;

            const resData = await response.json();
            if (resData && resData.success && resData.data) {
                const { unreadCount, notifications } = resData.data;
                cachedNotifications = notifications || [];
                updateNotificationBadge(unreadCount || 0);
                renderNotificationList(cachedNotifications);
            }
        } catch (error) {
            console.debug('[Notifications] Không thể tải thông báo:', error);
        }
    }

    function updateNotificationBadge(count) {
        const badge = document.getElementById('notificationBadge');
        const headerCount = document.getElementById('notificationHeaderCount');

        if (headerCount) {
            headerCount.textContent = count > 0 ? `${count} mới` : '0';
        }

        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.classList.remove('d-none');
            } else {
                badge.classList.add('d-none');
            }
        }
    }

    function renderNotificationList(notifications) {
        const listContainer = document.getElementById('notificationList');
        if (!listContainer) return;

        if (!notifications || notifications.length === 0) {
            listContainer.innerHTML = `
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-bell-slash fs-2 d-block mb-2 text-secondary opacity-50"></i>
                    <div class="fs-xs fw-medium">Chưa có thông báo mới nào</div>
                    <div class="fs-xxs text-muted mt-1">Các công việc mới và cập nhật sẽ xuất hiện tại đây</div>
                </div>
            `;
            return;
        }

        let html = '';
        notifications.forEach(item => {
            const isUnread = !item.read;
            const iconHtml = getNotificationIcon(item.type);
            const targetUrl = item.targetUrl || '/tasks';

            html += `
                <div class="notification-item d-flex align-items-start gap-2.5 p-3 border-bottom ${isUnread ? 'bg-light unread' : ''}" 
                     data-id="${item.id}" 
                     data-url="${targetUrl}" 
                     style="cursor: pointer;">
                    <div class="flex-shrink-0 mt-0.5">
                        ${iconHtml}
                    </div>
                    <div class="flex-grow-1" style="min-width: 0;">
                        <div class="d-flex align-items-center justify-content-between mb-1">
                            <span class="fs-xs fw-semibold ${isUnread ? 'text-dark' : 'text-secondary'} text-truncate me-2">${escapeHtml(item.title)}</span>
                            <span class="fs-xxs text-muted text-nowrap">${escapeHtml(item.timeAgo)}</span>
                        </div>
                        <div class="fs-xs text-secondary text-break mb-0" style="line-height: 1.4;">${escapeHtml(item.message || '')}</div>
                    </div>
                    ${isUnread ? '<span class="badge rounded-circle bg-primary p-1 mt-1.5 ms-1" style="width: 7px; height: 7px; flex-shrink: 0;"></span>' : ''}
                </div>
            `;
        });

        listContainer.innerHTML = html;

        // Gắn sự kiện click cho từng item
        const items = listContainer.querySelectorAll('.notification-item');
        items.forEach(el => {
            el.addEventListener('click', async function () {
                const id = this.getAttribute('data-id');
                const url = this.getAttribute('data-url');

                try {
                    await fetch(`${API_BASE}/${id}/read`, {
                        method: 'PATCH',
                        headers: { 'Accept': 'application/json' }
                    });
                } catch (e) {
                    console.error('Lỗi đánh dấu đã đọc:', e);
                }

                if (url) {
                    window.location.href = url;
                }
            });
        });
    }

    function getNotificationIcon(type) {
        switch (type) {
            case 'TASK_OVERDUE':
                return `<div class="rounded-circle d-flex align-items-center justify-content-center bg-danger-subtle text-danger" style="width: 32px; height: 32px;">
                            <i class="bi bi-exclamation-octagon-fill fs-6"></i>
                        </div>`;
            case 'TASK_DUE_SOON':
                return `<div class="rounded-circle d-flex align-items-center justify-content-center bg-warning-subtle text-warning-emphasis" style="width: 32px; height: 32px;">
                            <i class="bi bi-clock-history fs-6"></i>
                        </div>`;
            case 'TASK_ASSIGNED':
                return `<div class="rounded-circle d-flex align-items-center justify-content-center bg-primary-subtle text-primary" style="width: 32px; height: 32px;">
                            <i class="bi bi-person-check-fill fs-6"></i>
                        </div>`;
            case 'STATUS_CHANGED':
                return `<div class="rounded-circle d-flex align-items-center justify-content-center bg-success-subtle text-success" style="width: 32px; height: 32px;">
                            <i class="bi bi-arrow-repeat fs-6"></i>
                        </div>`;
            case 'SYSTEM':
            default:
                return `<div class="rounded-circle d-flex align-items-center justify-content-center bg-info-subtle text-info" style="width: 32px; height: 32px;">
                            <i class="bi bi-bell-fill fs-6"></i>
                        </div>`;
        }
    }

    async function markAllAsRead() {
        try {
            const response = await fetch(`${API_BASE}/read-all`, {
                method: 'POST',
                headers: { 'Accept': 'application/json' }
            });
            if (response.ok) {
                fetchNotifications();
            }
        } catch (error) {
            console.error('Lỗi đánh dấu tất cả đã đọc:', error);
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
})();
