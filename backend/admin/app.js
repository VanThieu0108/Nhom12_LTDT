const API = '/api/admin';
const tokenKey = 'kids-adventure-admin-token';
let token = sessionStorage.getItem(tokenKey);
let toastTimer;
const pageNames = {
  overview: 'Tổng quan',
  users: 'Người chơi',
  vocabulary: 'Câu hỏi',
  pets: 'Thú cưng'
};

const $ = (selector) => document.querySelector(selector);
const escapeText = (value) => String(value ?? '');
const dateText = (value) => value ? new Date(value).toLocaleString('vi-VN') : '—';
const zoneNames = { listening: 'Nghe', speaking: 'Nói', reading: 'Đọc', writing: 'Viết' };
const rarityNames = { common: 'Thường', rare: 'Hiếm', epic: 'Sử thi', legendary: 'Huyền thoại' };
const rarityClass = { common: 'green', rare: '', epic: 'orange', legendary: 'orange' };

function showToast(message) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('show'), 3000);
}

async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (token) headers.set('Authorization', `Bearer ${token}`);
  if (options.body) headers.set('Content-Type', 'application/json');
  const response = await fetch(`${API}${path}`, { ...options, headers });
  if (response.status === 401 && token) {
    logout();
    throw new Error('Phiên quản trị đã hết hạn. Vui lòng đăng nhập lại.');
  }
  if (response.status === 204) return null;
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error?.message || 'Yêu cầu không thành công.');
  return data;
}

function setAuthenticated(authenticated) {
  $('#login-view').hidden = authenticated;
  $('#admin-view').hidden = !authenticated;
  if (authenticated) {
    $('#today-label').textContent = new Date().toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
    openPage('overview');
  }
}

function logout() {
  token = null;
  sessionStorage.removeItem(tokenKey);
  setAuthenticated(false);
}

function openPage(page) {
  document.querySelectorAll('.page-section').forEach((section) => { section.hidden = section.id !== `page-${page}`; });
  document.querySelectorAll('.nav-item').forEach((button) => button.classList.toggle('active', button.dataset.page === page));
  $('#page-title').textContent = pageNames[page];
  if (page === 'overview') loadOverview();
  if (page === 'users') loadUsers();
  if (page === 'vocabulary') loadVocabulary();
  if (page === 'pets') loadPets();
}

function showError(error) {
  showToast(error.message || 'Đã xảy ra lỗi.');
}

async function loadOverview() {
  try {
    const data = await api('/overview');
    const cards = [
      ['Người chơi', data.counts.users, 'Tài khoản đang hoạt động', '♙'],
      ['Câu hỏi', data.counts.questions, 'Trong ngân hàng học tập', '▤'],
      ['Thú cưng', data.counts.pets, 'Có thể mở khóa trong game', '♧'],
      ['Bài đã hoàn thành', data.counts.completions, `${data.counts.minigame_scores} lượt chơi minigame`, '☆']
    ];
    $('#stat-grid').replaceChildren(...cards.map(([label, value, note, icon]) => {
      const card = document.createElement('article');
      card.className = 'stat-card';
      const badge = document.createElement('span');
      badge.className = 'stat-icon';
      badge.textContent = icon;
      const title = document.createElement('div');
      title.className = 'stat-label';
      title.textContent = label;
      const count = document.createElement('div');
      count.className = 'stat-value';
      count.textContent = Number(value).toLocaleString('vi-VN');
      const sub = document.createElement('div');
      sub.className = 'stat-note';
      sub.textContent = note;
      card.append(badge, title, count, sub);
      return card;
    }));
    const body = $('#recent-scores');
    body.replaceChildren(...data.recentScores.map((item) => {
      const row = document.createElement('tr');
      const cells = [item.explorer_name, ({ word_match: 'Ghép từ', runner: 'Chạy từ vựng', meteor: 'Thiên thạch' })[item.game_type] || item.game_type, item.score, `${item.distance} m`, dateText(item.created_at)];
      cells.forEach((value) => {
        const cell = document.createElement('td');
        cell.textContent = escapeText(value);
        row.append(cell);
      });
      return row;
    }));
    if (!data.recentScores.length) body.innerHTML = '<tr><td colspan="5" class="empty-state">Chưa có lượt chơi nào.</td></tr>';
  } catch (error) { showError(error); }
}

async function loadUsers() {
  try {
    const search = encodeURIComponent($('#user-search').value.trim());
    const data = await api(`/users?search=${search}&limit=100`);
    const body = $('#users-table');
    body.replaceChildren(...data.users.map((user) => {
      const row = document.createElement('tr');
      const values = [user.explorer_name, user.coins, `${user.stamina}/5`, user.experience, user.trophy_points, user.completed_levels, dateText(user.created_at)];
      values.forEach((value, index) => {
        const cell = document.createElement('td');
        if (index === 0) {
          const strong = document.createElement('strong');
          strong.textContent = escapeText(value);
          cell.append(strong);
        } else cell.textContent = escapeText(value);
        row.append(cell);
      });
      return row;
    }));
    $('#users-empty').hidden = data.users.length > 0;
  } catch (error) { showError(error); }
}

async function loadVocabulary() {
  try {
    const search = encodeURIComponent($('#question-search').value.trim());
    const zone = encodeURIComponent($('#zone-filter').value);
    const data = await api(`/vocabulary?search=${search}&zone=${zone}&limit=100`);
    const body = $('#questions-table');
    body.replaceChildren(...data.questions.map((question) => {
      const row = document.createElement('tr');
      const values = [zoneNames[question.zone], question.level, question.prompt, question.answer, question.translation];
      values.forEach((value, index) => {
        const cell = document.createElement('td');
        cell.textContent = escapeText(value);
        if (index === 0) {
          const badge = document.createElement('span');
          badge.className = 'badge';
          badge.textContent = escapeText(value);
          cell.replaceChildren(badge);
        }
        row.append(cell);
      });
      const actions = document.createElement('td');
      actions.className = 'row-actions';
      const edit = document.createElement('button');
      edit.className = 'text-btn';
      edit.textContent = 'Sửa';
      edit.addEventListener('click', () => openQuestionDialog(question));
      const remove = document.createElement('button');
      remove.className = 'text-btn danger';
      remove.textContent = 'Xóa';
      remove.addEventListener('click', () => deleteQuestion(question));
      actions.append(edit, remove);
      row.append(actions);
      return row;
    }));
    $('#questions-empty').hidden = data.questions.length > 0;
  } catch (error) { showError(error); }
}

async function loadPets() {
  try {
    const data = await api('/pets');
    const grid = $('#pet-grid');
    grid.replaceChildren(...data.pets.map((pet) => {
      const card = document.createElement('article');
      card.className = 'pet-card';
      const art = document.createElement('div');
      art.className = 'pet-art';
      if (pet.image_url) {
        const image = document.createElement('img');
        image.src = pet.image_url;
        image.alt = pet.name;
        image.loading = 'lazy';
        art.append(image);
      } else art.textContent = '🐾';
      const info = document.createElement('div');
      info.className = 'pet-info';
      const name = document.createElement('strong');
      name.textContent = pet.name;
      const species = document.createElement('small');
      species.textContent = `${pet.species} · ${pet.owners} người sở hữu`;
      const rarity = document.createElement('span');
      rarity.className = `badge ${rarityClass[pet.rarity] || ''}`;
      rarity.textContent = rarityNames[pet.rarity];
      info.append(name, species, rarity);
      const actions = document.createElement('div');
      actions.className = 'pet-card-actions';
      const edit = document.createElement('button');
      edit.className = 'text-btn';
      edit.textContent = 'Sửa';
      edit.addEventListener('click', () => openPetDialog(pet));
      const remove = document.createElement('button');
      remove.className = 'text-btn danger';
      remove.textContent = 'Xóa';
      remove.addEventListener('click', () => deletePet(pet));
      actions.append(edit, remove);
      card.append(art, info, actions);
      return card;
    }));
    $('#pets-empty').hidden = data.pets.length > 0;
  } catch (error) { showError(error); }
}

function openQuestionDialog(question = null) {
  const form = $('#question-form');
  form.reset();
  form.elements.id.value = question?.id || '';
  form.elements.zone.value = question?.zone || 'listening';
  form.elements.level.value = question?.level || 1;
  form.elements.prompt.value = question?.prompt || '';
  form.elements.answer.value = question?.answer || '';
  form.elements.translation.value = question?.translation || '';
  form.elements.choices.value = question?.choices?.join('\n') || '';
  form.elements.audioUrl.value = question?.audio_url || '';
  $('#question-dialog-title').textContent = question ? 'Chỉnh sửa câu hỏi' : 'Thêm câu hỏi';
  $('#question-form-error').textContent = '';
  $('#question-dialog').showModal();
}

function openPetDialog(pet = null) {
  const form = $('#pet-form');
  form.reset();
  form.elements.id.value = pet?.id || '';
  form.elements.name.value = pet?.name || '';
  form.elements.species.value = pet?.species || '';
  form.elements.rarity.value = pet?.rarity || 'common';
  form.elements.imageUrl.value = pet?.image_url || '';
  $('#pet-dialog-title').textContent = pet ? 'Chỉnh sửa thú cưng' : 'Thêm thú cưng';
  $('#pet-form-error').textContent = '';
  $('#pet-dialog').showModal();
}

async function deleteQuestion(question) {
  if (!confirm(`Xóa câu hỏi “${question.prompt}”?`)) return;
  try {
    await api(`/vocabulary/${question.id}`, { method: 'DELETE' });
    showToast('Đã xóa câu hỏi.');
    loadVocabulary();
  } catch (error) { showError(error); }
}

async function deletePet(pet) {
  if (!confirm(`Xóa thú cưng “${pet.name}”? Thao tác sẽ bị từ chối nếu người chơi đang sở hữu thú cưng này.`)) return;
  try {
    await api(`/pets/${pet.id}`, { method: 'DELETE' });
    showToast('Đã xóa thú cưng.');
    loadPets();
  } catch (error) { showError(error); }
}

$('#login-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  $('#login-error').textContent = '';
  try {
    const data = await api('/login', {
      method: 'POST',
      body: JSON.stringify({ username: form.elements.username.value, password: form.elements.password.value })
    });
    token = data.accessToken;
    sessionStorage.setItem(tokenKey, token);
    form.reset();
    setAuthenticated(true);
  } catch (error) {
    $('#login-error').textContent = error.message;
  }
});

$('#logout-btn').addEventListener('click', logout);
$('#main-nav').addEventListener('click', (event) => {
  const button = event.target.closest('[data-page]');
  if (button) openPage(button.dataset.page);
});
$('#user-search').addEventListener('input', debounce(loadUsers, 250));
$('#question-search').addEventListener('input', debounce(loadVocabulary, 250));
$('#zone-filter').addEventListener('change', loadVocabulary);
$('#add-question-btn').addEventListener('click', () => openQuestionDialog());
$('#add-pet-btn').addEventListener('click', () => openPetDialog());
document.querySelectorAll('[data-close]').forEach((button) => button.addEventListener('click', () => $(`#${button.dataset.close}`).close()));

$('#question-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const id = form.elements.id.value;
  const choices = form.elements.choices.value.split(/\r?\n/).map((choice) => choice.trim()).filter(Boolean);
  const audioUrl = form.elements.audioUrl.value.trim();
  const payload = {
    zone: form.elements.zone.value,
    level: Number(form.elements.level.value),
    prompt: form.elements.prompt.value.trim(),
    answer: form.elements.answer.value.trim(),
    translation: form.elements.translation.value.trim(),
    choices,
    audioUrl: audioUrl || null
  };
  try {
    await api(`/vocabulary${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
    $('#question-dialog').close();
    showToast(id ? 'Đã cập nhật câu hỏi.' : 'Đã thêm câu hỏi.');
    loadVocabulary();
    loadOverview();
  } catch (error) { $('#question-form-error').textContent = error.message; }
});

$('#pet-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const id = form.elements.id.value;
  const imageUrl = form.elements.imageUrl.value.trim();
  const payload = {
    name: form.elements.name.value.trim(),
    species: form.elements.species.value.trim(),
    rarity: form.elements.rarity.value,
    imageUrl: imageUrl || null
  };
  try {
    await api(`/pets${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
    $('#pet-dialog').close();
    showToast(id ? 'Đã cập nhật thú cưng.' : 'Đã thêm thú cưng.');
    loadPets();
    loadOverview();
  } catch (error) { $('#pet-form-error').textContent = error.message; }
});

function debounce(callback, delay) {
  let timeout;
  return (...args) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => callback(...args), delay);
  };
}

setAuthenticated(Boolean(token));
