/**
 * Mini LeetCode - Frontend Application
 *
 * Single-page application using vanilla JS + fetch() to call the REST APIs.
 * No framework dependencies - keeps the demo simple and deployable.
 */

// ============================================================
// Configuration
// ============================================================
const API_BASE = '/api'; // Relative path - works on any host/port

// ============================================================
// State
// ============================================================
const state = {
    currentPage: 'problems',
    problems: [],
    submissions: [],
    users: [],
    selectedProblem: null,
};

// ============================================================
// Navigation
// ============================================================

/**
 * Show a page by name and update the active nav link.
 * @param {string} pageName - one of 'problems', 'submissions', 'users'
 */
function showPage(pageName) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

    // Show target page
    document.getElementById(`page-${pageName}`)?.classList.add('active');
    document.querySelector(`[data-page="${pageName}"]`)?.classList.add('active');

    state.currentPage = pageName;

    // Load data for the page
    switch (pageName) {
        case 'problems':    loadProblems();    break;
        case 'submissions': loadSubmissions(); break;
        case 'users':       loadUsers();       break;
    }

    // Hide problem detail when switching pages
    if (pageName !== 'problems') hideProblemDetail();
}

// ============================================================
// API Helpers
// ============================================================

/**
 * Wrapper around fetch() that returns parsed JSON and handles errors.
 * @param {string} url
 * @param {object} options
 * @returns {Promise<any>}
 */
async function apiFetch(url, options = {}) {
    const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json', ...options.headers },
        ...options,
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        const message = data.error || data.message || `HTTP ${response.status}`;
        throw new Error(message);
    }

    return data;
}

// ============================================================
// Problems Page
// ============================================================

/** Load and render the problems table */
async function loadProblems() {
    showTableLoading('problems-tbody', 6);

    try {
        const problems = await apiFetch(`${API_BASE}/problems`);
        state.problems = problems;
        renderProblemsTable(problems);
        updateProblemStats(problems);
    } catch (err) {
        showTableError('problems-tbody', 6, err.message);
    }
}

/** Render the problems table with the given data */
function renderProblemsTable(problems) {
    const tbody = document.getElementById('problems-tbody');

    if (problems.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">
            <span class="empty-icon">📭</span>No problems found.
        </td></tr>`;
        return;
    }

    tbody.innerHTML = problems.map((p, idx) => `
        <tr onclick="showProblemDetail(${p.id})">
            <td style="color:var(--text-muted);font-size:0.85rem">${idx + 1}</td>
            <td class="problem-title-cell">${escapeHtml(p.title)}</td>
            <td>${difficultyBadge(p.difficulty)}</td>
            <td>${renderTags(p.tags)}</td>
            <td style="color:${acceptanceColor(p.acceptanceRate)}">${p.acceptanceRate?.toFixed(1) ?? 0}%</td>
            <td style="color:var(--text-muted)">${p.totalSubmissions?.toLocaleString() ?? 0}</td>
        </tr>
    `).join('');
}

/** Update the stats row above the table */
function updateProblemStats(problems) {
    const counts = { EASY: 0, MEDIUM: 0, HARD: 0 };
    problems.forEach(p => { if (counts[p.difficulty] !== undefined) counts[p.difficulty]++; });

    setText('stat-easy',   counts.EASY);
    setText('stat-medium', counts.MEDIUM);
    setText('stat-hard',   counts.HARD);
    setText('stat-total',  problems.length);
}

/** Filter problems table by search input and/or difficulty select */
function filterProblems() {
    const search     = document.getElementById('search-input')?.value.toLowerCase() ?? '';
    const difficulty = document.getElementById('difficulty-filter')?.value ?? '';

    const filtered = state.problems.filter(p => {
        const matchesSearch = !search ||
            p.title.toLowerCase().includes(search) ||
            (p.tags || '').toLowerCase().includes(search);
        const matchesDiff = !difficulty || p.difficulty === difficulty;
        return matchesSearch && matchesDiff;
    });

    renderProblemsTable(filtered);
}

// ============================================================
// Problem Detail + Submit
// ============================================================

/** Show the problem detail panel for the given problem ID */
async function showProblemDetail(problemId) {
    const problem = state.problems.find(p => p.id === problemId) ||
                    await apiFetch(`${API_BASE}/problems/${problemId}`).catch(() => null);

    if (!problem) { showToast('Problem not found', 'error'); return; }

    state.selectedProblem = problem;

    // Populate the detail panel
    setText('detail-title',       `#${problem.id} · ${problem.title}`);
    setHtml('detail-difficulty',  difficultyBadge(problem.difficulty));
    setText('detail-description', problem.description);
    setText('detail-constraints', problem.constraints || 'None');
    setText('detail-example-in',  problem.exampleInput  || '-');
    setText('detail-example-out', problem.exampleOutput || '-');
    setHtml('detail-tags',        renderTags(problem.tags));

    // Pre-fill the submit form
    document.getElementById('submit-problem-id').value = problem.id;

    // Show the detail section and scroll to it
    const detailEl = document.getElementById('problem-detail');
    detailEl.classList.add('visible');
    detailEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function hideProblemDetail() {
    document.getElementById('problem-detail')?.classList.remove('visible');
    state.selectedProblem = null;
}

/** Handle the Submit Solution form */
async function handleSubmit(event) {
    event.preventDefault();

    const userId    = document.getElementById('submit-user-id')?.value?.trim();
    const problemId = document.getElementById('submit-problem-id')?.value;
    const language  = document.getElementById('submit-language')?.value;
    const code      = document.getElementById('submit-code')?.value?.trim();

    if (!userId || isNaN(userId)) { showToast('Please enter a valid User ID', 'error'); return; }
    if (!code)                    { showToast('Please enter your code', 'error');         return; }

    const btn = document.getElementById('submit-btn');
    btn.disabled = true;
    btn.textContent = 'Submitting...';

    try {
        const result = await apiFetch(`${API_BASE}/submissions`, {
            method: 'POST',
            body: JSON.stringify({
                userId:    parseInt(userId),
                problemId: parseInt(problemId),
                language,
                code,
            }),
        });

        const statusLabel = result.status.replace(/_/g, ' ');
        const toastType   = result.status === 'ACCEPTED' ? 'success' : 'error';
        showToast(`${statusLabel} · ${result.executionTimeMs}ms`, toastType);

        // If on submissions page, refresh the table
        if (state.currentPage === 'submissions') loadSubmissions();

    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled    = false;
        btn.textContent = 'Submit Solution';
    }
}

// ============================================================
// Submissions Page
// ============================================================

/** Load and render the submissions table */
async function loadSubmissions() {
    showTableLoading('submissions-tbody', 7);

    try {
        // Load all submissions (we grab by querying all problems then merging)
        // For simplicity in the demo, we load recent submissions by fetching all
        const submissions = await apiFetch(`${API_BASE}/submissions/problem/1`)
            .catch(() => []);

        // Fetch submissions for all problems and merge
        const allSubs = await fetchAllSubmissions();
        state.submissions = allSubs;
        renderSubmissionsTable(allSubs);
    } catch (err) {
        showTableError('submissions-tbody', 7, err.message);
    }
}

/** Fetch submissions from multiple problem IDs and merge them */
async function fetchAllSubmissions() {
    // Use user-based fetching for the demo (users 1-5)
    const promises = [1, 2, 3, 4, 5].map(uid =>
        apiFetch(`${API_BASE}/submissions/user/${uid}`).catch(() => [])
    );
    const results = await Promise.all(promises);
    // Flatten and sort by submittedAt descending
    return results.flat().sort((a, b) =>
        new Date(b.submittedAt) - new Date(a.submittedAt)
    );
}

/** Filter submissions based on search/filter inputs */
async function filterSubmissions() {
    const userId    = document.getElementById('sub-user-filter')?.value?.trim();
    const statusVal = document.getElementById('sub-status-filter')?.value;

    showTableLoading('submissions-tbody', 7);

    try {
        let subs;
        if (userId && !isNaN(userId)) {
            subs = await apiFetch(`${API_BASE}/submissions/user/${userId}`);
        } else {
            subs = state.submissions;
        }

        if (statusVal) {
            subs = subs.filter(s => s.status === statusVal);
        }

        renderSubmissionsTable(subs);
    } catch (err) {
        showTableError('submissions-tbody', 7, err.message);
    }
}

/** Render the submissions table */
function renderSubmissionsTable(submissions) {
    const tbody = document.getElementById('submissions-tbody');

    if (submissions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">
            <span class="empty-icon">📭</span>No submissions found.
        </td></tr>`;
        return;
    }

    tbody.innerHTML = submissions.slice(0, 100).map(s => `
        <tr>
            <td style="color:var(--text-muted);font-size:0.85rem">${s.id}</td>
            <td style="color:var(--text-secondary)">${escapeHtml(s.username || `User #${s.userId}`)}</td>
            <td>${escapeHtml(s.problemTitle || `Problem #${s.problemId}`)}</td>
            <td><span class="badge badge-${languageClass(s.language)}">${escapeHtml(s.language)}</span></td>
            <td>${statusBadge(s.status)}</td>
            <td style="color:var(--text-muted)">${s.executionTimeMs > 0 ? s.executionTimeMs + ' ms' : '-'}</td>
            <td style="color:var(--text-muted);font-size:0.82rem">${formatDate(s.submittedAt)}</td>
        </tr>
    `).join('');
}

/** Load stats for a specific user */
async function loadUserStats() {
    const userId = document.getElementById('sub-user-filter')?.value?.trim();
    if (!userId || isNaN(userId)) {
        showToast('Enter a valid User ID to view stats', 'error');
        return;
    }

    try {
        const stats = await apiFetch(`${API_BASE}/submissions/user/${userId}/stats`);
        const panel = document.getElementById('stats-panel');

        panel.innerHTML = `
            <div class="panel">
                <h2>📊 Stats for <em>${escapeHtml(stats.username)}</em></h2>
                <div class="stats-row" style="margin:0">
                    <div class="stat-card stat-total">
                        <span class="stat-value">${stats.totalSubmissions}</span>
                        <span class="stat-label">Total</span>
                    </div>
                    <div class="stat-card stat-easy">
                        <span class="stat-value">${stats.acceptedSubmissions}</span>
                        <span class="stat-label">Accepted</span>
                    </div>
                    <div class="stat-card stat-medium">
                        <span class="stat-value">${stats.uniqueProblemsSolved}</span>
                        <span class="stat-label">Solved</span>
                    </div>
                    <div class="stat-card stat-hard">
                        <span class="stat-value">${stats.acceptanceRate}%</span>
                        <span class="stat-label">Acc. Rate</span>
                    </div>
                </div>
            </div>
        `;
        panel.style.display = 'block';
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ============================================================
// Users Page
// ============================================================

/** Load and render the users table */
async function loadUsers() {
    showTableLoading('users-tbody', 6);

    try {
        const users = await apiFetch(`${API_BASE}/users`);
        state.users = users;
        renderUsersTable(users);
    } catch (err) {
        showTableError('users-tbody', 6, err.message);
    }
}

/** Render the users table */
function renderUsersTable(users) {
    const tbody = document.getElementById('users-tbody');

    if (users.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No users found.</td></tr>`;
        return;
    }

    tbody.innerHTML = users.map(u => `
        <tr>
            <td style="color:var(--text-muted);font-size:0.85rem">${u.id}</td>
            <td style="font-weight:600">${escapeHtml(u.username)}</td>
            <td style="color:var(--text-secondary)">${escapeHtml(u.email)}</td>
            <td><span class="badge ${u.role === 'ADMIN' ? 'badge-hard' : 'badge-easy'}">${u.role}</span></td>
            <td style="font-weight:600;color:var(--accent)">${u.totalSolved}</td>
            <td style="color:var(--text-secondary)">${escapeHtml(u.rank)}</td>
        </tr>
    `).join('');
}

// ============================================================
// Register Modal
// ============================================================

function openRegisterModal() {
    document.getElementById('register-modal')?.classList.add('visible');
}

function closeRegisterModal() {
    document.getElementById('register-modal')?.classList.remove('visible');
    document.getElementById('register-form')?.reset();
}

async function handleRegister(event) {
    event.preventDefault();

    const dto = {
        username: document.getElementById('reg-username').value.trim(),
        email:    document.getElementById('reg-email').value.trim(),
        password: document.getElementById('reg-password').value,
    };

    const btn = document.getElementById('register-btn');
    btn.disabled    = true;
    btn.textContent = 'Registering...';

    try {
        await apiFetch(`${API_BASE}/users/register`, {
            method: 'POST',
            body: JSON.stringify(dto),
        });
        showToast(`User "${dto.username}" registered successfully!`, 'success');
        closeRegisterModal();
        if (state.currentPage === 'users') loadUsers();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled    = false;
        btn.textContent = 'Register';
    }
}

// ============================================================
// UI Helpers
// ============================================================

function difficultyBadge(difficulty) {
    const map = { EASY: 'easy', MEDIUM: 'medium', HARD: 'hard' };
    const cls = map[difficulty] || 'easy';
    return `<span class="badge badge-${cls}">${difficulty}</span>`;
}

function statusBadge(status) {
    const map = {
        ACCEPTED:     'accepted',
        WRONG_ANSWER: 'wrong',
        TIME_LIMIT:   'tle',
        COMPILE_ERROR:'ce',
    };
    const cls   = map[status] || 'wrong';
    const label = status?.replace(/_/g, ' ') || '?';
    return `<span class="badge badge-${cls}">${label}</span>`;
}

function languageClass(lang) {
    // Return a consistent badge style for any language string
    return 'easy'; // Keep it simple - just use the easy (teal) style
}

function renderTags(tagsStr) {
    if (!tagsStr) return '';
    return tagsStr.split(',')
        .map(t => `<span class="tag">${escapeHtml(t.trim())}</span>`)
        .join('');
}

function acceptanceColor(rate) {
    if (rate >= 60) return 'var(--easy)';
    if (rate >= 40) return 'var(--medium)';
    return 'var(--hard)';
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function setHtml(id, html) {
    const el = document.getElementById(id);
    if (el) el.innerHTML = html;
}

/** Replace a table body with a loading spinner row */
function showTableLoading(tbodyId, colspan) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    tbody.innerHTML = `<tr><td colspan="${colspan}" class="loading">
        <div class="spinner"></div><br>Loading...
    </td></tr>`;
}

/** Replace a table body with an error message */
function showTableError(tbodyId, colspan, message) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    tbody.innerHTML = `<tr><td colspan="${colspan}" class="empty-state">
        <span style="color:var(--hard)">⚠ ${escapeHtml(message)}</span>
    </td></tr>`;
}

/** Show a toast notification */
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${icons[type] || ''}</span> ${escapeHtml(message)}`;
    container?.appendChild(toast);

    // Auto-remove after 4 seconds
    setTimeout(() => toast.remove(), 4000);
}

// ============================================================
// Initialisation
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // Wire up nav links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => showPage(link.dataset.page));
    });

    // Load the default page
    showPage('problems');
});
