function formatDate(date) {
    return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function viewSponsorProfile(userId) {
    window.location.href = `/sponsor/profile/${userId}`;
}

function viewSponsorshipDetails(sponsorshipId) {
    // This could open a detailed modal or navigate to a detailed page
    fetch(`/api/sponsorships/${sponsorshipId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // For now, just show an alert with details
                // In a full implementation, this would open a detailed modal
                alert(`Sponsorship Details:\nAmount: ${getCurrencySymbol(data.sponsorship.currency)}${data.sponsorship.amount}\nTerms: ${data.sponsorship.terms}`);
            }
        })
        .catch(error => {
            console.error('Error fetching sponsorship details:', error);
        });
}

function showNotification(message, type) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 z-50 px-6 py-3 rounded-lg shadow-lg transition-all duration-300 transform translate-x-full ${
        type === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'
    }`;
    notification.textContent = message;

    document.body.appendChild(notification);

    // Animate in
    setTimeout(() => {
        notification.classList.remove('translate-x-full');
    }, 100);

    // Remove after 3 seconds
    setTimeout(() => {
        notification.classList.add('translate-x-full');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

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
    } else if (activeTab === 'invitations') {
        loadInvitations();
        initializeInvitationHandlers();
    } else if (activeTab === 'sponsorships') {
        loadSponsorships();
        initializeSponsorshipHandlers();
    }

    // Initialize invitation modal handlers
    initializeInvitationModal();

    // Initialize floating action button
    initializeFloatingActionButton();
}

// Function to open invitation modal with post details
function openInvitationModal(postId) {
    const invitationModal = document.getElementById('invitationModal');
    const postPreview = document.getElementById('postPreview');
    const invitationPostId = document.getElementById('invitationPostId');

    if (!invitationModal || !postPreview || !invitationPostId) {
        console.error('Invitation modal elements not found');
        return;
    }

    // Set the post ID
    invitationPostId.value = postId;

    // Fetch post details and populate preview
    fetch(`/api/posts/${postId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.post) {
                const post = data.post;
                postPreview.innerHTML = `
                    <div class="flex items-center mb-2">
                        <img src="${post.user?.profileImageUrl || '/placeholder-avatar.png'}" 
                             alt="User" class="w-8 h-8 rounded-full mr-2 object-cover">
                        <div>
                            <p class="font-semibold text-sm">${post.user?.firstName || ''} ${post.user?.lastName || ''}</p>
                            <p class="text-xs text-gray-500">${post.user?.role || ''}</p>
                        </div>
                    </div>
                    <h4 class="font-semibold text-sm mb-1">${post.title}</h4>
                    <p class="text-xs text-gray-600">${post.description}</p>
                    <span class="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded mt-2">${post.postType}</span>
                `;
            } else {
                postPreview.innerHTML = '<p class="text-red-500 text-sm">Unable to load post details</p>';
            }
        })
        .catch(error => {
            console.error('Error fetching post details:', error);
            postPreview.innerHTML = '<p class="text-red-500 text-sm">Error loading post details</p>';
        });

    // Show the modal
    invitationModal.classList.add('active');
}

// Function to initialize invitation modal handlers
function initializeInvitationModal() {
    const invitationModal = document.getElementById('invitationModal');
    const closeInvitationModal = document.getElementById('closeInvitationModal');
    const cancelInvitation = document.getElementById('cancelInvitation');
    const invitationForm = document.getElementById('invitationForm');

    if (!invitationModal || !closeInvitationModal || !cancelInvitation || !invitationForm) {
        console.error('Invitation modal elements not found');
        return;
    }

    // Close modal events
    [closeInvitationModal, cancelInvitation].forEach(btn => {
        btn.addEventListener('click', () => {
            invitationModal.classList.remove('active');
            invitationForm.reset();
        });
    });

    // Click outside modal to close
    invitationModal.addEventListener('click', (e) => {
        if (e.target === invitationModal) {
            invitationModal.classList.remove('active');
            invitationForm.reset();
        }
    });

    // Handle form submission
    invitationForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(invitationForm);
        const postId = formData.get('postId');
        const message = formData.get('message');

        try {
            const response = await fetch('/api/invitations/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    postId: postId,
                    message: message
                })
            });

            const result = await response.json();

            if (result.success) {
                // Close modal and reset form
                invitationModal.classList.remove('active');
                invitationForm.reset();

                // Show success message
                showMessage('Invitation sent successfully!', 'success');
            } else {
                showMessage(result.message || 'Failed to send invitation', 'error');
            }
        } catch (error) {
            console.error('Error sending invitation:', error);
            showMessage('An error occurred while sending the invitation', 'error');
        }
    });
}

// Floating Action Button functionality
function initializeFloatingActionButton() {
    const fabNewPost = document.getElementById('fabNewPost');
    const newPostBtn = document.getElementById('newPostBtn');

    if (fabNewPost && newPostBtn) {
        fabNewPost.addEventListener('click', () => {
            // Trigger the existing new post modal
            newPostBtn.click();
        });
    }
}

// Sponsorships functionality
function loadSponsorships() {
    const sponsorshipsContainer = document.getElementById('sponsorshipsContainer');
    const sponsorshipsLoading = document.getElementById('sponsorshipsLoading');
    const sponsorshipsEmpty = document.getElementById('sponsorshipsEmpty');
    const sponsorshipsList = document.getElementById('sponsorshipsList');

    if (!sponsorshipsContainer) return;

    // Show loading state
    sponsorshipsLoading.style.display = 'block';
    sponsorshipsEmpty.style.display = 'none';
    sponsorshipsList.style.display = 'none';

    fetch('/api/sponsorships/athlete')
        .then(response => response.json())
        .then(data => {
            sponsorshipsLoading.style.display = 'none';

            if (data.success && data.sponsorships && data.sponsorships.length > 0) {
                displaySponsorships(data.sponsorships);
                updateSponsorshipStats(data);
                sponsorshipsList.style.display = 'block';
            } else {
                sponsorshipsEmpty.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error loading sponsorships:', error);
            sponsorshipsLoading.style.display = 'none';
            sponsorshipsEmpty.style.display = 'block';
        });
}

function displaySponsorships(sponsorships) {
    const sponsorshipsList = document.getElementById('sponsorshipsList');
    if (!sponsorshipsList) return;

    sponsorshipsList.innerHTML = '';

    sponsorships.forEach(sponsorship => {
        const sponsorshipCard = createSponsorshipCard(sponsorship);
        sponsorshipsList.appendChild(sponsorshipCard);
    });
}

function createSponsorshipCard(sponsorship) {
    const card = document.createElement('div');
    card.className = 'bg-white border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow';
    card.setAttribute('data-status', sponsorship.status.toLowerCase());

    const statusColor = getStatusColor(sponsorship.status);
    const currencySymbol = getCurrencySymbol(sponsorship.currency);

    card.innerHTML = `
        <div class="flex justify-between items-start mb-4">
            <div class="flex items-center">
                <img src="${sponsorship.sponsor.profileImageUrl || '/placeholder-avatar.png'}" 
                     alt="Sponsor" class="w-12 h-12 rounded-full object-cover mr-4">
                <div>
                    <h3 class="text-lg font-semibold text-gray-900">${sponsorship.sponsor.name}</h3>
                    <p class="text-sm text-gray-600">${sponsorship.sponsor.companyName || 'Individual Sponsor'}</p>
                    <p class="text-xs text-gray-500">${sponsorship.sponsor.industry || 'Sports'}</p>
                </div>
            </div>
            <span class="px-3 py-1 rounded-full text-sm font-medium ${statusColor}">
                ${sponsorship.status}
            </span>
        </div>
        
        <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
                <p class="text-sm text-gray-600">Amount</p>
                <p class="text-xl font-bold text-green-600">${currencySymbol}${sponsorship.amount.toLocaleString()}</p>
            </div>
            <div>
                <p class="text-sm text-gray-600">Duration</p>
                <p class="text-sm font-medium">${formatDate(sponsorship.contractStartDate)} - ${formatDate(sponsorship.contractEndDate)}</p>
            </div>
        </div>
        
        <div class="mb-4">
            <p class="text-sm text-gray-600 mb-2">Terms</p>
            <p class="text-sm text-gray-800">${sponsorship.terms || 'No specific terms provided'}</p>
        </div>
        
        <div class="flex justify-between items-center">
            <p class="text-xs text-gray-500">Created: ${formatDate(sponsorship.createdAt)}</p>
            <div class="flex space-x-2">
                <button onclick="viewSponsorProfile(${sponsorship.sponsor.userId})" 
                        class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm">
                    View Profile
                </button>
                <button onclick="viewSponsorshipDetails(${sponsorship.id})" 
                        class="bg-gray-500 hover:bg-gray-600 text-white px-3 py-1 rounded text-sm">
                    Details
                </button>
            </div>
        </div>
    `;

    return card;
}

function updateSponsorshipStats(data) {
    const totalCount = document.getElementById('totalSponsorshipsCount');
    const activeCount = document.getElementById('activeSponsorshipsCount');
    const totalValue = document.getElementById('totalSponsorshipValue');
    const currentMonthValue = document.getElementById('currentMonthValue');

    if (totalCount) totalCount.textContent = data.totalCount || 0;
    if (activeCount) activeCount.textContent = data.activeCount || 0;

    // Calculate total value
    let total = 0;
    let currentMonth = 0;
    const currentDate = new Date();

    if (data.sponsorships) {
        data.sponsorships.forEach(sponsorship => {
            total += sponsorship.amount;

            // Check if sponsorship is active in current month
            const startDate = new Date(sponsorship.contractStartDate);
            const endDate = new Date(sponsorship.contractEndDate);
            if (startDate <= currentDate && endDate >= currentDate) {
                currentMonth += sponsorship.amount;
            }
        });
    }

    if (totalValue) totalValue.textContent = '$' + total.toLocaleString();
    if (currentMonthValue) currentMonthValue.textContent = '$' + currentMonth.toLocaleString();
}

function initializeSponsorshipHandlers() {
    // Filter buttons
    const filterButtons = document.querySelectorAll('.sponsorship-filter-btn');
    filterButtons.forEach(button => {
        button.addEventListener('click', () => {
            filterButtons.forEach(btn => {
                btn.classList.remove('active', 'bg-blue-500', 'text-white');
                btn.classList.add('bg-gray-200', 'text-gray-700');
            });

            button.classList.remove('bg-gray-200', 'text-gray-700');
            button.classList.add('active', 'bg-blue-500', 'text-white');

            const filter = button.getAttribute('data-filter');
            filterSponsorships(filter);
        });
    });
}

function filterSponsorships(filter) {
    const sponsorshipCards = document.querySelectorAll('[data-status]');

    sponsorshipCards.forEach(card => {
        const status = card.getAttribute('data-status');

        if (filter === 'all') {
            card.style.display = 'block';
        } else if (filter === 'active' && status === 'accepted') {
            card.style.display = 'block';
        } else if (filter === 'pending' && status === 'pending') {
            card.style.display = 'block';
        } else if (filter === 'completed' && (status === 'completed' || status === 'expired')) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// Utility functions
function getStatusColor(status) {
    switch (status.toLowerCase()) {
        case 'accepted':
        case 'active':
            return 'bg-green-100 text-green-800';
        case 'pending':
            return 'bg-yellow-100 text-yellow-800';
        case 'declined':
        case 'rejected':
            return 'bg-red-100 text-red-800';
        case 'completed':
            return 'bg-blue-100 text-blue-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
}

function getCurrencySymbol(currency) {
    switch (currency) {
        case 'USD': return '$';
        case 'EUR': return '€';
        case 'GBP': return '£';
        case 'INR': return '₹';
        default: return '$';
    }
}

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

    // Handle send invitation button
    if (e.target.classList.contains('send-invitation-btn') || e.target.closest('.send-invitation-btn')) {
        e.preventDefault();
        const inviteBtn = e.target.classList.contains('send-invitation-btn') ? e.target : e.target.closest('.send-invitation-btn');
        const postId = inviteBtn.getAttribute('data-post-id');

        // Open invitation modal with post details
        openInvitationModal(postId);
        return;
    }

    // Handle visit profile button
    if (e.target.classList.contains('visit-profile-btn') || e.target.closest('.visit-profile-btn')) {
        e.preventDefault();
        const profileBtn = e.target.classList.contains('visit-profile-btn') ? e.target : e.target.closest('.visit-profile-btn');
        const userId = profileBtn.getAttribute('data-user-id');
        const userRole = profileBtn.getAttribute('data-user-role');

        // Redirect to user's profile page
        window.location.href = `/${userRole}/profile/${userId}`;
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

    // Handle report button
    if (e.target.textContent.includes('Report')) {
        showMessage('Post reported. Thank you for helping keep our community safe.', 'success');
    }
});

// Placeholder functions for missing functionality
function loadDailyLogs() {
    console.log('loadDailyLogs function - placeholder');
}

function loadTodaysSummary() {
    console.log('loadTodaysSummary function - placeholder');
}

function loadAthleteStats() {
    console.log('loadAthleteStats function - placeholder');
}

function loadCharts() {
    console.log('loadCharts function - placeholder');
}

function initializeChartPeriodButtons() {
    console.log('initializeChartPeriodButtons function - placeholder');
}

function loadProfileData() {
    console.log('loadProfileData function - placeholder');
}

function initializeProfileHandlers() {
    console.log('initializeProfileHandlers function - placeholder');
}

function loadInvitations() {
    console.log('loadInvitations function - placeholder');
}

function initializeInvitationHandlers() {
    console.log('initializeInvitationHandlers function - placeholder');
}

// Initialize the page
initializePage();
