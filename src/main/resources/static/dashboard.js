// Tab functionality - Updated for page-based routing
function initializePage() {
    // Get the active tab from the server-side attribute using a script tag
    const activeTabScript = document.querySelector('script[data-active-tab]');
    let activeTab = 'explore';

    if (activeTabScript) {
        activeTab = activeTabScript.getAttribute('data-active-tab');
    } else {
        // Fallback: try to get from URL
        const path = window.location.pathname;
        if (path.includes('/dailylogs')) activeTab = 'dailylogs';
        else if (path.includes('/invitations')) activeTab = 'invitations';
        else if (path.includes('/profile')) activeTab = 'profile';
        else if (path.includes('/mycoach')) activeTab = 'mycoach';
        else if (path.includes('/myathletes')) activeTab = 'myathletes';
        else if (path.includes('/sponsorships')) activeTab = 'sponsorships';
    }

    // Show the correct tab content
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => content.classList.remove('active'));

    const activeTabElement = document.getElementById(activeTab);
    if (activeTabElement) {
        activeTabElement.classList.add('active');
    }

    // Show/hide search and filter nav based on active tab
    const searchFilterNav = document.getElementById('searchFilterNav');
    if (searchFilterNav) {
        if (activeTab === 'explore') {
            searchFilterNav.style.display = 'block';
        } else {
            searchFilterNav.style.display = 'none';
        }
    }

    // Load data based on active tab
    if (activeTab === 'dailylogs') {
        loadDailyLogs();
        loadTodaysSummary();
        loadAthleteStats();
        loadCharts();
        initializeChartPeriodButtons();
    } else if (activeTab === 'profile') {
        loadProfileData();
        initializeProfileHandlers();
    }
}

// Modal functionality
const newPostBtn = document.getElementById('newPostBtn');
const newPostModal = document.getElementById('newPostModal');
const closeModal = document.getElementById('closeModal');
const cancelPost = document.getElementById('cancelPost');
const newPostForm = document.getElementById('newPostForm');

// Daily Log Modal functionality
const addLogBtn = document.getElementById('addLogBtn');
const dailyLogModal = document.getElementById('dailyLogModal');
const closeDailyLogModal = document.getElementById('closeDailyLogModal');
const cancelDailyLog = document.getElementById('cancelDailyLog');
const dailyLogForm = document.getElementById('dailyLogForm');

newPostBtn.addEventListener('click', () => {
    newPostModal.classList.add('active');
});

// Daily Log Modal Events
if (addLogBtn) {
    addLogBtn.addEventListener('click', () => {
        dailyLogModal.classList.add('active');
    });
}

[closeModal, cancelPost].forEach(btn => {
    btn.addEventListener('click', () => {
        newPostModal.classList.remove('active');
        newPostForm.reset();
    });
});

// Daily Log Modal close events
if (closeDailyLogModal && cancelDailyLog) {
    [closeDailyLogModal, cancelDailyLog].forEach(btn => {
        btn.addEventListener('click', () => {
            dailyLogModal.classList.remove('active');
            dailyLogForm.reset();
        });
    });
}

// Close modals when clicking outside
newPostModal.addEventListener('click', (e) => {
    if (e.target === newPostModal) {
        newPostModal.classList.remove('active');
        newPostForm.reset();
    }
});

if (dailyLogModal) {
    dailyLogModal.addEventListener('click', (e) => {
        if (e.target === dailyLogModal) {
            dailyLogModal.classList.remove('active');
            dailyLogForm.reset();
        }
    });
}

// Handle form submission
newPostForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(newPostForm);

    try {
        const response = await fetch('/api/posts/create', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            // Close modal and reset form
            newPostModal.classList.remove('active');
            newPostForm.reset();

            // Show success message
            showMessage('Post created successfully!', 'success');

            // Reload page to show new post
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showMessage(result.message || 'Failed to create post', 'error');
        }
    } catch (error) {
        console.error('Error creating post:', error);
        showMessage('An error occurred while creating the post', 'error');
    }
});

// Function to show messages
function showMessage(message, type) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `fixed top-4 right-4 p-4 rounded z-50 ${
        type === 'success'
            ? 'bg-green-100 border border-green-400 text-green-700'
            : 'bg-red-100 border border-red-400 text-red-700'
    }`;
    messageDiv.textContent = message;

    document.body.appendChild(messageDiv);

    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

// Handle post action buttons
document.addEventListener('click', async function(e) {
    // Handle delete post button - check both the button and its child elements
    if (e.target.classList.contains('delete-post-btn') || e.target.closest('.delete-post-btn')) {
        e.preventDefault();
        const deleteBtn = e.target.classList.contains('delete-post-btn') ? e.target : e.target.closest('.delete-post-btn');
        const postId = deleteBtn.getAttribute('data-post-id');

        if (confirm('Are you sure you want to delete this post?')) {
            try {
                const response = await fetch(`/api/posts/${postId}`, {
                    method: 'DELETE'
                });

                const result = await response.json();

                if (result.success) {
                    // Remove the post from DOM
                    const postElement = document.querySelector(`[data-post-id="${postId}"]`);
                    if (postElement) {
                        postElement.remove();
                    }
                    showMessage('Post deleted successfully!', 'success');
                } else {
                    showMessage(result.message || 'Failed to delete post', 'error');
                }
            } catch (error) {
                console.error('Error deleting post:', error);
                showMessage('An error occurred while deleting the post', 'error');
            }
        }
        return;
    }

    // Handle like button
    if (e.target.classList.contains('like-btn') || e.target.closest('.like-btn')) {
        e.preventDefault();
        const likeBtn = e.target.classList.contains('like-btn') ? e.target : e.target.closest('.like-btn');
        const postId = likeBtn.getAttribute('data-post-id');

        try {
            const response = await fetch(`/api/posts/${postId}/like`, {
                method: 'POST'
            });

            const result = await response.json();

            if (result.success) {
                // Update button state
                const likeIcon = likeBtn.querySelector('.like-icon');
                const likeText = likeBtn.querySelector('.like-text');
                const likeCount = likeBtn.querySelector('.like-count');

                if (result.liked) {
                    // User liked the post
                    likeBtn.classList.remove('bg-green-500', 'hover:bg-green-600');
                    likeBtn.classList.add('bg-blue-500', 'hover:bg-blue-600');
                    likeIcon.classList.add('text-red-300');
                    likeText.textContent = 'Liked';
                    likeBtn.setAttribute('data-liked', 'true');
                } else {
                    // User unliked the post
                    likeBtn.classList.remove('bg-blue-500', 'hover:bg-blue-600');
                    likeBtn.classList.add('bg-green-500', 'hover:bg-green-600');
                    likeIcon.classList.remove('text-red-300');
                    likeText.textContent = 'Like';
                    likeBtn.setAttribute('data-liked', 'false');
                }

                likeCount.textContent = `(${result.likesCount})`;
            } else {
                showMessage(result.message || 'Failed to update like', 'error');
            }
        } catch (error) {
            console.error('Error toggling like:', error);
            showMessage('An error occurred while updating like', 'error');
        }
        return;
    }

    // Handle other buttons
    if (e.target.textContent.includes('Invite')) {
        showMessage('Invitation sent!', 'success');
    } else if (e.target.textContent.includes('Report')) {
        showMessage('Post reported. Thank you for helping keep our community safe.', 'success');
    }
});

// Search and filter functionality
const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');
const clearSearchBtn = document.getElementById('clearSearchBtn');
const filterTags = document.querySelectorAll('.filter-tag');
let currentFilter = 'all';

// Function to render posts in the container
function renderPosts(posts, message = null) {
    const postsContainer = document.getElementById('postsContainer');
    postsContainer.innerHTML = '';

    if (posts.length === 0) {
        postsContainer.innerHTML = `
                    <div class="col-span-full text-center py-12">
                        <div class="text-gray-500 text-lg">${message || 'No posts found'}</div>
                        <p class="text-gray-400 text-sm mt-2">Try adjusting your search or filter criteria</p>
                    </div>
                `;
        return;
    }

    posts.forEach(post => {
        const postElement = document.createElement('div');
        postElement.className = 'bg-white p-4 rounded-lg shadow-md';
        postElement.setAttribute('data-post-id', post.id);

        // Use global variable set in HTML for current user id
        const currentUser = window.currentUserId;
        const isOwner = post.user && post.user.id === currentUser;
        const userLikes = post.userLikes || [];
        const isLiked = userLikes.some(like => like.id === currentUser);

        postElement.innerHTML = `
                    <div class="flex items-center justify-between mb-3">
                        <div class="flex items-center">
                            <img src="${post.user?.profileImageUrl || 'https://via.placeholder.com/40'}"
                                 alt="User" class="w-10 h-10 rounded-full mr-3 object-cover">
                            <div>
                                <h3 class="font-semibold text-sm">${post.user?.firstName || ''} ${post.user?.lastName || ''}</h3>
                                <p class="text-gray-500 text-xs">${post.user?.role || ''}</p>
                            </div>
                        </div>
                        ${isOwner ? `
                            <button class="delete-post-btn text-red-500 hover:text-red-700 text-sm"
                                    data-post-id="${post.id}" title="Delete post">
                                <i class="fas fa-trash"></i>
                            </button>
                        ` : ''}
                    </div>

                    <h4 class="text-md font-semibold mb-2">${post.title}</h4>
                    <p class="mb-3 text-sm text-gray-700">${post.description}</p>

                    ${post.imageUrl ? `
                        <div class="mb-3">
                            <img src="${post.imageUrl}" alt="Post Image" class="w-full h-32 object-cover rounded">
                        </div>
                    ` : ''}

                    <div class="text-xs text-gray-400 mb-3">${new Date(post.postedAt).toLocaleDateString()}</div>

                    <div class="flex space-x-2 text-sm">
                        <button class="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600 text-xs flex items-center">
                            <i class="fas fa-user-plus mr-1"></i> Invite
                        </button>
                        <button class="like-btn ${isLiked ? 'bg-blue-500 hover:bg-blue-600' : 'bg-green-500 hover:bg-green-600'} text-white px-3 py-1 rounded text-xs flex items-center space-x-1"
                                data-post-id="${post.id}" data-liked="${isLiked}">
                            <i class="like-icon fas fa-heart ${isLiked ? 'text-red-300' : ''}"></i>
                            <span class="like-text">${isLiked ? 'Liked' : 'Like'}</span>
                            <span class="like-count">(${userLikes.length})</span>
                        </button>
                        <button class="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600 text-xs flex items-center">
                            <i class="fas fa-flag mr-1"></i> Report
                        </button>
                    </div>
                `;

        postsContainer.appendChild(postElement);
    });
}

// Debounce function to limit search API calls
function debounce(func, delay) {
    let timeout;
    return function(...args) {
        const context = this;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), delay);
    };
}

// Search posts function
const performSearch = async () => {
    const query = searchInput.value.trim();

    if (!query) {
        // If search query is empty, show all posts or current filter
        if (currentFilter === 'all') {
            window.location.reload();
        } else {
            filterByType(currentFilter);
        }
        return;
    }

    try {
        const response = await fetch(`/api/posts/search?query=${encodeURIComponent(query)}`);
        const result = await response.json();

        if (result.success) {
            renderPosts(result.posts, result.message);
            showMessage(`Found ${result.posts.length} posts matching "${query}"`, 'success');
        } else {
            showMessage(result.message || 'Failed to search posts', 'error');
        }
    } catch (error) {
        console.error('Error searching posts:', error);
        showMessage('An error occurred while searching posts', 'error');
    }
};

// Filter posts by type
const filterByType = async (type) => {
    try {
        const response = await fetch(`/api/posts/filter?type=${encodeURIComponent(type)}`);
        const result = await response.json();

        if (result.success) {
            renderPosts(result.posts, result.message);
            currentFilter = type;
        } else {
            showMessage(result.message || 'Failed to filter posts', 'error');
        }
    } catch (error) {
        console.error('Error filtering posts:', error);
        showMessage('An error occurred while filtering posts', 'error');
    }
};

// Search button click
searchBtn.addEventListener('click', (e) => {
    e.preventDefault();
    performSearch();
});

// Enter key in search input
searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        performSearch();
    }
});

// Clear search button
clearSearchBtn.addEventListener('click', (e) => {
    e.preventDefault();
    searchInput.value = '';
    clearSearchBtn.style.display = 'none';

    // Reset to current filter or all posts
    if (currentFilter === 'all') {
        window.location.reload();
    } else {
        filterByType(currentFilter);
    }
});

// Show/hide clear button based on input
searchInput.addEventListener('input', (e) => {
    const query = e.target.value.trim();
    clearSearchBtn.style.display = query ? 'inline-block' : 'none';
});

// Filter tag clicks
filterTags.forEach(tag => {
    tag.addEventListener('click', async (e) => {
        e.preventDefault();
        const type = e.target.getAttribute('data-type');

        // Update active filter tag appearance
        filterTags.forEach(t => {
            t.classList.remove('bg-blue-500', 'text-white');
            t.classList.add('bg-gray-200', 'text-gray-800');
        });
        e.target.classList.remove('bg-gray-200', 'text-gray-800');
        e.target.classList.add('bg-blue-500', 'text-white');

        // Clear search when filtering
        searchInput.value = '';
        clearSearchBtn.style.display = 'none';

        // Filter posts
        await filterByType(type);
    });
});

// Initialize "All" filter as active
document.querySelector('.filter-tag[data-type="all"]').classList.add('bg-blue-500', 'text-white');
document.querySelector('.filter-tag[data-type="all"]').classList.remove('bg-gray-200', 'text-gray-800');

// Daily Log functionality
if (dailyLogForm) {
    dailyLogForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(dailyLogForm);

        try {
            const response = await fetch('/api/dailylogs/create', {
                method: 'POST',
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                // Close modal and reset form
                dailyLogModal.classList.remove('active');
                dailyLogForm.reset();

                // Show success message
                showMessage('Training log saved successfully!', 'success');

                // Refresh daily logs data
                loadDailyLogs();
                loadTodaysSummary();
                loadAthleteStats();
                loadCharts();
            } else {
                showMessage(result.message || 'Failed to save training log', 'error');
            }
        } catch (error) {
            console.error('Error creating daily log:', error);
            showMessage('An error occurred while saving the training log', 'error');
        }
    });
}

// Function to load daily logs
async function loadDailyLogs() {
    try {
        const response = await fetch('/api/dailylogs/my-logs');
        const result = await response.json();

        if (result.success) {
            renderDailyLogs(result.dailyLogs);
        } else {
            console.error('Failed to load daily logs:', result.message);
        }
    } catch (error) {
        console.error('Error loading daily logs:', error);
    }
}

// Function to load today's summary
async function loadTodaysSummary() {
    try {
        const response = await fetch('/api/dailylogs/today');
        const result = await response.json();

        if (result.success) {
            updateTodaysSummary(result.dailyLogs, result.totalDuration);
        } else {
            console.error('Failed to load today\'s summary:', result.message);
        }
    } catch (error) {
        console.error('Error loading today\'s summary:', error);
    }
}

// Function to load athlete stats (streak and total duration)
async function loadAthleteStats() {
    try {
        const response = await fetch('/api/dailylogs/stats');
        const result = await response.json();

        if (result.success) {
            updateAthleteStats(result);
        } else {
            console.error('Failed to load athlete stats:', result.message);
        }
    } catch (error) {
        console.error('Error loading athlete stats:', error);
    }
}

// Function to update athlete statistics display
function updateAthleteStats(stats) {
    const currentStreakEl = document.getElementById('currentStreak');
    const totalLifetimeDurationEl = document.getElementById('totalLifetimeDuration');

    if (currentStreakEl) {
        currentStreakEl.textContent = `${stats.currentStreak || 0} days`;
    }

    if (totalLifetimeDurationEl) {
        const hours = Math.floor((stats.totalLifetimeDuration || 0) / 60);
        const minutes = (stats.totalLifetimeDuration || 0) % 60;
        if (hours > 0) {
            totalLifetimeDurationEl.textContent = `${hours}h ${minutes}m`;
        } else {
            totalLifetimeDurationEl.textContent = `${minutes} min`;
        }
    }
}

// Function to render daily logs
function renderDailyLogs(dailyLogs) {
    const dailyLogsList = document.getElementById('dailyLogsList');

    if (!dailyLogsList) return;

    if (dailyLogs.length === 0) {
        dailyLogsList.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="fas fa-dumbbell text-4xl mb-4"></i>
                <p>No training logs yet. Start by adding your first workout!</p>
            </div>
        `;
        return;
    }

    dailyLogsList.innerHTML = dailyLogs.map(log => `
        <div class="border-b border-gray-200 py-4 last:border-b-0" data-log-id="${log.id}">
            <div class="flex justify-between items-start">
                <div class="flex-1">
                    <div class="flex items-center space-x-3 mb-2">
                        <div class="bg-blue-100 p-2 rounded-full">
                            <i class="fas fa-dumbbell text-blue-600"></i>
                        </div>
                        <div>
                            <h4 class="font-semibold text-gray-900">${log.trainingType}</h4>
                            <p class="text-sm text-gray-500">${formatDateTime(log.createdAt)}</p>
                        </div>
                    </div>
                    <div class="ml-11">
                        <p class="text-sm text-gray-600 mb-1">
                            <i class="fas fa-clock mr-1"></i>
                            Duration: ${log.trainingDurationMinutes} minutes
                        </p>
                        ${log.notes ? `<p class="text-sm text-gray-600 italic">"${log.notes}"</p>` : ''}
                        ${log.sport ? `<p class="text-xs text-blue-600 mt-1">Sport: ${log.sport.name}</p>` : ''}
                    </div>
                </div>
                <button class="delete-log-btn text-red-500 hover:text-red-700 text-sm ml-4" 
                        data-log-id="${log.id}" title="Delete log">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
}

// Function to update today's summary
function updateTodaysSummary(todaysLogs, totalDuration) {
    const totalDurationEl = document.getElementById('totalDuration');
    const totalSessionsEl = document.getElementById('totalSessions');
    const lastActivityEl = document.getElementById('lastActivity');

    if (totalDurationEl) totalDurationEl.textContent = `${totalDuration || 0} min`;
    if (totalSessionsEl) totalSessionsEl.textContent = todaysLogs.length;

    if (lastActivityEl) {
        if (todaysLogs.length > 0) {
            const lastLog = todaysLogs[0]; // Assuming logs are ordered by date desc
            lastActivityEl.textContent = lastLog.trainingType;
        } else {
            lastActivityEl.textContent = 'No activity today';
        }
    }
}

// Function to format date and time
function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 1) {
        return 'Today at ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (diffDays === 2) {
        return 'Yesterday at ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else {
        return date.toLocaleDateString() + ' at ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
}

// Handle delete log button clicks
document.addEventListener('click', async function(e) {
    if (e.target.classList.contains('delete-log-btn') || e.target.closest('.delete-log-btn')) {
        e.preventDefault();
        const deleteBtn = e.target.classList.contains('delete-log-btn') ? e.target : e.target.closest('.delete-log-btn');
        const logId = deleteBtn.getAttribute('data-log-id');

        if (confirm('Are you sure you want to delete this training log?')) {
            try {
                const response = await fetch(`/api/dailylogs/${logId}`, {
                    method: 'DELETE'
                });

                const result = await response.json();

                if (result.success) {
                    showMessage('Training log deleted successfully!', 'success');
                    loadDailyLogs();
                    loadTodaysSummary();
                    loadAthleteStats();
                    loadCharts();
                } else {
                    showMessage(result.message || 'Failed to delete training log', 'error');
                }
            } catch (error) {
                console.error('Error deleting training log:', error);
                showMessage('An error occurred while deleting the training log', 'error');
            }
        }
        return;
    }
});

// Chart variables
let durationChart = null;
let trainingTypeChart = null;
let currentChartPeriod = 7;

// Load daily logs on page load if user is on daily logs tab
document.addEventListener('DOMContentLoaded', function() {
    const dailyLogsTab = document.getElementById('dailylogs');
    if (dailyLogsTab && dailyLogsTab.classList.contains('active')) {
        loadDailyLogs();
        loadTodaysSummary();
        loadAthleteStats();
        loadCharts();
        initializeChartPeriodButtons();
    }
});

// Initialize chart period buttons
function initializeChartPeriodButtons() {
    const chartPeriodButtons = document.querySelectorAll('.chart-period-btn');

    chartPeriodButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Remove active class from all buttons
            chartPeriodButtons.forEach(btn => {
                btn.classList.remove('active', 'bg-blue-500', 'text-white');
                btn.classList.add('bg-gray-200', 'text-gray-700');
            });

            // Add active class to clicked button
            this.classList.add('active', 'bg-blue-500', 'text-white');
            this.classList.remove('bg-gray-200', 'text-gray-700');

            // Update current period and reload charts
            currentChartPeriod = parseInt(this.getAttribute('data-period'));
            loadCharts();
        });
    });
}

// Function to load and display charts
async function loadCharts() {
    try {
        const response = await fetch(`/api/dailylogs/chart-data?days=${currentChartPeriod}`);
        const result = await response.json();

        if (result.success) {
            renderDurationChart(result.durationChart);
            renderTrainingTypeChart(result.trainingTypeChart);
        } else {
            console.error('Failed to load chart data:', result.message);
            showMessage('Failed to load chart data', 'error');
        }
    } catch (error) {
        console.error('Error loading chart data:', error);
        showMessage('Error loading chart data', 'error');
    }
}

// Function to render the daily duration chart
function renderDurationChart(chartData) {
    const ctx = document.getElementById('durationChart');
    if (!ctx) return;

    // Destroy existing chart if it exists
    if (durationChart) {
        durationChart.destroy();
    }

    durationChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: chartData.labels,
            datasets: [{
                label: 'Training Duration (minutes)',
                data: chartData.data,
                borderColor: 'rgb(59, 130, 246)',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.3,
                pointBackgroundColor: 'rgb(59, 130, 246)',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    borderColor: 'rgb(59, 130, 246)',
                    borderWidth: 1,
                    cornerRadius: 8,
                    displayColors: false,
                    callbacks: {
                        label: function(context) {
                            return `${context.parsed.y} minutes`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    },
                    ticks: {
                        callback: function(value) {
                            return value + 'm';
                        }
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
            }
        }
    });
}

// Function to render the training type distribution chart
function renderTrainingTypeChart(chartData) {
    const ctx = document.getElementById('trainingTypeChart');
    if (!ctx) return;

    // Destroy existing chart if it exists
    if (trainingTypeChart) {
        trainingTypeChart.destroy();
    }

    // Don't render if no data
    if (!chartData.labels || chartData.labels.length === 0) {
        ctx.getContext('2d').clearRect(0, 0, ctx.width, ctx.height);
        const context = ctx.getContext('2d');
        context.font = '16px Arial';
        context.fillStyle = '#6B7280';
        context.textAlign = 'center';
        context.fillText('No training data available', ctx.width / 2, ctx.height / 2);
        return;
    }

    // Generate colors for each training type
    const colors = [
        'rgba(59, 130, 246, 0.8)',   // Blue
        'rgba(16, 185, 129, 0.8)',   // Green
        'rgba(245, 158, 11, 0.8)',   // Yellow
        'rgba(239, 68, 68, 0.8)',    // Red
        'rgba(139, 92, 246, 0.8)',   // Purple
        'rgba(236, 72, 153, 0.8)',   // Pink
        'rgba(14, 165, 233, 0.8)',   // Light Blue
        'rgba(34, 197, 94, 0.8)',    // Light Green
    ];

    const borderColors = [
        'rgb(59, 130, 246)',
        'rgb(16, 185, 129)',
        'rgb(245, 158, 11)',
        'rgb(239, 68, 68)',
        'rgb(139, 92, 246)',
        'rgb(236, 72, 153)',
        'rgb(14, 165, 233)',
        'rgb(34, 197, 94)',
    ];

    trainingTypeChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: chartData.labels,
            datasets: [{
                data: chartData.data,
                backgroundColor: colors.slice(0, chartData.labels.length),
                borderColor: borderColors.slice(0, chartData.labels.length),
                borderWidth: 2,
                hoverBorderWidth: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        usePointStyle: true,
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    borderColor: 'rgba(255, 255, 255, 0.1)',
                    borderWidth: 1,
                    cornerRadius: 8,
                    callbacks: {
                        label: function(context) {
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((context.parsed / total) * 100).toFixed(1);
                            return `${context.label}: ${context.parsed} sessions (${percentage}%)`;
                        }
                    }
                }
            },
            cutout: '65%'
        }
    });
}

// Initialize the page based on server-provided active tab
initializePage();

// ===== PROFILE MANAGEMENT FUNCTIONALITY =====

// Profile management variables
let profileData = {};
let currentUserRole = '';

// Function to load profile data
async function loadProfileData() {
    try {
        const response = await fetch('/api/profile/data');
        const result = await response.json();

        if (result.success) {
            profileData = result;
            currentUserRole = result.role;
            displayPersonalInfo(result.personal);
            displayProfessionalInfo(result.professional, result.role);
        } else {
            console.error('Failed to load profile data:', result.message);
            showMessage('Failed to load profile data', 'error');
        }
    } catch (error) {
        console.error('Error loading profile data:', error);
        showMessage('Error loading profile data', 'error');
    }
}

// Function to initialize profile event handlers
function initializeProfileHandlers() {
    // Personal info edit handlers
    const editPersonalBtn = document.getElementById('editPersonalBtn');
    const cancelPersonalBtn = document.getElementById('cancelPersonalBtn');
    const personalForm = document.getElementById('personalForm');

    // Professional info edit handlers
    const editProfessionalBtn = document.getElementById('editProfessionalBtn');
    const professionalForm = document.getElementById('professionalForm');

    if (editPersonalBtn) {
        editPersonalBtn.addEventListener('click', () => {
            togglePersonalEditMode(true);
        });
    }

    if (cancelPersonalBtn) {
        cancelPersonalBtn.addEventListener('click', () => {
            togglePersonalEditMode(false);
            loadProfileData(); // Reload to reset any changes
        });
    }

    if (personalForm) {
        personalForm.addEventListener('submit', handlePersonalFormSubmit);
    }

    if (editProfessionalBtn) {
        editProfessionalBtn.addEventListener('click', () => {
            toggleProfessionalEditMode(true);
        });
    }

    if (professionalForm) {
        professionalForm.addEventListener('submit', handleProfessionalFormSubmit);
    }
}

// Function to display personal information
function displayPersonalInfo(personal) {
    // Update profile image and basic info
    const profileImage = document.getElementById('profileImage');
    const fullName = document.getElementById('fullName');
    const userEmail = document.getElementById('userEmail');
    const userRole = document.getElementById('userRole');

    if (profileImage) {
        profileImage.src = personal.profileImageUrl || '/placeholder-avatar.png';
    }
    if (fullName) {
        fullName.textContent = `${personal.firstName || ''} ${personal.lastName || ''}`.trim() || 'Name not provided';
    }
    if (userEmail) {
        userEmail.textContent = personal.email || 'Email not provided';
    }
    if (userRole) {
        userRole.textContent = currentUserRole ? currentUserRole.charAt(0).toUpperCase() + currentUserRole.slice(1) : 'Role';
    }

    // Update individual fields
    document.getElementById('phoneDisplay').textContent = personal.phone || 'Not provided';
    document.getElementById('countryDisplay').textContent = personal.country || 'Not provided';
    document.getElementById('genderDisplay').textContent = personal.gender || 'Not provided';
    document.getElementById('aadhaarDisplay').textContent = personal.aadhaarNumber || 'Not provided';
    document.getElementById('bioDisplay').textContent = personal.bio || 'No bio provided';

    // Populate edit form fields
    document.getElementById('firstName').value = personal.firstName || '';
    document.getElementById('lastName').value = personal.lastName || '';
    document.getElementById('phone').value = personal.phone || '';
    document.getElementById('country').value = personal.country || '';
    document.getElementById('gender').value = personal.gender || '';
    document.getElementById('aadhaarNumber').value = personal.aadhaarNumber || '';
    document.getElementById('bio').value = personal.bio || '';
}

// Function to display professional information based on role
function displayProfessionalInfo(professional, role) {
    const displayDiv = document.getElementById('professionalDisplay');
    const editDiv = document.getElementById('professionalEdit');
    const formDiv = document.getElementById('professionalForm');

    if (!displayDiv || !editDiv || !formDiv) return;

    let displayHTML = '';
    let editHTML = '';

    switch (role) {
        case 'athlete':
            displayHTML = generateAthleteDisplayHTML(professional);
            editHTML = generateAthleteEditHTML(professional);
            break;
        case 'coach':
            displayHTML = generateCoachDisplayHTML(professional);
            editHTML = generateCoachEditHTML(professional);
            break;
        case 'sponsor':
            displayHTML = generateSponsorDisplayHTML(professional);
            editHTML = generateSponsorEditHTML(professional);
            break;
        default:
            displayHTML = '<p class="text-gray-500">No professional information available</p>';
            editHTML = '<p class="text-gray-500">No professional information to edit</p>';
    }

    displayDiv.innerHTML = displayHTML;
    formDiv.innerHTML = editHTML;
}

// Generate athlete display HTML
function generateAthleteDisplayHTML(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Height</label>
                <p class="text-gray-900">${data.height ? data.height + ' cm' : 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Weight</label>
                <p class="text-gray-900">${data.weight ? data.weight + ' kg' : 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">State</label>
                <p class="text-gray-900">${data.state || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">District</label>
                <p class="text-gray-900">${data.district || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Disability Status</label>
                <p class="text-gray-900">${data.isDisabled ? 'Yes' : 'No'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Disability Type</label>
                <p class="text-gray-900">${data.disabilityType || 'Not applicable'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Emergency Contact</label>
                <p class="text-gray-900">${data.emergencyContactName || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Emergency Phone</label>
                <p class="text-gray-900">${data.emergencyContactPhone || 'Not provided'}</p>
            </div>
            ${data.currentCoach ? `
                <div class="md:col-span-2">
                    <label class="block text-sm font-medium text-gray-700 mb-1">Current Coach</label>
                    <p class="text-gray-900">${data.currentCoach.name}</p>
                </div>
            ` : ''}
        </div>
    `;
}

// Generate athlete edit HTML
function generateAthleteEditHTML(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Height (cm)</label>
                <input type="number" name="height" value="${data.height || ''}" step="0.1" 
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Weight (kg)</label>
                <input type="number" name="weight" value="${data.weight || ''}" step="0.1"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">State</label>
                <input type="text" name="state" value="${data.state || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">District</label>
                <input type="text" name="district" value="${data.district || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Disability Status</label>
                <select name="isDisabled" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="false" ${!data.isDisabled ? 'selected' : ''}>No</option>
                    <option value="true" ${data.isDisabled ? 'selected' : ''}>Yes</option>
                </select>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Disability Type</label>
                <input type="text" name="disabilityType" value="${data.disabilityType || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Emergency Contact Name</label>
                <input type="text" name="emergencyContactName" value="${data.emergencyContactName || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Emergency Contact Phone</label>
                <input type="tel" name="emergencyContactPhone" value="${data.emergencyContactPhone || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div class="md:col-span-2 flex space-x-3">
                <button type="submit" class="bg-green-500 hover:bg-green-600 text-white px-6 py-2 rounded-lg flex items-center">
                    <i class="fas fa-save mr-2"></i> Save Changes
                </button>
                <button type="button" id="cancelProfessionalBtn" class="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg flex items-center">
                    <i class="fas fa-times mr-2"></i> Cancel
                </button>
            </div>
        </div>
    `;
}

// Generate coach display HTML
function generateCoachDisplayHTML(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Authority/Certification</label>
                <p class="text-gray-900">${data.authority || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Specialization</label>
                <p class="text-gray-900">${data.specialization || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Experience (Years)</label>
                <p class="text-gray-900">${data.experienceYears || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">State</label>
                <p class="text-gray-900">${data.state || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">District</label>
                <p class="text-gray-900">${data.district || 'Not provided'}</p>
            </div>
        </div>
    `;
}

// Generate coach edit HTML
function generateCoachEditHTML(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Authority/Certification</label>
                <input type="text" name="authority" value="${data.authority || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Specialization</label>
                <input type="text" name="specialization" value="${data.specialization || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Experience (Years)</label>
                <input type="number" name="experienceYears" value="${data.experienceYears || ''}" min="0"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">State</label>
                <input type="text" name="state" value="${data.state || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">District</label>
                <input type="text" name="district" value="${data.district || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div class="md:col-span-2 flex space-x-3">
                <button type="submit" class="bg-green-500 hover:bg-green-600 text-white px-6 py-2 rounded-lg flex items-center">
                    <i class="fas fa-save mr-2"></i> Save Changes
                </button>
                <button type="button" id="cancelProfessionalBtn" class="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg flex items-center">
                    <i class="fas fa-times mr-2"></i> Cancel
                </button>
            </div>
        </div>
    `;
}

// Generate sponsor display HTML
function generateSponsorDisplayHTML(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Company Name</label>
                <p class="text-gray-900">${data.companyName || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Industry</label>
                <p class="text-gray-900">${data.industry || 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Website</label>
                <p class="text-gray-900">${data.website ? `<a href="${data.website}" target="_blank" class="text-blue-600 hover:underline">${data.website}</a>` : 'Not provided'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Budget Range</label>
                <p class="text-gray-900">${data.budgetRange || 'Not provided'}</p>
            </div>
        </div>
    `;
}

// Generate sponsor edit HTML
function generateSponsorEditHTML(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Company Name</label>
                <input type="text" name="companyName" value="${data.companyName || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Industry</label>
                <input type="text" name="industry" value="${data.industry || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Website</label>
                <input type="url" name="website" value="${data.website || ''}"
                       class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Budget Range</label>
                <select name="budgetRange" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">Select Budget Range</option>
                    <option value="Under $1,000" ${data.budgetRange === 'Under $1,000' ? 'selected' : ''}>Under $1,000</option>
                    <option value="$1,000 - $5,000" ${data.budgetRange === '$1,000 - $5,000' ? 'selected' : ''}>$1,000 - $5,000</option>
                    <option value="$5,000 - $10,000" ${data.budgetRange === '$5,000 - $10,000' ? 'selected' : ''}>$5,000 - $10,000</option>
                    <option value="$10,000 - $25,000" ${data.budgetRange === '$10,000 - $25,000' ? 'selected' : ''}>$10,000 - $25,000</option>
                    <option value="$25,000 - $50,000" ${data.budgetRange === '$25,000 - $50,000' ? 'selected' : ''}>$25,000 - $50,000</option>
                    <option value="Over $50,000" ${data.budgetRange === 'Over $50,000' ? 'selected' : ''}>Over $50,000</option>
                </select>
            </div>
            <div class="md:col-span-2 flex space-x-3">
                <button type="submit" class="bg-green-500 hover:bg-green-600 text-white px-6 py-2 rounded-lg flex items-center">
                    <i class="fas fa-save mr-2"></i> Save Changes
                </button>
                <button type="button" id="cancelProfessionalBtn" class="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg flex items-center">
                    <i class="fas fa-times mr-2"></i> Cancel
                </button>
            </div>
        </div>
    `;
}

// Toggle personal info edit mode
function togglePersonalEditMode(editMode) {
    const displayDiv = document.getElementById('personalDisplay');
    const editDiv = document.getElementById('personalEdit');
    const editBtn = document.getElementById('editPersonalBtn');

    if (editMode) {
        displayDiv.classList.add('hidden');
        editDiv.classList.remove('hidden');
        editBtn.style.display = 'none';
    } else {
        displayDiv.classList.remove('hidden');
        editDiv.classList.add('hidden');
        editBtn.style.display = 'flex';
    }
}

// Toggle professional info edit mode
function toggleProfessionalEditMode(editMode) {
    const displayDiv = document.getElementById('professionalDisplay');
    const editDiv = document.getElementById('professionalEdit');
    const editBtn = document.getElementById('editProfessionalBtn');

    if (editMode) {
        displayDiv.classList.add('hidden');
        editDiv.classList.remove('hidden');
        editBtn.style.display = 'none';

        // Add cancel button handler after form is rendered
        setTimeout(() => {
            const cancelBtn = document.getElementById('cancelProfessionalBtn');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', () => {
                    toggleProfessionalEditMode(false);
                    loadProfileData(); // Reload to reset any changes
                });
            }
        }, 100);
    } else {
        displayDiv.classList.remove('hidden');
        editDiv.classList.add('hidden');
        editBtn.style.display = 'flex';
    }
}

// Handle personal form submission
async function handlePersonalFormSubmit(e) {
    e.preventDefault();

    const formData = new FormData(e.target);

    try {
        const response = await fetch('/api/profile/personal', {
            method: 'PUT',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            showMessage('Personal information updated successfully!', 'success');
            togglePersonalEditMode(false);
            loadProfileData(); // Reload to show updated data
        } else {
            showMessage(result.message || 'Failed to update personal information', 'error');
        }
    } catch (error) {
        console.error('Error updating personal information:', error);
        showMessage('Error updating personal information', 'error');
    }
}

// Handle professional form submission
async function handleProfessionalFormSubmit(e) {
    e.preventDefault();

    const formData = new FormData(e.target);
    const professionalData = {};

    // Convert FormData to regular object
    for (let [key, value] of formData.entries()) {
        professionalData[key] = value;
    }

    try {
        const response = await fetch('/api/profile/professional', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(professionalData)
        });

        const result = await response.json();

        if (result.success) {
            showMessage('Professional information updated successfully!', 'success');
            toggleProfessionalEditMode(false);
            loadProfileData(); // Reload to show updated data
        } else {
            showMessage(result.message || 'Failed to update professional information', 'error');
        }
    } catch (error) {
        console.error('Error updating professional information:', error);
        showMessage('Error updating professional information', 'error');
    }
}

