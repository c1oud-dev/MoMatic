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

function offsetDate(delta) {
    const d = new Date();
    d.setDate(d.getDate() + delta);
    return d.toISOString().split('T')[0];
}

function seedContributions() {
    const map = new Map();
    for (let i = 0; i < 60; i++) {
        const date = offsetDate(-i);
        const count = Math.max(0, Math.floor(Math.random() * 4) - (i % 5 === 0 ? 1 : 0));
        if (count > 0) map.set(date, count);
    }
    return map;
}

const elements = {
    greeting: document.getElementById('greeting'),
    subheadline: document.getElementById('subheadline'),
    loginToggle: document.getElementById('login-toggle'),
    sidebarLogin: document.getElementById('sidebar-login'),
    projectSection: document.getElementById('project-section'),
    running: {
        count: document.getElementById('running-count'),
        percentage: document.getElementById('running-percentage'),
        completed: document.getElementById('running-completed'),
        total: document.getElementById('running-total')
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
    todayStatus: document.getElementById('today-status'),
    todayContent: document.getElementById('today-content')
};

function updateAuthUI() {
    if (state.loggedIn) {
        elements.greeting.textContent = `Hi, ${state.userName}`;
        elements.subheadline.textContent = "Let's finish your task today!";
        elements.loginToggle.textContent = '로그아웃';
        elements.sidebarLogin.textContent = '로그아웃';
        } else {
        elements.greeting.textContent = '로그인 해주세요.';
                elements.subheadline.textContent = 'Summarize every meeting and manage your schedule in one place.';
                elements.loginToggle.textContent = 'Google로 로그인';
                elements.sidebarLogin.textContent = 'Google로 로그인';
            }
        }

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

function renderRunningTask() {
    const totalTasks = state.tasks.length;
    const completedTasks = state.tasks.filter((task) => isTaskCompleted(task)).length;
    const runningTasks = totalTasks - completedTasks;
    const percentage = totalTasks === 0 ? 0 : Math.round((completedTasks / totalTasks) * 100);

    elements.running.count.textContent = runningTasks;
    elements.running.percentage.textContent = `${percentage}%`;
    elements.running.completed.textContent = `${completedTasks}개`;
    elements.running.total.textContent = `${totalTasks}개`;
}

function isTaskCompleted(task) {
    const allDone = task.checklist.every((item) => item.done);
    return task.status === 'Completed' || allDone;
}

function renderContributionGrid() {
    const cells = [];
    for (let i = 62; i >= 0; i--) {
        const date = offsetDate(-i);
        const count = state.contributions.get(date) || 0;
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

function renderMeetings() {
    elements.meetingTrack.innerHTML = '';
    state.meetings
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .forEach((meeting) => {
            const card = document.createElement('div');
            card.className = 'meeting-card';
            card.innerHTML = `
                <div class="title">${meeting.title}</div>
                <div class="meta">${meeting.date} · ${meeting.time}</div>
                <div class="summary">${meeting.summary}</div>
                <div class="meta-row">
                    <span class="badge">Actions ${meeting.tasks.total}</span>
                    <span>미완료 ${meeting.tasks.pending}개</span>
                    <span>총 ${meeting.duration}</span>
                </div>
            `;
            elements.meetingTrack.appendChild(card);
        });
}

function statusClass(status) {
    const map = {
        'To Do': 'todo',
        'On Going': 'ongoing',
        Completed: 'completed',
        Paused: 'paused'
    };
    return map[status] || 'todo';
}

function renderTasks() {
    elements.taskTrack.innerHTML = '';
    state.tasks
        .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
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
                <div class="deadline">마감: ${task.dueDate}</div>
            `;
            elements.taskTrack.appendChild(card);

            const checklistEl = card.querySelector(`#checklist-${task.id}`);
            task.checklist.forEach((item, idx) => {
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

function sameDate(dateStr, dateObj) {
    return dateStr === dateObj.toISOString().split('T')[0];
}

function renderTodayCard() {
    const todayStr = state.today.toISOString().split('T')[0];
    const todaysMeetings = state.meetings.filter((m) => m.date === todayStr);
    const todaysTasks = state.tasks.filter((t) => t.dueDate === todayStr || !isTaskCompleted(t));
    const hasMeetings = todaysMeetings.length > 0;
    const hasTasks = todaysTasks.length > 0;

    elements.todayContent.innerHTML = '';

    if (!hasMeetings && !hasTasks) {
        elements.todayTitle.textContent = '오늘은 일정이 없어요.';
        elements.todayStatus.textContent = '-';
        elements.todayContent.innerHTML = '<p>휴식을 취하고 다음 스프린트를 준비해 볼까요?</p>';
        return;
    }

    if (!hasMeetings && hasTasks) {
        elements.todayTitle.textContent = '오늘 회의는 없어요.';
        elements.todayStatus.textContent = '할 일 집중';
        renderTaskList(todaysTasks);
        return;
    }

    if (hasMeetings && !hasTasks) {
        elements.todayTitle.textContent = '오늘 회의만 있어요.';
        elements.todayStatus.textContent = '회의 집중';
        renderMeetingSummary(todaysMeetings);
        return;
    }

    elements.todayTitle.textContent = '오늘 회의도 할 일도 있어요!';
        elements.todayStatus.textContent = '바쁜 하루';
        renderMeetingSummary(todaysMeetings);
        renderTaskList(todaysTasks);
    }

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

function renderTaskList(list) {
    const box = document.createElement('div');
    box.className = 'spotlight__section';
    const ul = document.createElement('ul');
    ul.className = 'spotlight__list';
    list.forEach((task) => {
        const li = document.createElement('li');
        li.innerHTML = `<span class="spotlight__pill">${task.status}</span> ${task.title}`;
        ul.appendChild(li);
    });
    box.appendChild(ul);
    elements.todayContent.appendChild(box);
}

function bindEvents() {
    elements.loginToggle.addEventListener('click', toggleLogin);
    elements.sidebarLogin.addEventListener('click', toggleLogin);
    elements.prevMonth.addEventListener('click', () => changeMonth(-1));
    elements.nextMonth.addEventListener('click', () => changeMonth(1));
    attachCarousel();
}

function toggleLogin() {
    state.loggedIn = !state.loggedIn;
    updateAuthUI();
}

function changeMonth(delta) {
    const next = new Date(currentMonth);
    next.setMonth(next.getMonth() + delta);
    renderCalendar(next);
}

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
}

bootstrap();