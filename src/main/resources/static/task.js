const STATUS_ORDER = [
    { key: 'all', label: 'All Task' },
    { key: 'todo', label: 'To Do' },
    { key: 'on-going', label: 'On Going' },
    { key: 'completed', label: 'Completed' },
    { key: 'paused', label: 'Paused' }
];

const STATUS_STYLES = {
    'todo': { color: '#f3a218' },
    'on-going': { color: '#5267ff' },
    'completed': { color: '#2db47d' },
    'paused': { color: '#e14c26' }
};

const state = {
    tasks: [
        {
            id: 1,
            title: '모델 Accuracy 보완',
            meeting: '회의록: 요약',
            status: 'todo',
            due: '2023-09-16',
            project: 'AI 모델 고도화',
            source: 'action',
            userEdited: false,
            checklist: [
                { text: '빠르게 필사하는 영어 공부', done: true },
                { text: 'QA-384 검수 프로세스 정리', done: false }
            ]
        },
        {
            id: 2,
            title: '추출된 단어 분석',
            meeting: '회의록: 요약',
            status: 'todo',
            due: '2023-10-02',
            project: 'NLP 실험',
            source: 'action',
            userEdited: true,
            checklist: [
                { text: 'today', done: true },
                { text: 'today', done: true },
                { text: '추출된 단어 분석', done: false },
                { text: '추출된 단어 분석', done: false }
            ]
        },
        {
            id: 3,
            title: '빠르게 필사하는 영어 공부',
            meeting: '회의록: 요약',
            status: 'todo',
            due: '2023-10-12',
            project: '개인 성장',
            source: 'manual',
            userEdited: true,
            checklist: [
                { text: '빠르게 필사하는 영어 공부', done: true },
                { text: '빠르게 필사하는 영어 공부', done: false }
            ]
        },
        {
            id: 4,
            title: 'QA-384 검수 프로세스 정리',
            meeting: '회의록: 요약',
            status: 'todo',
            due: '2023-09-28',
            project: 'QA 고도화',
            source: 'action',
            userEdited: false,
            checklist: [
                { text: 'QA-384 검수 프로세스 정리', done: true },
                { text: 'QA-384 검수 프로세스 정리', done: false },
                { text: 'QA-384 검수 프로세스 정리', done: false }
            ]
        },
        {
            id: 5,
            title: '정책 변경 대응 정리',
            meeting: '회의록: 요약',
            status: 'todo',
            due: '2023-09-28',
            source: 'manual',
            userEdited: true,
            checklist: [
                { text: '정책 변경 대응 정리', done: true },
                { text: '정책 변경 대응 정리', done: false },
                { text: '정책 변경 대응 정리', done: false }
            ]
        },
        {
            id: 6,
            title: '빠르게 필사하는 영어 공부',
            meeting: '회의록: 요약',
            status: 'on-going',
            due: '2023-10-12',
            project: '개인 성장',
            source: 'action',
            userEdited: false,
            checklist: [
                { text: '빠르게 필사하는 영어 공부', done: true },
                { text: '빠르게 필사하는 영어 공부', done: false }
            ]
        },
        {
            id: 7,
            title: '분리수거 픽업 문의 드려요',
            meeting: '회의록: 요약',
            status: 'on-going',
            due: '2023-10-01',
            source: 'action',
            userEdited: true,
            checklist: [
                { text: '오늘 꼭 끝내기', done: true },
                { text: 'todo item', done: true },
                { text: '추출된 단어 분석', done: false }
            ]
        },
        {
            id: 8,
            title: '맛있겠다',
            meeting: '회의록: 요약',
            status: 'on-going',
            due: '2023-09-30',
            project: '신메뉴 협업',
            source: 'manual',
            userEdited: true,
            checklist: [
                { text: 'today', done: false },
                { text: 'QA-384 검수 프로세스 정리', done: false },
                { text: '빠르게 필사하는 영어 공부', done: false },
                { text: 'to do item', done: false }
            ]
        },
        {
            id: 9,
            title: '추출된 단어 분석',
            meeting: '회의록: 요약',
            status: 'completed',
            due: '2023-09-21',
            project: 'NLP 실험',
            source: 'action',
            userEdited: false,
            checklist: [
                { text: '추출된 단어 분석', done: true },
                { text: '추출된 단어 분석', done: true },
                { text: '추출된 단어 분석', done: true }
            ]
        },
        {
            id: 10,
            title: '빠르게 필사하는 영어 공부',
            meeting: '회의록: 요약',
            status: 'completed',
            due: '2023-10-12',
            source: 'manual',
            userEdited: true,
            checklist: [
                { text: '빠르게 필사하는 영어 공부', done: true },
                { text: '빠르게 필사하는 영어 공부', done: true }
            ]
        },
        {
            id: 11,
            title: 'QA-384 검수 프로세스 정리',
            meeting: '회의록: 요약',
            status: 'completed',
            due: '2023-09-28',
            project: 'QA 고도화',
            source: 'action',
            userEdited: true,
            checklist: [
                { text: 'QA-384 검수 프로세스 정리', done: true },
                { text: 'QA-384 검수 프로세스 정리', done: true }
            ]
        },
        {
            id: 12,
            title: '예쁜 색인 것 같아요',
            meeting: '회의록: 요약',
            status: 'paused',
            due: '2023-10-03',
            project: '신규 테마',
            source: 'action',
            userEdited: false,
            checklist: [
                { text: '예쁜 색인 것 같아요', done: true },
                { text: '예쁜 색인 것 같아요', done: false }
            ]
        }
    ],
    selectedTab: 'all',
    showChecklist: true,
    showTodo: true,
    search: ''
};

let tabContainer;
let board;
let searchInput;
let toggleChecklist;
let toggleTodo;

function initDomReferences() {
    tabContainer = document.getElementById('status-tabs');
    board = document.getElementById('board');
    searchInput = document.getElementById('search');
    toggleChecklist = document.getElementById('toggle-checklist');
    toggleTodo = document.getElementById('toggle-todo');

    const missingElements = [tabContainer, board, searchInput, toggleChecklist, toggleTodo].includes(null);
    if (missingElements) {
        console.error('필수 DOM 요소를 찾지 못했습니다. 렌더링을 중단합니다.');
        return false;
    }
    return true;
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
}

function countByStatus(tasks) {
    const counts = STATUS_ORDER.reduce((acc, { key }) => ({ ...acc, [key]: 0 }), {});
    tasks.forEach((task) => {
        counts.all += 1;
        counts[task.status] += 1;
    });
    return counts;
}

function renderTabs() {
    const filtered = applySearch(state.tasks);
    const counts = countByStatus(filtered);
    tabContainer.innerHTML = '';

    STATUS_ORDER.forEach(({ key, label }) => {
        const tab = document.createElement('button');
        tab.className = `tab ${state.selectedTab === key ? 'active' : ''}`;
        tab.dataset.status = key;
        const indicator = document.createElement('span');
        indicator.className = 'tab__indicator';
        indicator.style.background = STATUS_STYLES[key]?.color || '#f3a218';
        indicator.textContent = state.selectedTab === key ? '✓' : '';

        const text = document.createElement('span');
        text.textContent = label;

        const count = document.createElement('span');
        count.className = 'count';
        count.textContent = counts[key];

        tab.append(indicator, text, count);
        tab.addEventListener('click', () => {
            state.selectedTab = key;
            renderTabs();
            renderBoard();
        });
        tabContainer.appendChild(tab);
    });
}

function applySearch(tasks) {
    if (!state.search) return tasks;
    const keyword = state.search.toLowerCase();
    return tasks.filter((task) =>
        task.title.toLowerCase().includes(keyword) || task.meeting.toLowerCase().includes(keyword)
    );
}

function filteredTasksForStatus(statusKey) {
    const tasks = applySearch(state.tasks);
    return tasks.filter((task) => task.status === statusKey);
}

function renderBoard() {
    board.innerHTML = '';
    const allStatuses = STATUS_ORDER.filter((status) => status.key !== 'all');
    const statuses = state.selectedTab === 'all'
        ? allStatuses
        : allStatuses.filter(({ key }) => key === state.selectedTab);

    statuses.forEach(({ key, label }) => {
        const column = document.createElement('div');
        column.className = 'column';

        const header = document.createElement('div');
        header.className = 'column__header';
        const title = document.createElement('div');
        title.className = 'column__title';
        const dot = document.createElement('span');
        dot.className = 'status-dot';
        dot.style.background = STATUS_STYLES[key].color;
        title.append(dot, document.createTextNode(label));

        const count = document.createElement('div');
        count.className = 'column__count';
        const tasksForColumn = filteredTasksForStatus(key);
        count.textContent = tasksForColumn.length;

        header.append(title, count);
        column.appendChild(header);

        if (tasksForColumn.length === 0) {
            const empty = document.createElement('div');
            empty.className = 'placeholder';
            empty.textContent = '채우기 전 빈 레이아웃';
            column.appendChild(empty);
        } else {
            tasksForColumn.forEach((task) => column.appendChild(renderTaskCard(task)));
        }

        board.appendChild(column);
    });
}

function renderTaskCard(task) {
    const card = document.createElement('div');
    card.className = `task-card ${task.status === 'paused' ? 'paused' : ''} ${task.status === 'todo' ? 'todo' : ''} ${task.status === 'completed' ? 'completed' : ''}`;

    const header = document.createElement('div');
    header.className = 'task-title-row';

    const title = document.createElement('h3');
    title.className = 'task-title';
    title.textContent = task.title;

    const actions = document.createElement('div');
    actions.className = 'task-actions';

    const editBtn = document.createElement('button');
    editBtn.className = 'task-action-btn';
    editBtn.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 17.17V20h2.83L17.9 8.93l-2.83-2.83L4 17.17zM20.7 7.04a1 1 0 000-1.41l-2.34-2.34a1 1 0 00-1.41 0l-1.82 1.82 3.75 3.75 1.82-1.82z" fill="currentColor"></path></svg>';
    editBtn.title = '수정';
    editBtn.addEventListener('click', () => handleEdit(task.id));

    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'task-action-btn danger';
    deleteBtn.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 7h10l-.9 12.1a2 2 0 01-2 1.9H9.9a2 2 0 01-2-1.9L7 7zm12-2a1 1 0 01.12 2H4.88A1 1 0 015 5h3.5l.44-1.33A1 1 0 019.88 3h4.24a1 1 0 01.94.67L15.5 5H19z" fill="currentColor"></path></svg>';
    deleteBtn.title = '삭제';
    deleteBtn.addEventListener('click', () => handleDelete(task.id));

    actions.append(editBtn, deleteBtn);
    header.append(title, actions);
    card.appendChild(header);

    if (state.showChecklist) {
        const list = document.createElement('ul');
        list.className = 'checklist';
        task.checklist.forEach((item, index) => {
            if (!state.showTodo && !item.done) return;
            const li = document.createElement('li');
            const box = document.createElement('button');
            box.type = 'button';
            box.className = `check-box ${item.done ? 'checked' : ''}`;
            box.textContent = item.done ? '✓' : '';
            box.addEventListener('click', () => toggleChecklist(task.id, index));
            li.append(box, document.createTextNode(item.text));
            list.appendChild(li);
        });
        card.appendChild(list);
    }

    const badgeRow = document.createElement('div');
    badgeRow.className = 'badge-row';
    if (task.source === 'action') {
        const badge = document.createElement('span');
        badge.className = 'badge source meeting';
        badge.textContent = '회의 요약 결과';
        badgeRow.appendChild(badge);
    }
    if (task.source === 'manual' || task.userEdited) {
        const badge = document.createElement('span');
        badge.className = 'badge source direct';
        badge.textContent = '직접 추가';
        badgeRow.appendChild(badge);
    }
    card.appendChild(badgeRow);

    const dueRow = document.createElement('div');
    dueRow.className = 'task-info';
    dueRow.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 4a1 1 0 012 0v1h6V4a1 1 0 112 0v1h1a2 2 0 012 2v12a2 2 0 01-2 2H4a2 2 0 01-2-2V7a2 2 0 012-2h1V4a1 1 0 112 0v1zm13 7H4v7a1 1 0 001 1h14a1 1 0 001-1z" fill="currentColor"></path></svg> ${task.due ? formatDate(task.due) : '마감 기한'}`;
    card.appendChild(dueRow);

    if (task.project) {
        const projectRow = document.createElement('div');
        projectRow.className = 'task-info';
        projectRow.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 4h14a2 2 0 012 2v13l-5-3-5 3-5-3-5 3V6a2 2 0 012-2z" fill="currentColor"></path></svg> ${task.project}`;
        card.appendChild(projectRow);
    }

    return card;
}

function toggleChecklist(taskId, itemIndex) {
    const task = state.tasks.find((t) => t.id === taskId);
    if (!task || !task.checklist[itemIndex]) return;
    task.checklist[itemIndex].done = !task.checklist[itemIndex].done;
    if (task.source === 'action') {
        task.userEdited = true;
    }
    renderBoard();
}

function handleEdit(taskId) {
    const task = state.tasks.find((t) => t.id === taskId);
    if (!task) return;

    const newTitle = prompt('제목을 수정하세요', task.title);
    if (newTitle === null) return;
    const newStatus = prompt('상태 (todo/on-going/completed/paused)', task.status) || task.status;
    const newDue = prompt('마감 기한 (YYYY-MM-DD)', task.due) || task.due;
    const newProject = prompt('관련 프로젝트명 (비우면 없음)', task.project || '') || '';

    const trimmedTitle = newTitle.trim();
    task.title = trimmedTitle || task.title;
    if (STATUS_ORDER.some(({ key }) => key === newStatus) && newStatus !== 'all') {
        task.status = newStatus;
    }
    task.due = newDue;
    task.project = newProject.trim();
    task.userEdited = task.userEdited || task.source === 'action';

    renderTabs();
    renderBoard();
}

function handleDelete(taskId) {
    const confirmed = confirm('이 카드를 삭제할까요?');
    if (!confirmed) return;
    state.tasks = state.tasks.filter((t) => t.id !== taskId);
    renderTabs();
    renderBoard();
}

function init() {
    if (!initDomReferences()) {
        return;
    }

    searchInput.addEventListener('input', (e) => {
        state.search = e.target.value;
        renderTabs();
        renderBoard();
    });

    toggleChecklist.addEventListener('change', (e) => {
        state.showChecklist = e.target.checked;
        renderBoard();
    });

    toggleTodo.addEventListener('change', (e) => {
        state.showTodo = e.target.checked;
        renderBoard();
    });

    renderTabs();
    renderBoard();
}

document.addEventListener('DOMContentLoaded', init);