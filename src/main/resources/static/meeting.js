const meetings = [
  {
    id: 1,
    project: 'AI 특허발표 시연',
    date: '2024-12-27',
    time: '21:24',
    type: '대면',
    summaryDone: true,
    tasks: { count: 5, label: 'Task S/W' },
    headline: '출시 일정 조율 및 알파 버전 피드백 수집',
    summary: '클라이언트 일정 확인 및 기능 확정, 정리된 요구사항 기반으로 개발 진행하기로 함. 디자인 QA를 12/30까지 마무리하고, 1월 첫째 주에 알파 배포 및 테스트 진행.',
    audio: 'assets/audio/meeting-sample.wav',
    duration: '12:48',
    transcript: [
      '클라이언트 일정 확인 및 기능 확정, 정리된 요구사항 기반으로 개발 진행하기로 함.',
      '디자인 QA를 12/30까지 마무리하고, 1월 첫째 주에 알파 배포 및 테스트 진행.',
      '업데이트된 플로우 차트는 금요일 오전까지 공유, 음성 분석 정확도 92% 유지 목표.'
    ],
    tasksList: [
      { title: '디자인 QA 진행 및 스크린샷 정리', tag: 'Design' },
      { title: '알파 배포 플랜 공유 & QA 일정 확정', tag: 'Task S/W' },
      { title: '음성 분석 정확도 리포트 업데이트', tag: 'AI' }
    ]
  },
  {
    id: 2,
    project: '모바일 리뉴얼',
    date: '2024-12-30',
    time: '11:00',
    type: '원격',
    summaryDone: true,
    tasks: { count: 3, label: 'Task S/W' },
    headline: '신규 온보딩 화면 콘텐츠 확정',
    summary: '신규 온보딩 화면 문구 확정 및 A/B 테스트 일정 조율. 1월 둘째 주에 실험 시작, KPI는 가입 전환율 8%p 개선 목표.',
    audio: 'assets/audio/meeting-sample.wav',
    duration: '08:21',
    transcript: [
      '온보딩 화면 문구 3종 중 안 A 채택, 이미지 에셋 교체 필요.',
      '실험 목표는 가입 전환율 8%p 개선, 1월 둘째 주 시작.',
      '개발 QA는 1/8까지 완료, 가이드 문서는 1/5 배포.'
    ],
    tasksList: [
      { title: '온보딩 문구 최종 반영 및 번역 검수', tag: 'UX Writing' },
      { title: '이미지 에셋 교체 및 용량 점검', tag: 'Design' },
      { title: 'A/B 테스트 플랜 공유', tag: 'Task S/W' }
    ]
  },
  {
    id: 3,
    project: '가을 리서치 결과 공유',
    date: '2024-12-28',
    time: '15:12',
    type: '대면',
    summaryDone: false,
    tasks: { count: 7, label: 'Task S/W' },
    headline: '연말 사용자 리서치 결과 리뷰',
    summary: '리서치 인사이트 정리 중. 제품 로드맵 반영 항목을 월말까지 우선순위화 필요.',
    audio: 'assets/audio/meeting-sample.wav',
    duration: '14:05',
    transcript: [
      '참여자 12명 인터뷰 중 8명은 신규 기능 A에 긍정적.',
      '결제 흐름에서 주요 이탈 지점 2곳 확인, 개선 아이디어 수집.',
      '고객지원팀 요청 사항은 별도 문서로 정리 예정.'
    ],
    tasksList: [
      { title: '인사이트 문서 초안 완성', tag: 'Research' },
      { title: '로드맵 반영 항목 우선순위 합의', tag: 'Planning' },
      { title: '고객지원팀 요청 정리', tag: 'Support' }
    ]
  },
  {
    id: 4,
    project: '파트너사 NDA 서명',
    date: '2024-12-21',
    time: '08:45',
    type: '대면',
    summaryDone: true,
    tasks: { count: 2, label: 'Task S/W' },
    headline: '법무 검토 사항 확인',
    summary: '파트너사 NDA 초안 서명 완료. 세부 조항 2건 추가 협의 후 정식 서명 진행 예정.',
    audio: 'assets/audio/meeting-sample.wav',
    duration: '06:14',
    transcript: [
      '법무 검토 완료, 가이드라인 공유.',
      '세부 조항 2건 추가 협의 후 서명 일정 확정.'
    ],
    tasksList: [
      { title: '추가 조항 검토 회신', tag: 'Legal' },
      { title: '서명 일정 확정 및 캘린더 초대', tag: 'Task S/W' }
    ]
  },
  {
    id: 5,
    project: '연말 프로모션 기획',
    date: '2024-12-20',
    time: '22:10',
    type: '원격',
    summaryDone: false,
    tasks: { count: 4, label: 'Task S/W' },
    headline: '채널별 예산 배분 조율',
    summary: '예산 배분 초안 리뷰 중. 광고 세트 별 KPI를 12/29까지 업데이트해야 함.',
    audio: 'assets/audio/meeting-sample.wav',
    duration: '10:32',
    transcript: [
      '인플루언서 채널 예산 상향 필요.',
      '리타게팅 캠페인별 CPA 기준 재설정.',
      '성과 리포트 포맷 공유 예정.'
    ],
    tasksList: [
      { title: '예산 배분표 업데이트', tag: 'Marketing' },
      { title: '리타게팅 KPI 재정의', tag: 'Analytics' }
    ]
  },
  {
    id: 6,
    project: 'AI 보이스봇 고도화',
    date: '2024-12-29',
    time: '18:30',
    type: '원격',
    summaryDone: true,
    tasks: { count: 6, label: 'Task S/W' },
    headline: '정확도 및 응답속도 개선 계획',
    summary: 'STT 모델 버전 교체 일정 확정, 응답 지연을 25% 줄이는 실험 진행. 장애 대응 플레이북 업데이트 필요.',
    audio: 'assets/audio/meeting-sample.wav',
    duration: '09:55',
    transcript: [
      'STT 모델 교체 일정: 1/10 적용 목표.',
      '지연 시간 개선 실험: 12/31까지 25% 감소 목표.',
      '장애 대응 플레이북 개정 필요.'
    ],
    tasksList: [
      { title: '모델 교체 체크리스트 작성', tag: 'AI' },
      { title: '응답 지연 실험 모니터링', tag: 'Infra' },
      { title: '플레이북 업데이트', tag: 'Task S/W' }
    ]
  }
];

const state = {
  sort: 'newest',
  filter: null,
  query: '',
  selected: null
};

const bodyEl = document.getElementById('meeting-body');
const listCountEl = document.getElementById('list-count');
const searchEl = document.getElementById('search');
const chipEls = document.querySelectorAll('.chip');
const detailTitle = document.getElementById('detail-title');
const detailDate = document.getElementById('detail-date');
const detailHeading = document.getElementById('detail-heading');
const detailSummary = document.getElementById('detail-summary');
const detailFilename = document.getElementById('detail-filename');
const detailDuration = document.getElementById('detail-duration');
const detailAudio = document.getElementById('detail-audio');
const taskListEl = document.getElementById('task-list');

function formatDate(date, time) {
  const [y, m, d] = date.split('-');
  return `${y}. ${m}. ${d}. ${time}`;
}

function applyFilters() {
  let filtered = [...meetings];
  if (state.filter === 'summary') {
    filtered = filtered.filter((m) => m.summaryDone);
  }
  if (state.filter === 'pending') {
    filtered = filtered.filter((m) => !m.summaryDone);
  }
  if (state.query.trim()) {
    const q = state.query.toLowerCase();
    filtered = filtered.filter(
      (m) =>
        m.project.toLowerCase().includes(q) ||
        m.headline.toLowerCase().includes(q) ||
        m.summary.toLowerCase().includes(q)
    );
  }
  filtered.sort((a, b) => (state.sort === 'newest' ? b.id - a.id : a.id - b.id));
  return filtered;
}

function renderList() {
  const filtered = applyFilters();
  bodyEl.innerHTML = '';

  if (!filtered.length) {
    bodyEl.innerHTML = `<tr><td colspan="5" class="empty">조건에 맞는 회의가 없습니다.</td></tr>`;
    listCountEl.textContent = '0 entries';
    return;
  }

  filtered.forEach((meeting) => {
    const row = document.createElement('tr');
    row.dataset.id = meeting.id;
    if (state.selected === meeting.id) {
      row.classList.add('selected');
    }

    row.innerHTML = `
      <td>
        <div class="count">${meeting.project}</div>
        <div class="small">${meeting.headline}</div>
      </td>
      <td>${meeting.date}</td>
      <td><span class="badge badge--outline">${meeting.type}</span></td>
      <td>
        <span class="badge ${meeting.summaryDone ? 'badge--green' : 'badge--orange'}">${meeting.summaryDone ? '완료' : '진행 중'}</span>
      </td>
      <td>
        <span class="badge badge--outline">${meeting.tasks.count}개</span>
        <span class="badge badge--outline">${meeting.tasks.label}</span>
      </td>
    `;

    row.addEventListener('click', () => selectMeeting(meeting.id));
    bodyEl.appendChild(row);
  });

  listCountEl.textContent = `Showing ${filtered.length} of ${meetings.length} entries`;
}

function renderTasks(items) {
  taskListEl.innerHTML = '';
  items.forEach((task) => {
    const item = document.createElement('label');
    item.className = 'task-item';
    item.innerHTML = `
      <input type="checkbox" aria-label="${task.title}">
      <div>
        <p class="task-item__title">${task.title}</p>
        <p class="small">${task.tag} 관련</p>
      </div>
      <span class="task-item__tag">${task.tag}</span>
    `;
    taskListEl.appendChild(item);
  });
}

function selectMeeting(id) {
  state.selected = id;
  renderList();
  const meeting = meetings.find((m) => m.id === id);
  if (!meeting) return;

  detailTitle.textContent = meeting.project;
  detailDate.textContent = formatDate(meeting.date, meeting.time);
  detailHeading.textContent = meeting.headline;
  detailSummary.textContent = `${meeting.transcript.join('\n')}`;
  detailFilename.textContent = `${meeting.project}_${meeting.date}.wav`;
  detailDuration.textContent = meeting.duration;
  detailAudio.src = meeting.audio;
  renderTasks(meeting.tasksList);
}

searchEl.addEventListener('input', (e) => {
  state.query = e.target.value;
  renderList();
});

chipEls.forEach((chip) => {
  chip.addEventListener('click', () => {
    const sortValue = chip.dataset.sort;
    const filterValue = chip.dataset.filter;

    if (sortValue) {
      state.sort = sortValue;
      document.querySelectorAll('[data-sort]').forEach((c) => c.classList.remove('active'));
      chip.classList.add('active');
    }

    if (filterValue) {
      state.filter = state.filter === filterValue ? null : filterValue;
      document.querySelectorAll('[data-filter]').forEach((c) => c.classList.toggle('active', state.filter === c.dataset.filter));
    }

    renderList();
    if (!state.selected && applyFilters().length) {
      selectMeeting(applyFilters()[0].id);
    }
  });
});

renderList();
if (applyFilters().length) {
  selectMeeting(applyFilters()[0].id);
}