    posts.forEach(post => {
        if (filterType === 'all') {
            post.style.display = 'block';
            visibleCount++;
        } else {
            // Check post type (you may need to add data attributes to posts)
            const postTypeElement = post.querySelector('[data-post-type]');
            const postType = postTypeElement?.getAttribute('data-post-type') || '';
            
            if (postType === filterType) {
                post.style.display = 'block';
                visibleCount++;
            } else {
                post.style.display = 'none';
            }
        }
    });

    showFilterResults(visibleCount, filterType);
}

// Show search results
function showSearchResults(count, term) {
    // Remove existing message
    const existingMessage = document.getElementById('searchResultsMessage');
    if (existingMessage) {
        existingMessage.remove();
        if (activeTab === 'explore') {

    // Add new message
    const postsContainer = document.getElementById('postsContainer');
    if (postsContainer) {
        const message = document.createElement('div');
        message.id = 'searchResultsMessage';
        message.className = 'col-span-full text-center py-4 text-gray-600';
        message.textContent = count > 0 ? 
            `Found ${count} result(s) for "${term}"` : 
            `No results found for "${term}"`;
        postsContainer.insertBefore(message, postsContainer.firstChild);
    }
            searchFilterNav.style.display = 'block';
        } else {
// Show filter results
function showFilterResults(count, filterType) {
    // Remove existing message
    const existingMessage = document.getElementById('filterResultsMessage');
    if (existingMessage) {
        existingMessage.remove();
    }
        initializeProfileHandlers();
    // Add new message if filtering
    if (filterType !== 'all') {
        const postsContainer = document.getElementById('postsContainer');
        if (postsContainer) {
            const message = document.createElement('div');
            message.id = 'filterResultsMessage';
            message.className = 'col-span-full text-center py-4 text-gray-600';
            message.textContent = count > 0 ? 
                `Showing ${count} ${filterType.toLowerCase()} post(s)` : 
                `No ${filterType.toLowerCase()} posts found`;
            postsContainer.insertBefore(message, postsContainer.firstChild);
        }
        loadInvitations();
}
        initializeInvitationTabs();
// Initialize invitation tabs functionality
function initializeInvitationTabs() {
    const receivedTab = document.getElementById('receivedInvitationsTab');
    const sentTab = document.getElementById('sentInvitationsTab');
    const receivedContent = document.getElementById('receivedInvitations');
    const sentContent = document.getElementById('sentInvitations');
    initializePostActions();
    if (receivedTab && sentTab && receivedContent && sentContent) {
        // Received invitations tab
        receivedTab.addEventListener('click', () => {
            // Update tab styling
            receivedTab.classList.add('active', 'bg-blue-500', 'text-white');
            receivedTab.classList.remove('hover:bg-gray-100');
            sentTab.classList.remove('active', 'bg-blue-500', 'text-white');
            sentTab.classList.add('hover:bg-gray-100');

            // Show/hide content
            receivedContent.classList.remove('hidden');
            sentContent.classList.add('hidden');

            // Load received invitations
            loadReceivedInvitations();
        });
    const filterTags = document.querySelectorAll('.filter-tag');
        // Sent invitations tab
        sentTab.addEventListener('click', () => {
            // Update tab styling
            sentTab.classList.add('active', 'bg-blue-500', 'text-white');
            sentTab.classList.remove('hover:bg-gray-100');
            receivedTab.classList.remove('active', 'bg-blue-500', 'text-white');
            receivedTab.classList.add('hover:bg-gray-100');

            // Show/hide content
            sentContent.classList.remove('hidden');
            receivedContent.classList.add('hidden');

            // Load sent invitations
            loadSentInvitations();
        });
            }
        // Load initial data (received invitations)
        loadReceivedInvitations();
    }
}
        if (clearSearchBtn) {
// Load invitations data
async function loadInvitations() {
    // This will be called by the tab initialization
    console.log('Loading invitations...');
}
        }
// Load received invitations
async function loadReceivedInvitations() {
    try {
        const response = await fetch('/api/invitations/received');
        const result = await response.json();
                
        if (result.success) {
            displayReceivedInvitations(result.invitations);
        } else {
            console.error('Failed to load received invitations:', result.message);
        }
    } catch (error) {
        console.error('Error loading received invitations:', error);
    }
    if (searchTerm) {
        // Show clear button
// Load sent invitations
async function loadSentInvitations() {
        }
        const response = await fetch('/api/invitations/sent');
        // Filter posts based on search term
        const posts = document.querySelectorAll('[data-post-id]');
        let visibleCount = 0;
            displaySentInvitations(result.invitations);
        posts.forEach(post => {
            console.error('Failed to load sent invitations:', result.message);
            const description = post.querySelector('p')?.textContent.toLowerCase() || '';
            const userName = post.querySelector('h3')?.textContent.toLowerCase() || '';
        console.error('Error loading sent invitations:', error);
            if (title.includes(searchTerm.toLowerCase()) || 
                description.includes(searchTerm.toLowerCase()) || 
                userName.includes(searchTerm.toLowerCase())) {
// Display received invitations
function displayReceivedInvitations(invitations) {
    const container = document.getElementById('receivedInvitationsList');
    if (!container) return;

    if (invitations && invitations.length > 0) {
        container.innerHTML = invitations.map(invitation => `
            <div class="border border-gray-200 rounded-lg p-4 mb-4 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start mb-3">
function clearSearch() {
                        <img src="${invitation.sender?.profileImageUrl || '/placeholder-avatar.png'}" 
                             alt="Sender" class="w-12 h-12 rounded-full object-cover mr-3">
    searchInput.value = '';
                            <h4 class="font-semibold text-gray-900">${invitation.sender?.firstName} ${invitation.sender?.lastName}</h4>
                            <p class="text-sm text-gray-600">${invitation.sender?.role}</p>
                            <button class="visit-profile-btn text-blue-600 hover:text-blue-800 text-sm"
                                    data-user-id="${invitation.sender?.id}" data-user-role="${invitation.sender?.role}">
                                <i class="fas fa-user mr-1"></i>View Profile
                            </button>
                        </div>
                    </div>
                    <span class="px-3 py-1 rounded-full text-sm ${getInvitationStatusColor(invitation.status)}">
                        ${invitation.status}
                    </span>
                </div>
                
                <div class="mb-3">
                    <h5 class="font-medium text-gray-800">Post: ${invitation.post?.title}</h5>
                    <p class="text-sm text-gray-600">${invitation.post?.description}</p>
                </div>
                
                ${invitation.message ? `
                    <div class="mb-3">
                        <p class="text-sm text-gray-700 italic">"${invitation.message}"</p>
                    </div>
                ` : ''}
                
                <div class="flex justify-between items-center text-xs text-gray-500 mb-3">
                    <span>Received: ${formatDate(invitation.createdAt)}</span>
                    <span>Post Type: ${invitation.post?.postType}</span>
                </div>
                
                ${invitation.status === 'PENDING' ? `
                    <div class="flex space-x-2">
                        <button class="accept-invitation-btn bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded text-sm"
                                data-invitation-id="${invitation.id}">
                            <i class="fas fa-check mr-1"></i>Accept
        });
                        <button class="decline-invitation-btn bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded text-sm"
                                data-invitation-id="${invitation.id}">
                            <i class="fas fa-times mr-1"></i>Decline
                        </button>
                    </div>
                ` : ''}
        if (cancelPersonalBtn) {
            cancelPersonalBtn.addEventListener('click', () => {
                personalDisplay.style.display = 'block';
        // Add event listeners for invitation actions
        initializeInvitationActions();

        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="fas fa-inbox text-4xl mb-4"></i>
                <p>No invitations received yet</p>
            </div>
        `;

// Initialize achievements functionality
function initializeAchievements() {
// Display sent invitations
function displaySentInvitations(invitations) {
    const container = document.getElementById('sentInvitationsList');
    if (!container) return;
    // Open achievement modal
    if (invitations && invitations.length > 0) {
        container.innerHTML = invitations.map(invitation => `
            <div class="border border-gray-200 rounded-lg p-4 mb-4 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start mb-3">
                    <div class="flex items-center">
                        <img src="${invitation.receiver?.profileImageUrl || '/placeholder-avatar.png'}" 
                             alt="Receiver" class="w-12 h-12 rounded-full object-cover mr-3">
    const noAchievements = document.getElementById('noAchievements');
                            <h4 class="font-semibold text-gray-900">${invitation.receiver?.firstName} ${invitation.receiver?.lastName}</h4>
                            <p class="text-sm text-gray-600">${invitation.receiver?.role}</p>
                            <button class="visit-profile-btn text-blue-600 hover:text-blue-800 text-sm"
                                    data-user-id="${invitation.receiver?.id}" data-user-role="${invitation.receiver?.role}">
                                <i class="fas fa-user mr-1"></i>View Profile
                            </button>

    if (achievements && achievements.length > 0) {
                    <span class="px-3 py-1 rounded-full text-sm ${getInvitationStatusColor(invitation.status)}">
                        ${invitation.status}
                    </span>
                </div>
                
                <div class="mb-3">
                    <h5 class="font-medium text-gray-800">Post: ${invitation.post?.title}</h5>
                    <p class="text-sm text-gray-600">${invitation.post?.description}</p>
                </div>
                
                ${invitation.message ? `
                    <div class="mb-3">
                        <p class="text-sm text-gray-700 italic">"${invitation.message}"</p>
                    </div>
                ` : ''}
                
                <div class="flex justify-between items-center text-xs text-gray-500">
                    <span>Sent: ${formatDate(invitation.createdAt)}</span>
                    <span>Post Type: ${invitation.post?.postType}</span>
                </div>
            </div>
        `).join('');

        // Add event listeners for visit profile
        initializeVisitProfileButtons();
    } else {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="fas fa-paper-plane text-4xl mb-4"></i>
                <p>No invitations sent yet</p>
            </div>
        `;
    }
                        ${achievement.level ? `<span class="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">${achievement.level}</span>` : ''}
                        ${achievement.category ? `<span class="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full">${achievement.category}</span>` : ''}
// Initialize invitation action buttons
function initializeInvitationActions() {
    // Accept invitation buttons
    document.querySelectorAll('.accept-invitation-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const invitationId = btn.getAttribute('data-invitation-id');
            await respondToInvitation(invitationId, 'ACCEPTED');
        });
    });
                
    // Decline invitation buttons
    document.querySelectorAll('.decline-invitation-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const invitationId = btn.getAttribute('data-invitation-id');
            await respondToInvitation(invitationId, 'DECLINED');
                        ` : ''}
                        ${achievement.position ? `
                            <span class="flex items-center">
    // Initialize visit profile buttons
    initializeVisitProfileButtons();
}
                    ` : ''}
// Respond to invitation
async function respondToInvitation(invitationId, status) {
    try {
        const response = await fetch(`/api/invitations/${invitationId}/respond`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `status=${status}`
        });
    }
        const result = await response.json();

        if (result.success) {
            showMessage(`Invitation ${status.toLowerCase()} successfully!`, 'success');
            // Reload received invitations
            loadReceivedInvitations();
        } else {
            showMessage(result.message || `Failed to ${status.toLowerCase()} invitation`, 'error');
        }
    } catch (error) {
        console.error('Error responding to invitation:', error);
        showMessage('An error occurred while responding to the invitation', 'error');
    }
}

// Get invitation status color
function getInvitationStatusColor(status) {
    switch (status?.toUpperCase()) {
        case 'PENDING':
            return 'bg-yellow-100 text-yellow-800';
        case 'ACCEPTED':
            return 'bg-green-100 text-green-800';
        case 'DECLINED':
            return 'bg-red-100 text-red-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
}

// Initialize visit profile buttons
function initializeVisitProfileButtons() {
    document.querySelectorAll('.visit-profile-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const userId = btn.getAttribute('data-user-id');
            const userRole = btn.getAttribute('data-user-role');

            if (userId && userRole) {
                // Navigate to user profile
                window.location.href = `/${userRole.toLowerCase()}/profile/${userId}`;
            }
        });
            showMessage('Achievement deleted successfully', 'success');
            loadUserAchievements(); // Reload achievements list
        } else {
            showMessage(result.message || 'Failed to delete achievement', 'error');
        }
    } catch (error) {
        console.error('Error deleting achievement:', error);
        showMessage('An error occurred while deleting the achievement', 'error');
    }
}

// Initialize post actions (like buttons, delete buttons, etc.)
function initializePostActions() {
    // Initialize like buttons
    document.querySelectorAll('.like-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const postId = btn.getAttribute('data-post-id');
            await toggleLike(postId);
        });
    });

    // Initialize send invitation buttons
    document.querySelectorAll('.send-invitation-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const postId = btn.getAttribute('data-post-id');
            openInvitationModal(postId);
        });
    });
}

// Toggle like function
async function toggleLike(postId) {
    try {
        const response = await fetch(`/api/posts/${postId}/like`, {
            method: 'POST'
        });

        const result = await response.json();

        if (result.success) {
            // Update the like button and count
            const likeBtn = document.querySelector(`[data-post-id="${postId}"]`);
            if (likeBtn) {
                const likeIcon = likeBtn.querySelector('.like-icon');
                const likeText = likeBtn.querySelector('.like-text');
                const likeCount = likeBtn.querySelector('.like-count');

                if (result.liked) {
                    likeBtn.classList.remove('bg-green-500', 'hover:bg-green-600');
                    likeBtn.classList.add('bg-blue-500', 'hover:bg-blue-600');
                    likeIcon.classList.add('text-red-300');
                    likeText.textContent = 'Liked';
                } else {
                    likeBtn.classList.remove('bg-blue-500', 'hover:bg-blue-600');
                    likeBtn.classList.add('bg-green-500', 'hover:bg-green-600');
                    likeIcon.classList.remove('text-red-300');
                    likeText.textContent = 'Like';
                }

                likeCount.textContent = `(${result.likesCount})`;
            }
        } else {
            showMessage(result.message || 'Failed to update like', 'error');
        }
    } catch (error) {
        console.error('Error toggling like:', error);
        showMessage('An error occurred while updating like', 'error');
    }
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

// Initialize the page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    initializeNewPostModal();
});

// Initialize New Post Modal
function initializeNewPostModal() {
    const newPostBtn = document.getElementById('newPostBtn');
    const fabNewPost = document.getElementById('fabNewPost');
    const newPostModal = document.getElementById('newPostModal');
    const closeModal = document.getElementById('closeModal');
    const cancelPost = document.getElementById('cancelPost');
    const newPostForm = document.getElementById('newPostForm');

    // Open modal handlers
    [newPostBtn, fabNewPost].forEach(btn => {
        if (btn) {
            btn.addEventListener('click', () => {
                if (newPostModal) {
                    newPostModal.classList.add('active');
                    newPostModal.style.display = 'flex';
                }
            });
        }
    });

    // Close modal handlers
    [closeModal, cancelPost].forEach(btn => {
        if (btn) {
            btn.addEventListener('click', () => {
                if (newPostModal) {
                    newPostModal.classList.remove('active');
                    newPostModal.style.display = 'none';
                    if (newPostForm) newPostForm.reset();
                }
            });
        }
    });

    // Click outside modal to close
    if (newPostModal) {
        newPostModal.addEventListener('click', (e) => {
            if (e.target === newPostModal) {
                newPostModal.classList.remove('active');
                newPostModal.style.display = 'none';
                if (newPostForm) newPostForm.reset();
            }
        });
    }

    // Handle form submission
    if (newPostForm) {
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
                    newPostModal.style.display = 'none';
                    newPostForm.reset();

                    // Show success message
                    showMessage('Post created successfully!', 'success');

                    // Reload the page to show the new post
                    window.location.reload();
                } else {
                    showMessage(result.message || 'Failed to create post', 'error');
                }
            } catch (error) {
                console.error('Error creating post:', error);
                showMessage('An error occurred while creating the post', 'error');
            }
        });
    }
}
