const meetingList = document.getElementById('meeting-list');
const actionList = document.getElementById('action-list');
const transcriptList = document.getElementById('transcript');
const meetingTitle = document.getElementById('meeting-title');
const timelineMeta = document.getElementById('timeline-meta');

async function fetchMeetings() {
    const res = await fetch('/api/meetings');
    if (!res.ok) throw new Error('회의 목록을 불러오지 못했습니다.');
    return res.json();
}

async function fetchMeeting(id) {
    const res = await fetch(`/api/meetings/${id}`);
    if (!res.ok) throw new Error('회의 상세를 불러오지 못했습니다.');
    return res.json();
}

function formatDateRange(start, end) {
    const startDate = new Date(start);
    const endDate = new Date(end);
    return `${startDate.toLocaleDateString('ko-KR')} ${startDate.toLocaleTimeString('ko-KR', {hour:'2-digit', minute:'2-digit'})} - ${endDate.toLocaleTimeString('ko-KR', {hour:'2-digit', minute:'2-digit'})}`;
}

function renderMeetings(meetings) {
    meetingList.innerHTML = '';
    meetings.forEach((m) => {
        const card = document.createElement('div');
        card.className = 'meeting-card';
        card.dataset.id = m.id;
        card.innerHTML = `
            <div class="meta">${formatDateRange(m.startedAt, m.endedAt)}</div>
            <div class="title">${m.title}</div>
            <div class="meta">${m.summary ?? '요약 대기중'}</div>
        `;
        card.addEventListener('click', () => selectMeeting(m.id, card));
        meetingList.appendChild(card);
    });
    if (meetings.length) {
        selectMeeting(meetings[0].id, meetingList.firstChild);
    } else {
        meetingList.innerHTML = '<p class="empty">등록된 회의가 없습니다.</p>';
    }
}

function renderActions(actionItems) {
    actionList.innerHTML = '';
    if (!actionItems.length) {
        actionList.innerHTML = '<p class="empty">액션 아이템이 없습니다.</p>';
        return;
    }
    actionItems.forEach((item) => {
        const row = document.createElement('div');
        row.className = 'action-item';
        const statusClass = item.status === 'DONE' ? 'done' : item.status === 'IN_PROGRESS' ? 'in-progress' : 'todo';
        row.innerHTML = `
            <div>
                <div class="title">${item.task}</div>
                <div class="due">담당자 ${item.assignee} · 마감 ${item.dueDate}</div>
            </div>
            <div class="status ${statusClass}">${item.status.replace('_',' ')}</div>
        `;
        actionList.appendChild(row);
    });
}

function renderTranscript(entries) {
    transcriptList.innerHTML = '';
    if (!entries.length) {
        transcriptList.innerHTML = '<p class="empty">전사 데이터가 없습니다.</p>';
        return;
    }
    entries.forEach((t) => {
        const row = document.createElement('div');
        row.className = 'transcript-entry';
        row.innerHTML = `
            <div class="speaker">${t.speaker}</div>
            <div class="time">${t.startSec}s - ${t.endSec}s</div>
            <div class="content">${t.content}</div>
        `;
        transcriptList.appendChild(row);
    });
}

async function selectMeeting(id, cardElement) {
    document.querySelectorAll('.meeting-card').forEach((c) => c.classList.remove('active'));
    if (cardElement) cardElement.classList.add('active');

    const detail = await fetchMeeting(id);
    meetingTitle.textContent = detail.meeting.title;
    timelineMeta.textContent = `${new Date(detail.meeting.startedAt).toLocaleDateString('ko-KR')} · ${detail.actionItems.length}개 액션`;
    renderActions(detail.actionItems);
    renderTranscript(detail.transcripts);
}

async function bootstrap() {
    try {
        const meetings = await fetchMeetings();
        renderMeetings(meetings);
    } catch (err) {
        meetingList.innerHTML = `<p class="empty">${err.message}</p>`;
    }
}

document.getElementById('refresh-btn').addEventListener('click', bootstrap);

bootstrap();