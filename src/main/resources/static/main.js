// 데모용 초기 데이터와 전역 상태
const state = {
    loggedIn: false,
    userName: 'Haneul',
    projects: ['Project #1', 'Project #2'],
    tasks: [
        {
            id: 1,
            title: '오늘 대표 회의록 작성',
            project: 'Project #1',
            meeting: '프로젝트 A 시작 회의',
            status: 'On Going',
            dueDate: offsetDate(0),
            checklist: [
                { text: 'to-do item', done: false },
                { text: 'to-do item', done: true },
                { text: 'Indented to-do item', done: false }
            ]
        },
        {
            id: 2,
            title: 'Voice Activity 연동 이슈 해결',
            project: 'Project #2',
            meeting: '백로그 리파인먼트',
            status: 'On Going',
            dueDate: offsetDate(3),
            checklist: [
                { text: '녹음/정리 모듈 오류 로그 확인', done: true },
                { text: 'API 응답 시간 개선 아이디어 정리', done: false }
            ]
        },
        {
            id: 3,
            title: '모델 Accuracy 보완',
            project: 'Project #1',
            meeting: 'LLM 검증 회의',
            status: 'To Do',
            dueDate: offsetDate(1),
            checklist: [
                { text: '테스트 데이터셋 업데이트', done: false },
                { text: '지표 변동 모니터링', done: false }
            ]
        },
        {
            id: 4,
            title: '데이터 취합 자동화',
            project: 'Project #2',
            meeting: '주간 동기화',
            status: 'Paused',
            dueDate: offsetDate(7),
            checklist: [
                { text: '파이프라인 설계 공유', done: false },
                { text: '스케줄링 설정', done: false }
            ]
        }
    ],
    meetings: [
        {
            id: 'm1',
            title: '모델 Accuracy 회의',
            date: offsetDate(-1),
            time: '2:00pm - 3:00pm',
            summary: '새로운 벤치마크 결과를 공유하고 개선 방향을 확정했어요.',
            tasks: { total: 12, pending: 3 },
            duration: '1시간'
        },
        {
            id: 'm2',
            title: 'Voice Activity Detection 개선',
            date: offsetDate(-3),
            time: '11:00am - 11:30am',
            summary: '녹음/정리 오류에 대한 핫픽스와 QA 계획을 정리했어요.',
            tasks: { total: 7, pending: 1 },
            duration: '30분'
        },
        {
            id: 'm3',
            title: '프로젝트 A 킥오프',
            date: offsetDate(-6),
            time: '9:00am - 10:00am',
            summary: '리소스와 마일스톤을 확정하고 위험 요소를 체크했어요.',
            tasks: { total: 9, pending: 4 },
            duration: '1시간'
        }
    ],
    contributions: seedContributions(),
    today: new Date()
};

// 오늘 날짜에서 원하는 만큼 더하고 ISO 문자열(yyyy-mm-dd)만 반환
function offsetDate(delta) {
    const d = new Date();
    d.setDate(d.getDate() + delta);
    return d.toISOString().split('T')[0];
}

// 최근 60일의 랜덤 완료 횟수를 생성해 잔디 데이터로 사용
function seedContributions() {
    const map = new Map();
    for (let i = 0; i < 60; i++) {
        const date = offsetDate(-i);
        const count = Math.max(0, Math.floor(Math.random() * 4) - (i % 5 === 0 ? 1 : 0));
        if (count > 0) map.set(date, count);
    }
    return map;
}

// 자주 쓰는 DOM 요소 캐시
const elements = {
    greeting: document.getElementById('greeting'),
    sidebarLogin: document.getElementById('sidebar-login'),
    sidebarLogout: document.getElementById('sidebar-logout'),
    sidebarAuthTitle: document.getElementById('sidebar-auth-title'),

    heroProfile: document.getElementById('hero-profile'),
    heroAvatar: document.getElementById('hero-avatar'),

    projectSection: document.getElementById('project-section'),
    projectAdd: document.getElementById('add-project'),
    running: {
        percentage: document.getElementById('running-percentage'),
        completed: document.getElementById('running-completed'),
        total: document.getElementById('running-total'),
        ring: document.getElementById('running-ring')
    },
    contributionGrid: document.getElementById('contribution-grid'),
    meetingTrack: document.getElementById('meeting-track'),
    taskTrack: document.getElementById('task-track'),
    controls: document.querySelectorAll('.control[data-target]'),
    monthLabel: document.getElementById('month-label'),
    weekdayRow: document.getElementById('weekday-row'),
    calendarGrid: document.getElementById('calendar-grid'),
    prevMonth: document.getElementById('prev-month'),
    nextMonth: document.getElementById('next-month'),
    todayTitle: document.getElementById('today-title'),
    todayContent: document.getElementById('today-content')
};

// 로그인 상태에 따라 인사말과 버튼 문구 업데이트
function updateAuthUI() {
    if (state.loggedIn) {
        // 상단 타이틀
        elements.greeting.textContent = `Hi, ${state.userName}`;

        // 프로필 / 아바타 노출
        elements.heroProfile.style.display = 'flex';
        if (elements.heroAvatar) {
            elements.heroAvatar.textContent = state.userName.charAt(0).toUpperCase();
        }

        elements.sidebarAuthTitle.textContent = 'Log out';
        elements.sidebarLogin.style.display = 'none';
        elements.sidebarLogout.style.display = 'inline-flex';
    } else {
        // 상단 타이틀
        elements.greeting.textContent = '로그인 해주세요.';

        elements.heroProfile.style.display = 'flex';
        elements.heroAvatar.textContent = 'H';

        elements.sidebarAuthTitle.textContent = 'Log In';
        elements.sidebarLogin.style.display = 'inline-flex';
        elements.sidebarLogout.style.display = 'none';
    }
}

// 사이드바 프로젝트 링크 렌더링
function renderProjects() {
    elements.projectSection.querySelectorAll('.nav__item').forEach((el) => el.remove());
    state.projects.forEach((project) => {
        const link = document.createElement('a');
        link.className = 'nav__item';
        link.href = '#';
        link.textContent = project;
        elements.projectSection.appendChild(link);
    });
}

// 전체/완료/진행 중 태스크 통계 카드 갱신
function renderRunningTask() {
    const tasks = state.loggedIn ? state.tasks : [];
    const totalTasks = tasks.length;
    const completedTasks = tasks.filter((task) => isTaskCompleted(task)).length;
    const runningTasks = totalTasks - completedTasks;
    const percentage = totalTasks === 0 ? 0 : Math.round((completedTasks / totalTasks) * 100);

    elements.running.percentage.textContent = `${percentage}%`;
    elements.running.completed.textContent = completedTasks;
    elements.running.total.textContent = totalTasks;
    if (elements.running.ring) {
        elements.running.ring.style.background = `conic-gradient(#546fff ${percentage * 3.6}deg, #2d2d2d 0deg)`;
    }
}

// 체크리스트가 모두 완료되었는지 또는 상태가 완료인지 판단
function isTaskCompleted(task) {
    const allDone = task.checklist.every((item) => item.done);
    return task.status === 'Completed' || allDone;
}

// 완료 횟수를 기반으로 잔디 스타일 그리드 생성
function getContributionColumns() {
    if (!elements.contributionGrid) return 0;
    const styles = getComputedStyle(elements.contributionGrid);
    const cellSize = parseFloat(styles.getPropertyValue('--grid-cell-size')) || 16;
    const gap = parseFloat(styles.getPropertyValue('--grid-gap')) || 6;
    const width = elements.contributionGrid.clientWidth;
    const columns = Math.max(7, Math.floor((width + gap) / (cellSize + gap)));
    elements.contributionGrid.style.setProperty('--grid-columns', columns);
    return columns;
}

function renderContributionGrid() {
    const cells = [];
    const columns = getContributionColumns();
        const totalCells = Math.max(columns * 7, 0);
        for (let i = totalCells - 1; i >= 0; i--) {
        const date = offsetDate(-i);
        const count = state.loggedIn ? state.contributions.get(date) || 0 : 0;
        cells.push({ date, count });
    }
    elements.contributionGrid.innerHTML = '';
    cells.forEach(({ date, count }) => {
        const cell = document.createElement('div');
        cell.className = `cell level-${Math.min(4, count)}`;
        cell.title = `${date} · ${count} task`;
        elements.contributionGrid.appendChild(cell);
    });
}

// 최근 회의 정보를 카드 캐러셀로 렌더링
function renderMeetings() {
    elements.meetingTrack.innerHTML = '';
    const meetings = state.loggedIn ? state.meetings : [];
        const renderList = meetings.length > 0
            ? meetings
            : [
                {
                    title: '회의 제목',
                    date: '2025-09-28',
                    time: '10:00am - 11:00am',
                    summary: 'MoMatic로 간단 요약을 확인할 수 있어요.',
                    tasks: { total: 5, pending: 3 },
                    duration: '45분'
                },
                {
                    title: '회의 제목',
                    date: '2025-09-24',
                    time: '1:00pm - 2:00pm',
                    summary: '회의 결과를 놓치지 않도록 정리해요.',
                    tasks: { total: 5, pending: 3 },
                    duration: '1Hour'
                },
                {
                    title: '회의 제목',
                    date: '2025-09-20',
                    time: '3:00pm - 4:00pm',
                    summary: '오늘 회의록을 빠르게 확인할 수 있어요.',
                    tasks: { total: 5, pending: 3 },
                    duration: '45분'
                }
            ];
        renderList
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .slice(0, 3)
        .forEach((meeting) => {
            const card = document.createElement('div');
            card.className = 'meeting-card';
            card.innerHTML = `
                <div>
                    <p class="meeting-card__title">${meeting.title}</p>
                    <p class="meeting-card__meta">${meeting.date} · ${meeting.time}</p>
                </div>
                <p class="meeting-card__summary">${meeting.summary}</p>
                <div class="meeting-card__meta-group">
                    <div class="meeting-card__row">
                        <span class="meta-icon meta-icon--actions" aria-hidden="true">✓</span>
                        <div class="meeting-card__row-text">
                            <span class="meeting-card__label">Action Items</span>
                            <span class="meeting-card__value">${meeting.tasks.total}개 (${meeting.tasks.pending}개 미완료)</span>
                        </div>
                    </div>
                    <div class="meeting-card__row">
                        <span class="meta-icon meta-icon--time" aria-hidden="true">★</span>
                        <div class="meeting-card__row-text">
                            <span class="meeting-card__label">회의 시간</span>
                            <span class="meeting-card__value">${meeting.duration}</span>
                        </div>
                    </div>
                </div>
            `;
            elements.meetingTrack.appendChild(card);
        });
}

// 태스크 상태에 맞는 CSS 클래스 반환
function statusClass(status) {
    const map = {
        'To Do': 'todo',
        'On Going': 'ongoing',
        Completed: 'completed',
        Paused: 'paused'
    };
    return map[status] || 'todo';
}

// 예정된 할 일 목록을 카드와 체크리스트로 렌더링
function renderTasks() {
    elements.taskTrack.innerHTML = '';
        const tasks = state.loggedIn ? state.tasks : [];
        const renderList = tasks.length > 0
            ? tasks
            : [
                {
                    id: 'placeholder-1',
                    title: 'Task 제목(무엇을 할지)',
                    project: 'Project #1',
                    meeting: '어떤 소회의 / 프로젝트에서 나온 Task',
                    status: 'Completed',
                    dueDate: offsetDate(3),
                    checklist: [{ text: 'to-do item', done: false }]
                },
                {
                    id: 'placeholder-2',
                    title: 'Task 제목(무엇을 할지)',
                    project: 'Project #2',
                    meeting: '어떤 소회의 / 프로젝트에서 나온 Task',
                    status: 'On Going',
                    dueDate: offsetDate(7),
                    checklist: [{ text: 'to-do item', done: false }]
                }
            ];
        renderList
        .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
        .slice(0, 2)
        .forEach((task) => {
            const card = document.createElement('div');
            card.className = 'task-card';
            const due = new Date(task.dueDate);
            const remaining = Math.ceil((due - new Date()) / (1000 * 60 * 60 * 24));
            card.innerHTML = `
                <div class="task-card__header">
                    <span class="status ${statusClass(task.status)}">${task.status}</span>
                    <span class="meta">${remaining > 0 ? `${remaining} Days Left` : 'Due Today'}</span>
                </div>
                <div class="title">${task.title}</div>
                <div class="meta">${task.meeting} · ${task.project}</div>
                <div class="checklist" id="checklist-${task.id}"></div>
                <div class="deadline">${remaining > 0 ? `${remaining} Days Left` : 'Due Today'}</div>
            `;
            elements.taskTrack.appendChild(card);

            const checklistEl = card.querySelector(`#checklist-${task.id}`);
            task.checklist.slice(0, 2).forEach((item, idx) => {
                const id = `task-${task.id}-item-${idx}`;
                const label = document.createElement('label');
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.checked = item.done;
                checkbox.id = id;
                checkbox.addEventListener('change', () => toggleChecklist(task.id, idx, checkbox.checked));
                const text = document.createElement('span');
                text.textContent = item.text;
                if (item.done) text.style.textDecoration = 'line-through';
                checkbox.addEventListener('change', () => {
                    text.style.textDecoration = checkbox.checked ? 'line-through' : 'none';
                });
                label.appendChild(checkbox);
                label.appendChild(text);
                checklistEl.appendChild(label);
            });
        });
}

// 체크리스트 항목 토글 시 상태 업데이트 및 관련 UI 재렌더링
function toggleChecklist(taskId, itemIdx, checked) {
    const task = state.tasks.find((t) => t.id === taskId);
    if (!task) return;
    const item = task.checklist[itemIdx];
    const wasDone = item.done;
    item.done = checked;

    if (checked && !wasDone) {
        const today = state.today.toISOString().split('T')[0];
        const current = state.contributions.get(today) || 0;
        state.contributions.set(today, current + 1);
    }

    if (isTaskCompleted(task)) {
        task.status = 'Completed';
    } else if (task.status === 'Completed') {
        task.status = 'On Going';
    }

    renderRunningTask();
    renderContributionGrid();
    renderCalendar(currentMonth);
    renderTodayCard();
    renderTasks();
}

// 좌우 이동 버튼에 스크롤 이벤트 연결
function attachCarousel() {
    elements.controls.forEach((control) => {
        control.addEventListener('click', () => {
            const targetId = control.dataset.target;
            const direction = Number(control.dataset.direction);
            const track = document.getElementById(targetId);
            track.scrollBy({ left: 320 * direction, behavior: 'smooth' });
        });
    });
}

const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
let currentMonth = new Date();

// 월 이동 시 캘린더 격자와 날짜 표시 갱신
function renderCalendar(date = new Date()) {
    currentMonth = new Date(date);
    const month = currentMonth.getMonth();
    const year = currentMonth.getFullYear();
    elements.monthLabel.textContent = `${year}년 ${month + 1}월`;

    elements.weekdayRow.innerHTML = '';
    weekdays.forEach((day) => {
        const span = document.createElement('span');
        span.textContent = day;
        elements.weekdayRow.appendChild(span);
    });

    elements.calendarGrid.innerHTML = '';
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    for (let i = 0; i < firstDay; i++) {
        const filler = document.createElement('div');
        elements.calendarGrid.appendChild(filler);
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const btn = document.createElement('button');
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        btn.textContent = day;
        if (sameDate(dateStr, new Date())) btn.classList.add('today');
        if (state.contributions.has(dateStr)) btn.classList.add('has-task');
        elements.calendarGrid.appendChild(btn);
    }
}

// yyyy-mm-dd 문자열과 Date 객체가 같은 날짜인지 비교
function sameDate(dateStr, dateObj) {
    return dateStr === dateObj.toISOString().split('T')[0];
}

// 오늘 회의/할 일 유무에 따라 카드 타이틀, 배지, 리스트 구성
function renderTodayCard() {
    if (!state.loggedIn) {
        elements.todayTitle.textContent = '오늘 대표 회의 제목';
        elements.todayContent.innerHTML = `
            <div class="spotlight__section">
                <ul class="spotlight__list">
                    <li>
                        <div class="spotlight__pill">오늘 대표 회의록</div>
                        <div>프로젝트명 · 시간대</div>
                    </li>
                </ul>
            </div>
            <div class="spotlight__divider"></div>
            <div class="spotlight__section">
                <div class="spotlight__row">
                    <div class="spotlight__pill">오늘 할일</div>
                    <div class="spotlight__pill">On Going</div>
                </div>
                <ul class="spotlight__list">
                    <li><input type="checkbox" /> to-do item</li>
                    <li><input type="checkbox" /> to-do item</li>
                    <li><input type="checkbox" /> Indented to-do item</li>
                </ul>
            </div>
        `;
        return;
    }
    const todayStr = state.today.toISOString().split('T')[0];
    const todaysMeetings = state.meetings.filter((m) => m.date === todayStr);
    const todaysTasks = state.tasks.filter((t) => t.dueDate === todayStr || !isTaskCompleted(t));
    const hasMeetings = todaysMeetings.length > 0;
    const hasTasks = todaysTasks.length > 0;

    elements.todayContent.innerHTML = '';

    if (!hasMeetings && !hasTasks) {
        elements.todayTitle.textContent = '오늘은 일정이 없어요.';
        elements.todayContent.innerHTML = '<p>휴식을 취하고 다음 스프린트를 준비해 볼까요?</p>';
        return;
    }

    if (!hasMeetings && hasTasks) {
        elements.todayTitle.textContent = '오늘 회의는 없어요.';
        renderTaskList(todaysTasks, 'On Going');
        return;
    }

    if (hasMeetings && !hasTasks) {
        elements.todayTitle.textContent = '오늘 회의만 있어요.';
        renderMeetingSummary(todaysMeetings);
        return;
    }

    elements.todayTitle.textContent = '오늘 회의도 할 일도 있어요!';
    renderMeetingSummary(todaysMeetings);
    elements.todayContent.appendChild(createDivider());
    renderTaskList(todaysTasks, 'On Going');
    }

// 오늘 회의 목록을 스포트라이트 섹션으로 렌더링
function renderMeetingSummary(list) {
    const box = document.createElement('div');
    box.className = 'spotlight__section';
    const ul = document.createElement('ul');
    ul.className = 'spotlight__list';
    list.forEach((m) => {
        const li = document.createElement('li');
        li.innerHTML = `<div class="spotlight__pill">오늘 대표 회의록</div><div>${m.title} · ${m.time}</div>`;
        ul.appendChild(li);
    });
    box.appendChild(ul);
    elements.todayContent.appendChild(box);
}

// 오늘 할 일 목록을 스포트라이트 섹션으로 렌더링
function renderTaskList(list, statusLabel) {
    const box = document.createElement('div');
    box.className = 'spotlight__section';
    const header = document.createElement('div');
    header.className = 'spotlight__row';
    header.innerHTML = `
        <div class="spotlight__pill">오늘 할일</div>
        <div class="spotlight__pill">${statusLabel || 'On Going'}</div>
    `;
    const ul = document.createElement('ul');
    ul.className = 'spotlight__list';
    list.forEach((task) => {
        const li = document.createElement('li');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.checked = isTaskCompleted(task);
        const text = document.createElement('span');
        text.textContent = task.title;
        li.appendChild(checkbox);
        li.appendChild(text);
        ul.appendChild(li);
    });
    box.appendChild(header);
    box.appendChild(ul);
    elements.todayContent.appendChild(box);
}

function createDivider() {
    const divider = document.createElement('div');
    divider.className = 'spotlight__divider';
    return divider;
}

// 버튼/캐러셀 등 모든 이벤트 리스너 설정
function bindEvents() {
    elements.sidebarLogin.addEventListener('click', toggleLogin);
    elements.sidebarLogout.addEventListener('click', toggleLogin);
    elements.prevMonth.addEventListener('click', () => changeMonth(-1));
    elements.nextMonth.addEventListener('click', () => changeMonth(1));
    attachCarousel();
    window.addEventListener('resize', renderContributionGrid);
    if (elements.projectAdd) {
        elements.projectAdd.addEventListener('click', () => {
            window.location.href = 'project.html';
        });
    }
}

// 로그인/로그아웃 토글
function toggleLogin() {
    state.loggedIn = !state.loggedIn;
    updateAuthUI();
    renderRunningTask();
    renderContributionGrid();
    renderMeetings();
    renderTasks();
    renderTodayCard();
}

// 캘린더 월 이동 후 재렌더링
function changeMonth(delta) {
    const next = new Date(currentMonth);
    next.setMonth(next.getMonth() + delta);
    renderCalendar(next);
}

// 초기 렌더링 및 이벤트 바인딩 실행 진입점
function bootstrap() {
    updateAuthUI();
    renderProjects();
    renderRunningTask();
    renderContributionGrid();
    renderMeetings();
    renderTasks();
    renderCalendar(state.today);
    renderTodayCard();
    bindEvents();
    if (elements.contributionGrid && 'ResizeObserver' in window) {
        const observer = new ResizeObserver(() => renderContributionGrid());
        observer.observe(elements.contributionGrid);
    }
}

bootstrap();