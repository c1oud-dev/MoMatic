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
            checklist: [
                { text: '예쁜 색인 것 같아요', done: true },
                { text: '예쁜 색인 것 같아요', done: false }
            ]
        }
    ],
    selectedTab: 'on-going',
    showChecklist: true,
    showTodo: true,
    search: ''
};

const tabContainer = document.getElementById('status-tabs');
const board = document.getElementById('board');
const searchInput = document.getElementById('search');
const toggleChecklist = document.getElementById('toggle-checklist');
const toggleTodo = document.getElementById('toggle-todo');

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
        tab.innerHTML = `<span>${label}</span><span class="count">${counts[key]}</span>`;
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
    const statuses = STATUS_ORDER.filter((status) => status.key !== 'all');

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

    const top = document.createElement('div');
    top.className = 'task-top';
    const statusBadge = document.createElement('div');
    statusBadge.className = 'badge status';
    statusBadge.textContent = statusLabel(task.status);
    const dueBadge = document.createElement('div');
    dueBadge.className = 'badge due';
    dueBadge.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 4a1 1 0 012 0v1h6V4a1 1 0 112 0v1h1a2 2 0 012 2v12a2 2 0 01-2 2H4a2 2 0 01-2-2V7a2 2 0 012-2h1V4a1 1 0 112 0v1zm13 7H4v7a1 1 0 001 1h14a1 1 0 001-1z" fill="currentColor"></path></svg> ${formatDate(task.due)}`;
    top.append(statusBadge, dueBadge);

    const title = document.createElement('h3');
    title.className = 'task-title';
    title.textContent = task.title;

    const meta = document.createElement('div');
    meta.className = 'task-meta';
    meta.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 4h14a2 2 0 012 2v13l-5-3-5 3-5-3-5 3V6a2 2 0 012-2z" fill="currentColor"></path></svg> ${task.meeting}`;

    card.append(top, title, meta);

    if (state.showChecklist) {
        const list = document.createElement('ul');
        list.className = 'checklist';
        task.checklist.forEach((item) => {
            if (!state.showTodo && !item.done) return;
            const li = document.createElement('li');
            const box = document.createElement('span');
            box.className = `check-box ${item.done ? 'checked' : ''}`;
            box.textContent = item.done ? '✓' : '';
            li.append(box, document.createTextNode(item.text));
            list.appendChild(li);
        });
        card.appendChild(list);
    }

    return card;
}

function statusLabel(key) {
    switch (key) {
        case 'todo':
            return 'To Do';
        case 'on-going':
            return 'On going';
        case 'completed':
            return 'Complete';
        case 'paused':
            return 'Paused';
        default:
            return 'Task';
    }
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