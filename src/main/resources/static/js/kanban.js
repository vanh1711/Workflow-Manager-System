/**
 * Quản lý tương tác kéo-thả (HTML5 Drag and Drop) trên bảng Kanban.
 * Tích hợp kiểm tra luật State Machine ở Client và gọi REST API PATCH /api/v1/tasks/{id}/status.
 */
document.addEventListener('DOMContentLoaded', () => {
    const cards = document.querySelectorAll('.kanban-card');
    const columns = document.querySelectorAll('.kanban-column');

    // Cho phép di chuyển tự do giữa các trạng thái khác nhau (đồng bộ với TaskStatus.java)
    const ALLOWED_TRANSITIONS = {
        'TODO': ['IN_PROGRESS', 'REVIEW', 'DONE'],
        'IN_PROGRESS': ['TODO', 'REVIEW', 'DONE'],
        'REVIEW': ['TODO', 'IN_PROGRESS', 'DONE'],
        'DONE': ['TODO', 'IN_PROGRESS', 'REVIEW']
    };

    let draggedCard = null;
    let sourceStatus = null;
    let sourceContainer = null;

    cards.forEach(card => {
        card.addEventListener('dragstart', (e) => {
            draggedCard = card;
            sourceStatus = card.getAttribute('data-current-status');
            sourceContainer = card.parentElement;

            card.classList.add('is-dragging');
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', card.getAttribute('data-task-id'));

            // Cho phép thả vào tất cả các cột khác
            const allowedNext = ALLOWED_TRANSITIONS[sourceStatus] || [];
            columns.forEach(col => {
                const colStatus = col.getAttribute('data-status');
                if (colStatus !== sourceStatus && !allowedNext.includes(colStatus)) {
                    col.classList.add('drop-target-invalid');
                }
            });
        });

        card.addEventListener('dragend', () => {
            if (draggedCard) {
                draggedCard.classList.remove('is-dragging');
            }
            columns.forEach(col => {
                col.classList.remove('drop-target-active', 'drop-target-invalid');
            });
            draggedCard = null;
            sourceStatus = null;
            sourceContainer = null;
        });
    });

    columns.forEach(col => {
        const targetStatus = col.getAttribute('data-status');
        const container = col.querySelector('.kanban-cards-container');

        col.addEventListener('dragover', (e) => {
            if (!draggedCard) return;

            const allowedNext = ALLOWED_TRANSITIONS[sourceStatus] || [];
            if (allowedNext.includes(targetStatus)) {
                e.preventDefault();
                e.dataTransfer.dropEffect = 'move';
                col.classList.add('drop-target-active');
            } else {
                e.dataTransfer.dropEffect = 'none';
            }
        });

        col.addEventListener('dragleave', () => {
            col.classList.remove('drop-target-active');
        });

        col.addEventListener('drop', async (e) => {
            e.preventDefault();
            col.classList.remove('drop-target-active');

            if (!draggedCard) return;

            const taskId = draggedCard.getAttribute('data-task-id');
            const version = draggedCard.getAttribute('data-version');
            const currentCard = draggedCard;
            const originalContainer = sourceContainer;
            const originalStatus = sourceStatus;

            // Di chuyển thẻ tạm thời sang cột đích
            container.appendChild(currentCard);
            updateColumnCounts();

            try {
                const response = await fetch(`/api/v1/tasks/${taskId}/status`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({
                        status: targetStatus,
                        version: version ? parseInt(version) : null
                    })
                });

                const result = await response.json();

                if (response.ok && result.success) {
                    // Cập nhật trạng thái và version mới vào thẻ
                    currentCard.setAttribute('data-current-status', targetStatus);
                    if (result.data && result.data.version !== undefined) {
                        currentCard.setAttribute('data-version', result.data.version);
                    }
                    showToast('success', result.message || 'Cập nhật trạng thái thành công!');
                } else {
                    // Thất bại (409 hoặc 422): Hoàn trả thẻ về cột cũ
                    originalContainer.appendChild(currentCard);
                    updateColumnCounts();
                    showToast('error', result.message || 'Chuyển trạng thái thất bại!');
                }
            } catch (err) {
                // Lỗi mạng: Hoàn trả thẻ về cột cũ
                originalContainer.appendChild(currentCard);
                updateColumnCounts();
                showToast('error', 'Không thể kết nối đến máy chủ. Đã hoàn tác thao tác!');
            }
        });
    });

    function updateColumnCounts() {
        columns.forEach(col => {
            const count = col.querySelectorAll('.kanban-card').length;
            const badge = col.querySelector('.kanban-count-badge');
            if (badge) {
                badge.textContent = count;
            }
        });
    }

    function showToast(type, message) {
        const toastContainer = document.getElementById('toast-notification-container');
        if (!toastContainer) return;

        const isSuccess = type === 'success';
        const bgClass = isSuccess ? 'bg-success text-white' : 'bg-danger text-white';
        const icon = isSuccess ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill';

        const toastEl = document.createElement('div');
        toastEl.className = `toast align-items-center ${bgClass} border-0 shadow`;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');
        toastEl.innerHTML = `
            <div class="d-flex">
                <div class="toast-body d-flex align-items-center gap-2">
                    <i class="bi ${icon}"></i>
                    <span>${message}</span>
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;

        toastContainer.appendChild(toastEl);
        const bsToast = new bootstrap.Toast(toastEl, { delay: 4000 });
        bsToast.show();

        toastEl.addEventListener('hidden.bs.toast', () => {
            toastEl.remove();
        });
    }
});
