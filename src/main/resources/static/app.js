const API_BASE = 'http://localhost:9999/api';

let currentPlayer = null;

function showTab(tabName, clickEvent) {
    // Hide all tabs
    for (const tab of document.querySelectorAll('.tab-content')) {
        tab.classList.remove('active');
    }
    for (const btn of document.querySelectorAll('.tab-btn')) {
        btn.classList.remove('active');
    }
    
    // Show selected tab
    document.getElementById(`${tabName}-tab`).classList.add('active');
    clickEvent.target.classList.add('active');
}

function showLoading(show) {
    document.getElementById('loading').style.display = show ? 'block' : 'none';
}

function showError(message) {
    const errorDiv = document.getElementById('error');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

function showResults(show) {
    document.getElementById('results').style.display = show ? 'block' : 'none';
}

async function searchPlayer() {
    const gameName = document.getElementById('gameName').value.trim();
    const tagLine = document.getElementById('tagLine').value.trim();
    const region = document.getElementById('region').value;
    
    if (!gameName || !tagLine) {
        showError('⚠️ Veuillez entrer un nom de joueur et un tag');
        return;
    }
    
    currentPlayer = { gameName, tagLine, region };
    
    showLoading(true);
    showResults(false);
    
    try {
        await Promise.all([
            loadPlayerSummary(gameName, tagLine, region),
            loadTopChampions(gameName, tagLine, region),
            loadRecentMatches(gameName, tagLine)
        ]);
        
        showResults(true);
    } catch (error) {
        showError(`❌ Erreur: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

async function loadPlayerSummary(gameName, tagLine, region) {
    const response = await fetch(
        `${API_BASE}/player-summary?gameName=${encodeURIComponent(gameName)}&tagLine=${encodeURIComponent(tagLine)}&region=${region}&language=en_US`
    );
    
    if (!response.ok) {
        throw new Error('Joueur non trouvé');
    }
    
    const summary = await response.text();
    
    document.getElementById('playerName').textContent = `${gameName} #${tagLine}`;
    document.getElementById('playerInfo').textContent = summary;
}

async function loadTopChampions(gameName, tagLine, region) {
    const response = await fetch(
        `${API_BASE}/top-champions?gameName=${encodeURIComponent(gameName)}&tagLine=${encodeURIComponent(tagLine)}&region=${region}&count=6&language=en_US`
    );
    
    if (!response.ok) {
        throw new Error('Impossible de charger les champions');
    }
    
    const championsText = await response.text();
    const championsGrid = document.getElementById('championsGrid');
    championsGrid.innerHTML = '';
    
    // Parse the text response
    const lines = championsText.split('\n').filter(line => line.trim());
    
    const championRegex = /- (.+): Level (\d+), ([\d,]+) pts/;
    for (const line of lines) {
        // Format: "- ChampName: Level X, Y pts"
        const match = championRegex.exec(line);
        if (match) {
            const [, name, level, points] = match;
            const card = document.createElement('div');
            card.className = 'champion-card';
            card.innerHTML = `
                <h3>${name}</h3>
                <div class="level">🏆 Niveau ${level}</div>
                <div class="points">⭐ ${points} points</div>
            `;
            championsGrid.appendChild(card);
        }
    }
}

async function loadRecentMatches(gameName, tagLine) {
    const response = await fetch(
        `${API_BASE}/recent-matches?gameName=${encodeURIComponent(gameName)}&tagLine=${encodeURIComponent(tagLine)}&count=5`
    );
    
    if (!response.ok) {
        throw new Error('Impossible de charger les matchs');
    }
    
    const matchesText = await response.text();
    const matchesList = document.getElementById('matchesList');
    matchesList.innerHTML = '';
    
    // Parse the text response
    const lines = matchesText.split('\n').filter(line => line.trim());
    
    const matchRegex = /(.+?) (.+?): (\d+)\/(\d+)\/(\d+) - (Win|Loss)/;
    for (const line of lines) {
        // Format: "matchId ChampName: K/D/A - Result"
        const match = matchRegex.exec(line);
        if (match) {
            const [, matchId, champion, kills, deaths, assists, result] = match;
            const card = document.createElement('div');
            card.className = `match-card ${result.toLowerCase()}`;
            card.innerHTML = `
                <div class="match-header">
                    <div class="match-champion">${champion}</div>
                    <div class="match-result ${result.toLowerCase()}">${result === 'Win' ? '✅ Victoire' : '❌ Défaite'}</div>
                </div>
                <div class="match-stats">
                    <span>⚔️ ${kills} / 💀 ${deaths} / 🤝 ${assists}</span>
                    <span>KDA: ${deaths > 0 ? ((Number.parseInt(kills, 10) + Number.parseInt(assists, 10)) / Number.parseInt(deaths, 10)).toFixed(2) : 'Perfect'}</span>
                </div>
                <div class="match-id">Match ID: ${matchId}</div>
            `;
            matchesList.appendChild(card);
        }
    }
}

// Allow Enter key to search
document.getElementById('gameName').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') searchPlayer();
});

document.getElementById('tagLine').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') searchPlayer();
});
