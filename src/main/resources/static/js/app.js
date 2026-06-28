// ========== 全局状态 ==========
const state = {
    token: localStorage.getItem('token'),
    userId: localStorage.getItem('userId'),
    username: localStorage.getItem('username'),
    nickname: localStorage.getItem('nickname'),
    currentConversationId: null,
    conversations: []
};

// ========== API 工具函数 ==========
async function api(url, options = {}) {
    const headers = { 'Content-Type': 'application/json' };
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    const response = await fetch(url, { ...options, headers: { ...headers, ...options.headers } });
    if (response.status === 401) {
        logout();
        throw new Error('未登录');
    }
    return response.json();
}

async function apiUpload(url, formData) {
    const headers = {};
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    console.log('发送上传请求到:', url);
    console.log('请求头:', headers);

    const response = await fetch(url, { method: 'POST', headers, body: formData });

    console.log('响应状态:', response.status);
    console.log('响应头:', Object.fromEntries(response.headers.entries()));

    if (response.status === 401) {
        logout();
        throw new Error('未登录或 Token 已过期');
    }
    if (response.status === 403) {
        const errorText = await response.text();
        console.error('403 响应内容:', errorText);
        throw new Error('权限不足: ' + errorText);
    }
    if (!response.ok) {
        const errorText = await response.text();
        console.error('请求失败:', response.status, errorText);
        throw new Error(`请求失败 (${response.status}): ${errorText}`);
    }

    const data = await response.json();
    console.log('响应数据:', data);
    return data;
}

// ========== 认证相关 ==========
function initAuth() {
    // 标签切换
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById('login-form').classList.toggle('hidden');
            document.getElementById('register-form').classList.toggle('hidden');
            document.getElementById('auth-error').textContent = '';
        });
    });

    // 登录
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        try {
            const res = await api('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });
            if (res.code === 200) {
                saveAuth(res.data);
                showMainPage();
            } else {
                document.getElementById('auth-error').textContent = res.message;
            }
        } catch (err) {
            document.getElementById('auth-error').textContent = '登录失败，请重试';
        }
    });

    // 注册
    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('reg-username').value;
        const password = document.getElementById('reg-password').value;
        const nickname = document.getElementById('reg-nickname').value;
        try {
            const res = await api('/api/auth/register', {
                method: 'POST',
                body: JSON.stringify({ username, password, nickname })
            });
            if (res.code === 200) {
                saveAuth(res.data);
                showMainPage();
            } else {
                document.getElementById('auth-error').textContent = res.message;
            }
        } catch (err) {
            document.getElementById('auth-error').textContent = '注册失败，请重试';
        }
    });
}

function saveAuth(data) {
    state.token = data.token;
    state.userId = data.userId;
    state.username = data.username;
    state.nickname = data.nickname;
    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('username', data.username);
    localStorage.setItem('nickname', data.nickname);
}

function logout() {
    state.token = null;
    state.userId = null;
    state.currentConversationId = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('nickname');
    document.getElementById('auth-page').classList.remove('hidden');
    document.getElementById('main-page').classList.add('hidden');
}

// ========== 主页面 ==========
function showMainPage() {
    document.getElementById('auth-page').classList.add('hidden');
    document.getElementById('main-page').classList.remove('hidden');
    loadConversations();

    // 退出登录
    document.getElementById('logout-btn').addEventListener('click', logout);

    // 新对话
    document.getElementById('new-chat-btn').addEventListener('click', createConversation);

    // 文档管理
    document.getElementById('manage-docs-btn').addEventListener('click', showDocModal);

    // 发送消息
    document.getElementById('send-btn').addEventListener('click', sendMessage);
    document.getElementById('message-input').addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // 自动调整输入框高度
    document.getElementById('message-input').addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = Math.min(this.scrollHeight, 120) + 'px';
    });
}

// ========== 对话管理 ==========
async function loadConversations() {
    const res = await api('/api/chat/conversations');
    if (res.code === 200) {
        state.conversations = res.data;
        renderConversationList();
    }
}

function renderConversationList() {
    const list = document.getElementById('conversation-list');
    list.innerHTML = state.conversations.map(conv => `
        <div class="conv-item ${conv.id === state.currentConversationId ? 'active' : ''}"
             data-id="${conv.id}" onclick="selectConversation(${conv.id})">
            <div class="conv-title">${escapeHtml(conv.title)}</div>
            <div class="conv-time">${formatTime(conv.updatedAt)}</div>
            <button class="conv-delete" onclick="event.stopPropagation(); deleteConversation(${conv.id})">✕</button>
        </div>
    `).join('');
}

async function createConversation() {
    const res = await api('/api/chat/conversations', {
        method: 'POST',
        body: JSON.stringify({ title: '新对话' })
    });
    if (res.code === 200) {
        state.conversations.unshift(res.data);
        selectConversation(res.data.id);
    }
}

async function selectConversation(conversationId) {
    state.currentConversationId = conversationId;
    renderConversationList();

    const conv = state.conversations.find(c => c.id === conversationId);
    document.getElementById('chat-header').innerHTML = `<h3>${escapeHtml(conv?.title || '对话')}</h3>`;
    document.getElementById('chat-input-area').classList.remove('hidden');

    // 加载历史消息
    const res = await api(`/api/chat/conversations/${conversationId}/messages`);
    if (res.code === 200) {
        renderMessages(res.data);
    }
}

async function deleteConversation(conversationId) {
    if (!confirm('确定要删除这个对话吗？')) return;
    await api(`/api/chat/conversations/${conversationId}`, { method: 'DELETE' });
    state.conversations = state.conversations.filter(c => c.id !== conversationId);
    if (state.currentConversationId === conversationId) {
        state.currentConversationId = null;
        document.getElementById('chat-messages').innerHTML = '<div class="welcome-msg"><h2>👋 选择或创建一个对话</h2></div>';
        document.getElementById('chat-input-area').classList.add('hidden');
    }
    renderConversationList();
}

// ========== 消息处理 ==========
function renderMessages(messages) {
    const container = document.getElementById('chat-messages');
    if (messages.length === 0) {
        container.innerHTML = '<div class="welcome-msg"><h2>开始你的第一个问题吧！</h2></div>';
        return;
    }
    container.innerHTML = messages.map(msg => `
        <div class="message ${msg.role.toLowerCase()}">
            <div class="msg-content">${formatContent(msg.content)}</div>
        </div>
    `).join('');
    container.scrollTop = container.scrollHeight;
}

async function sendMessage() {
    const input = document.getElementById('message-input');
    const message = input.value.trim();
    if (!message || !state.currentConversationId) return;

    // 显示用户消息
    appendMessage('USER', message);
    input.value = '';
    input.style.height = 'auto';

    // 显示加载状态
    const loadingId = appendLoading();
    document.getElementById('send-btn').disabled = true;

    try {
        const res = await api('/api/chat/send', {
            method: 'POST',
            body: JSON.stringify({
                conversationId: state.currentConversationId,
                message: message
            })
        });

        removeLoading(loadingId);

        if (res.code === 200) {
            appendMessage('ASSISTANT', res.data.reply);
            // 更新对话标题
            loadConversations();
        } else {
            appendMessage('ASSISTANT', '❌ 请求失败：' + res.message);
        }
    } catch (err) {
        removeLoading(loadingId);
        appendMessage('ASSISTANT', '❌ 网络错误，请重试');
    }

    document.getElementById('send-btn').disabled = false;
}

function appendMessage(role, content) {
    const container = document.getElementById('chat-messages');
    // 移除欢迎消息
    const welcome = container.querySelector('.welcome-msg');
    if (welcome) welcome.remove();

    const div = document.createElement('div');
    div.className = `message ${role.toLowerCase()}`;
    div.innerHTML = `<div class="msg-content">${formatContent(content)}</div>`;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function appendLoading() {
    const container = document.getElementById('chat-messages');
    const id = 'loading-' + Date.now();
    const div = document.createElement('div');
    div.id = id;
    div.className = 'message assistant';
    div.innerHTML = '<div class="msg-content loading-content"><div class="loading"></div> 思考中...</div>';
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
    return id;
}

function removeLoading(id) {
    const el = document.getElementById(id);
    if (el) el.remove();
}

// ========== 文档管理 ==========
function showDocModal() {
    document.getElementById('doc-modal').classList.remove('hidden');
    loadDocuments();

    // 关闭弹窗
    document.querySelector('.btn-close').onclick = () => {
        document.getElementById('doc-modal').classList.add('hidden');
    };

    // 上传区域点击
    const uploadArea = document.getElementById('upload-area');
    const fileInput = document.getElementById('file-input');

    uploadArea.onclick = () => fileInput.click();

    uploadArea.ondragover = (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = '#3b82f6';
    };

    uploadArea.ondragleave = () => {
        uploadArea.style.borderColor = '#d1d5db';
    };

    uploadArea.ondrop = (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = '#d1d5db';
        if (e.dataTransfer.files.length > 0) {
            uploadFile(e.dataTransfer.files[0]);
        }
    };

    fileInput.onchange = () => {
        if (fileInput.files.length > 0) {
            uploadFile(fileInput.files[0]);
            fileInput.value = '';
        }
    };
}

async function loadDocuments() {
    const res = await api('/api/documents');
    if (res.code === 200) {
        renderDocumentList(res.data);
    }
}

function renderDocumentList(docs) {
    const list = document.getElementById('doc-list');
    if (docs.length === 0) {
        list.innerHTML = '<p style="text-align:center;color:#9ca3af;padding:20px;">暂无文档，请上传</p>';
        return;
    }
    list.innerHTML = docs.map(doc => `
        <div class="doc-item">
            <div class="doc-info">
                <span class="doc-icon">${getFileIcon(doc.fileType)}</span>
                <div>
                    <div class="doc-name">${escapeHtml(doc.fileName)}</div>
                    <div style="font-size:12px;color:#9ca3af;">${formatFileSize(doc.fileSize)}</div>
                </div>
            </div>
            <span class="doc-status ${doc.status}">${getStatusText(doc.status)}</span>
            <button class="btn-delete" onclick="deleteDocument(${doc.id})">🗑️</button>
        </div>
    `).join('');
}

async function uploadFile(file) {
    console.log('开始上传文件:', file.name, '大小:', file.size);
    console.log('当前 Token:', state.token ? '已存在' : '不存在');

    const formData = new FormData();
    formData.append('file', file);
    try {
        const res = await apiUpload('/api/documents/upload', formData);
        console.log('上传响应:', res);
        if (res.code === 200) {
            loadDocuments();
        } else {
            alert('上传失败：' + res.message);
        }
    } catch (err) {
        console.error('上传错误:', err);
        alert('上传失败：' + err.message);
    }
}

async function deleteDocument(docId) {
    if (!confirm('确定要删除这个文档吗？')) return;
    await api(`/api/documents/${docId}`, { method: 'DELETE' });
    loadDocuments();
}

// ========== 工具函数 ==========
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatContent(content) {
    // 简单的 markdown 转换
    return content
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>');
}

function formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
    return date.toLocaleDateString('zh-CN');
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
}

function getFileIcon(type) {
    const icons = { pdf: '📕', doc: '📘', docx: '📘', txt: '📝', md: '📗' };
    return icons[type] || '📄';
}

function getStatusText(status) {
    const texts = { READY: '就绪', PARSING: '解析中', FAILED: '失败' };
    return texts[status] || status;
}

// ========== 初始化 ==========
document.addEventListener('DOMContentLoaded', () => {
    if (state.token) {
        showMainPage();
    } else {
        initAuth();
    }
});
