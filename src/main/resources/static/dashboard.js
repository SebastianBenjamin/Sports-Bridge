// SportsBridge Dashboard JavaScript

// Utility function to format dates
function formatDate(date) {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// Function to show messages to the user
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

// Tab functionality - Updated for page-based routing
function initializePage() {
    // Get the active tab from URL
    const path = window.location.pathname;
    let activeTab = 'explore';

    if (path.includes('/dailylogs')) activeTab = 'dailylogs';
    else if (path.includes('/invitations')) activeTab = 'invitations';
    else if (path.includes('/profile')) activeTab = 'profile';
    else if (path.includes('/mycoach')) activeTab = 'mycoach';
    else if (path.includes('/myathletes')) activeTab = 'myathletes';
    else if (path.includes('/sponsorships')) activeTab = 'sponsorships';

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
    if (activeTab === 'profile') {
        loadProfileData();
        initializeProfileHandlers();
        initializeAchievements();
    } else if (activeTab === 'invitations') {
        loadInvitations();
        initializeInvitationTabs();
    } else if (activeTab === 'explore') {
        initializeFilterAndSearch();
    }

    // Initialize common functionality
    initializePostActions();
    initializeInvitationModal();
    initializeFloatingActionButton();
}

// Initialize filter and search functionality
function initializeFilterAndSearch() {
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    const filterTags = document.querySelectorAll('.filter-tag');

    if (searchInput && searchBtn) {
        // Search functionality
        searchBtn.addEventListener('click', performSearch);
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                performSearch();
            }
        });

        // Clear search functionality
        if (clearSearchBtn) {
            clearSearchBtn.addEventListener('click', clearSearch);
        }

        // Filter functionality
        filterTags.forEach(tag => {
            tag.addEventListener('click', (e) => {
                e.preventDefault();
                const filterType = tag.getAttribute('data-type');
                applyFilter(filterType);

                // Update active filter styling
                filterTags.forEach(t => {
                    t.classList.remove('bg-blue-500', 'text-white');
                    t.classList.add('bg-gray-200', 'text-gray-800');
                });
                tag.classList.remove('bg-gray-200', 'text-gray-800');
                tag.classList.add('bg-blue-500', 'text-white');
            });
        });
    }
}

// Perform search - Enhanced implementation to show posts and profiles
async function performSearch() {
    const searchInput = document.getElementById('searchInput');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    const searchTerm = searchInput.value.trim();

    if (searchTerm) {
        // Show clear button
        if (clearSearchBtn) {
            clearSearchBtn.style.display = 'flex';
        }

        // Show loading state
        showSearchLoading();

        try {
            // Call the global search API
            const response = await fetch(`/api/search/global?query=${encodeURIComponent(searchTerm)}`);
            const result = await response.json();

            if (result.success) {
                displayGlobalSearchResults(result.results, searchTerm);
            } else {
                showMessage(result.message || 'Search failed', 'error');
                // Fallback to local search
                performLocalSearch(searchTerm);
            }
        } catch (error) {
            console.error('Error performing global search:', error);
            showMessage('Error performing search, showing local results', 'error');
            // Fallback to local search
            performLocalSearch(searchTerm);
        }
    }
}

// Fallback local search (original functionality)
function performLocalSearch(searchTerm) {
    // Filter posts based on search term (original functionality)
    const posts = document.querySelectorAll('[data-post-id]');
    let visibleCount = 0;

    posts.forEach(post => {
        const title = post.querySelector('h4')?.textContent.toLowerCase() || '';
        const description = post.querySelector('p')?.textContent.toLowerCase() || '';
        const userName = post.querySelector('h3')?.textContent.toLowerCase() || '';

        if (title.includes(searchTerm.toLowerCase()) ||
            description.includes(searchTerm.toLowerCase()) ||
            userName.includes(searchTerm.toLowerCase())) {
            post.style.display = 'block';
            visibleCount++;
        } else {
            post.style.display = 'none';
        }
    });

    // Show no results message if needed
    showSearchResults(visibleCount, searchTerm);
}

// Show loading state during search
function showSearchLoading() {
    const postsContainer = document.getElementById('postsContainer');
    if (postsContainer) {
        // Remove existing messages
        const existingMessage = document.getElementById('searchResultsMessage');
        if (existingMessage) {
            existingMessage.remove();
        }

        const loadingDiv = document.createElement('div');
        loadingDiv.id = 'searchResultsMessage';
        loadingDiv.className = 'col-span-full text-center py-8 text-gray-600';
        loadingDiv.innerHTML = `
            <i class="fas fa-spinner fa-spin text-2xl mb-4"></i>
            <p>Searching posts and profiles...</p>
        `;

        postsContainer.insertBefore(loadingDiv, postsContainer.firstChild);
    }
}

// Display global search results with both posts and profiles
function displayGlobalSearchResults(results, searchTerm) {
    const postsContainer = document.getElementById('postsContainer');
    if (!postsContainer) return;

    // Clear existing content
    const existingPosts = postsContainer.querySelectorAll('[data-post-id]');
    existingPosts.forEach(post => post.style.display = 'none');

    // Remove existing messages
    const existingMessage = document.getElementById('searchResultsMessage');
    if (existingMessage) {
        existingMessage.remove();
    }

    const posts = results.posts || [];
    const users = results.users || [];
    const totalResults = results.totalResults || 0;

    if (totalResults === 0) {
        const messageDiv = document.createElement('div');
        messageDiv.id = 'searchResultsMessage';
        messageDiv.className = 'col-span-full text-center py-8 text-gray-600';
        messageDiv.innerHTML = `
            <i class="fas fa-search text-4xl mb-4 text-gray-400"></i>
            <p>No posts or profiles found for "${searchTerm}"</p>
            <p class="text-sm text-gray-500 mt-2">Try searching with different keywords</p>
        `;
        postsContainer.insertBefore(messageDiv, postsContainer.firstChild);
        return;
    }

    // Create search results header
    const headerDiv = document.createElement('div');
    headerDiv.id = 'searchResultsMessage';
    headerDiv.className = 'col-span-full mb-6';
    headerDiv.innerHTML = `
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <h3 class="text-lg font-semibold text-blue-900 mb-2">
                <i class="fas fa-search mr-2"></i>Search Results for "${searchTerm}"
            </h3>
            <p class="text-blue-700">
                Found ${totalResults} result(s): ${posts.length} post(s) and ${users.length} profile(s)
            </p>
        </div>
    `;
    postsContainer.insertBefore(headerDiv, postsContainer.firstChild);

    // Display user profiles first if any
    if (users.length > 0) {
        const profilesSection = document.createElement('div');
        profilesSection.className = 'col-span-full mb-6';
        profilesSection.innerHTML = `
            <h4 class="text-md font-semibold text-gray-800 mb-4 flex items-center">
                <i class="fas fa-users mr-2"></i>Profiles (${users.length})
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                ${users.map(user => createUserProfileCard(user)).join('')}
            </div>
        `;
        postsContainer.insertBefore(profilesSection, postsContainer.firstChild.nextSibling);
    }

    // Display posts if any
    if (posts.length > 0) {
        const postsSection = document.createElement('div');
        postsSection.className = 'col-span-full mb-6';
        postsSection.innerHTML = `
            <h4 class="text-md font-semibold text-gray-800 mb-4 flex items-center">
                <i class="fas fa-file-alt mr-2"></i>Posts (${posts.length})
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                ${posts.map(post => createPostCard(post)).join('')}
            </div>
        `;

        const insertPosition = users.length > 0 ?
            postsContainer.children[2] :
            postsContainer.children[1];
        postsContainer.insertBefore(postsSection, insertPosition.nextSibling);
    }

    // Initialize action buttons for the new elements
    initializeSearchResultActions();
}

// Create user profile card for search results
function createUserProfileCard(user) {
    const professionalInfo = user.professionalInfo || {};
    const roleColor = getRoleColor(user.role);

    return `
        <div class="bg-white p-4 rounded-lg shadow-md border-l-4 ${roleColor.border} hover:shadow-lg transition-shadow">
            <div class="flex items-center mb-3">
                <img src="${user.profileImageUrl || '/placeholder-avatar.png'}" 
                     alt="Profile" class="w-12 h-12 rounded-full object-cover mr-3">
                <div class="flex-1">
                    <h5 class="font-semibold text-gray-900">${user.firstName} ${user.lastName}</h5>
                    <span class="px-2 py-1 rounded-full text-xs font-medium ${roleColor.bg} ${roleColor.text}">
                        ${user.role}
                    </span>
                </div>
            </div>
            
            ${user.bio ? `<p class="text-sm text-gray-600 mb-3">${user.bio.substring(0, 100)}${user.bio.length > 100 ? '...' : ''}</p>` : ''}
            
            <div class="text-xs text-gray-500 mb-3">
                ${getProfessionalSummary(user.role, professionalInfo)}
            </div>
            
            <div class="flex space-x-2">
                <button class="visit-profile-btn flex-1 bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded text-sm flex items-center justify-center"
                        data-user-id="${user.id}" data-user-role="${user.role}">
                    <i class="fas fa-user mr-1"></i>View Profile
                </button>
                <button class="send-message-btn bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded text-sm"
                        data-user-id="${user.id}" title="Send Message">
                    <i class="fas fa-envelope"></i>
                </button>
            </div>
        </div>
    `;
}

// Create post card for search results
function createPostCard(post) {
    return `
        <div class="bg-white p-4 rounded-lg shadow-md hover:shadow-lg transition-shadow" data-post-id="${post.id}">
            <div class="flex items-center justify-between mb-3">
                <div class="flex items-center">
                    <img src="${post.user?.profileImageUrl || '/placeholder-avatar.png'}" 
                         alt="User" class="w-8 h-8 rounded-full mr-2 object-cover">
                    <div>
                        <h6 class="text-sm font-semibold text-gray-900">${post.user?.firstName || ''} ${post.user?.lastName || ''}</h6>
                        <p class="text-xs text-gray-600">${post.user?.role || ''}</p>
                    </div>
                </div>
                <span class="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs" data-post-type="${post.postType}">
                    ${post.postType}
                </span>
            </div>

            <h5 class="font-bold text-md mb-2">${post.title}</h5>
            <p class="text-gray-700 text-sm mb-3">${post.description.substring(0, 150)}${post.description.length > 150 ? '...' : ''}</p>

            ${post.imageUrl ? `
                <div class="mb-3">
                    <img src="${post.imageUrl}" alt="Post Image" class="w-full h-32 object-cover rounded">
                </div>
            ` : ''}

            <div class="flex justify-between items-center text-xs text-gray-500 mb-3">
                <span>${formatDate(post.createdAt)}</span>
                <div class="flex items-center">
                    <i class="fas fa-heart mr-1"></i>
                    <span>${post.likesCount || 0}</span>
                </div>
            </div>

            <div class="flex space-x-2">
                <button class="like-btn bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded text-xs flex items-center"
                        data-post-id="${post.id}">
                    <i class="fas fa-heart like-icon mr-1"></i>
                    <span class="like-text">Like</span>
                    <span class="like-count ml-1">(${post.likesCount || 0})</span>
                </button>
                <button class="send-invitation-btn bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-xs flex items-center"
                        data-post-id="${post.id}">
                    <i class="fas fa-envelope mr-1"></i>Invite
                </button>
            </div>
        </div>
    `;
}

// Get role-specific colors
function getRoleColor(role) {
    switch (role) {
        case 'ATHLETE':
            return {
                bg: 'bg-green-100',
                text: 'text-green-800',
                border: 'border-green-500'
            };
        case 'COACH':
            return {
                bg: 'bg-blue-100',
                text: 'text-blue-800',
                border: 'border-blue-500'
            };
        case 'SPONSOR':
            return {
                bg: 'bg-purple-100',
                text: 'text-purple-800',
                border: 'border-purple-500'
            };
        default:
            return {
                bg: 'bg-gray-100',
                text: 'text-gray-800',
                border: 'border-gray-500'
            };
    }
}

// Get professional summary for user cards
function getProfessionalSummary(role, professionalInfo) {
    switch (role) {
        case 'ATHLETE':
            const location = professionalInfo.state && professionalInfo.district ? 
                `${professionalInfo.state}, ${professionalInfo.district}` : 
                (professionalInfo.state || 'Location not specified');
            const sport = professionalInfo.sport || 'Sport not specified';
            return `<i class="fas fa-map-marker-alt mr-1"></i>${location} • <i class="fas fa-running mr-1"></i>${sport}`;
            
        case 'COACH':
            const specialization = professionalInfo.specialization || 'Specialization not specified';
            const experience = professionalInfo.experienceYears ? 
                `${professionalInfo.experienceYears} years exp.` : 
                'Experience not specified';
            return `<i class="fas fa-chalkboard-teacher mr-1"></i>${specialization} • <i class="fas fa-clock mr-1"></i>${experience}`;
            
        case 'SPONSOR':
            const company = professionalInfo.companyName || 'Company not specified';
            const industry = professionalInfo.industry || 'Industry not specified';
            return `<i class="fas fa-building mr-1"></i>${company} • <i class="fas fa-industry mr-1"></i>${industry}`;
            
        default:
            return 'Professional information not available';
    }
}

// Initialize action buttons for search results
function initializeSearchResultActions() {
    // Initialize visit profile buttons
    initializeVisitProfileButtons();
    
    // Initialize like buttons
    document.querySelectorAll('.like-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const postId = btn.getAttribute('data-post-id');
            await toggleLike(postId);
        });
    });

    // Initialize invitation buttons
    document.querySelectorAll('.send-invitation-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const postId = btn.getAttribute('data-post-id');
            openInvitationModal(postId);
        });
    });

    // Initialize send message buttons (placeholder functionality)
    document.querySelectorAll('.send-message-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            // TODO: Implement messaging functionality
            showMessage('Messaging feature coming soon!', 'info');
        });
    });
}

function performSearch() {
    const searchInput = document.getElementById('searchInput');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    const searchTerm = searchInput.value.trim();

    if (searchTerm) {
        // Show clear button
        if (clearSearchBtn) {
            clearSearchBtn.style.display = 'flex';
        }

        // Filter posts based on search term
        const posts = document.querySelectorAll('[data-post-id]');
        let visibleCount = 0;

        posts.forEach(post => {
            const title = post.querySelector('h4')?.textContent.toLowerCase() || '';
            const description = post.querySelector('p')?.textContent.toLowerCase() || '';
            const userName = post.querySelector('h3')?.textContent.toLowerCase() || '';

            if (title.includes(searchTerm.toLowerCase()) ||
                description.includes(searchTerm.toLowerCase()) ||
                userName.includes(searchTerm.toLowerCase())) {
                post.style.display = 'block';
                visibleCount++;
            } else {
                post.style.display = 'none';
            }
        });

        // Show no results message if needed
        showSearchResults(visibleCount, searchTerm);
    }
}

// Clear search
function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    const clearSearchBtn = document.getElementById('clearSearchBtn');

    searchInput.value = '';
    if (clearSearchBtn) {
        clearSearchBtn.style.display = 'none';
    }

    // Show all original posts
    const posts = document.querySelectorAll('[data-post-id]');
    posts.forEach(post => {
        post.style.display = 'block';
    });

    // Remove search results
    const searchMessage = document.getElementById('searchResultsMessage');
    if (searchMessage) {
        searchMessage.remove();
    }

    // Remove any search-generated content
    const searchSections = document.querySelectorAll('.col-span-full');
    searchSections.forEach(section => {
        if (section.id !== 'searchResultsMessage' && 
            (section.innerHTML.includes('Profiles (') || section.innerHTML.includes('Posts ('))) {
            section.remove();
        }
    });

    // Reset filters
    const filterTags = document.querySelectorAll('.filter-tag');
    filterTags.forEach(t => {
        t.classList.remove('bg-blue-500', 'text-white');
        t.classList.add('bg-gray-200', 'text-gray-800');
    });
    const allFilter = document.querySelector('.filter-tag[data-type="all"]');
    if (allFilter) {
        allFilter.classList.remove('bg-gray-200', 'text-gray-800');
        allFilter.classList.add('bg-blue-500', 'text-white');
    }
}

// Apply filter - Fixed implementation
function applyFilter(filterType) {
    const posts = document.querySelectorAll('[data-post-id]');
    let visibleCount = 0;

    posts.forEach(post => {
        if (filterType === 'all') {
            post.style.display = 'block';
            visibleCount++;
        } else {
            // Look for post type in the post data
            const postElement = post.querySelector('[data-post-type]');
            const postType = postElement ? postElement.getAttribute('data-post-type') : '';

            // Also check for post type in span elements
            const typeSpans = post.querySelectorAll('span');
            let foundType = false;

            typeSpans.forEach(span => {
                if (span.textContent && span.textContent.trim().toUpperCase() === filterType.toUpperCase()) {
                    foundType = true;
                }
            });

            if (postType.toUpperCase() === filterType.toUpperCase() || foundType) {
                post.style.display = 'block';
                visibleCount++;
            } else {
                post.style.display = 'none';
            }
        }
    });

    // Show filter results message
    showFilterResults(visibleCount, filterType);
}

// Show search results message
function showSearchResults(count, searchTerm) {
    // Remove existing message
    const existingMessage = document.getElementById('searchResultsMessage');
    if (existingMessage) {
        existingMessage.remove();
    }

    const postsContainer = document.getElementById('postsContainer');
    if (postsContainer) {
        const messageDiv = document.createElement('div');
        messageDiv.id = 'searchResultsMessage';
        messageDiv.className = 'col-span-full text-center py-4 text-gray-600';
        messageDiv.innerHTML = count > 0
            ? `Found ${count} result(s) for "${searchTerm}"`
            : `No results found for "${searchTerm}"`;

        postsContainer.insertBefore(messageDiv, postsContainer.firstChild);
    }
}

// Show filter results message
function showFilterResults(count, filterType) {
    // Remove existing message
    const existingMessage = document.getElementById('filterResultsMessage');
    if (existingMessage) {
        existingMessage.remove();
    }

    if (filterType !== 'all') {
        const postsContainer = document.getElementById('postsContainer');
        if (postsContainer) {
            const messageDiv = document.createElement('div');
            messageDiv.id = 'filterResultsMessage';
            messageDiv.className = 'col-span-full text-center py-4 text-gray-600';
            messageDiv.innerHTML = count > 0
                ? `Showing ${count} ${filterType.toLowerCase()} post(s)`
                : `No ${filterType.toLowerCase()} posts found`;

            postsContainer.insertBefore(messageDiv, postsContainer.firstChild);
        }
    }
}

// Initialize invitation tabs - Fixed implementation
function initializeInvitationTabs() {
    const receivedTab = document.getElementById('receivedInvitationsTab');
    const sentTab = document.getElementById('sentInvitationsTab');
    const receivedContent = document.getElementById('receivedInvitations');
    const sentContent = document.getElementById('sentInvitations');

    if (receivedTab && sentTab && receivedContent && sentContent) {
        // Set up click handlers for tabs
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

        // Load initial data (received invitations by default)
        loadReceivedInvitations();
    }
}

// Load invitations data
async function loadInvitations() {
    console.log('Loading invitations...');
    // Load received invitations by default
    loadReceivedInvitations();
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
            showMessage('Failed to load received invitations', 'error');
        }
    } catch (error) {
        console.error('Error loading received invitations:', error);
        showMessage('Error loading received invitations', 'error');
    }
}

// Load sent invitations
async function loadSentInvitations() {
    try {
        const response = await fetch('/api/invitations/sent');
        const result = await response.json();

        if (result.success) {
            displaySentInvitations(result.invitations);
        } else {
            console.error('Failed to load sent invitations:', result.message);
            showMessage('Failed to load sent invitations', 'error');
        }
    } catch (error) {
        console.error('Error loading sent invitations:', error);
        showMessage('Error loading sent invitations', 'error');
    }
}

// Display received invitations
function displayReceivedInvitations(invitations) {
    const container = document.getElementById('receivedInvitationsList');
    if (!container) return;

    if (invitations && invitations.length > 0) {
        container.innerHTML = invitations.map(invitation => `
            <div class="border border-gray-200 rounded-lg p-4 mb-4 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start mb-3">
                    <div class="flex items-center">
                        <img src="${invitation.sender?.profileImageUrl || '/placeholder-avatar.png'}" 
                             alt="Sender" class="w-12 h-12 rounded-full object-cover mr-3">
                        <div>
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
                        </button>
                        <button class="decline-invitation-btn bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded text-sm"
                                data-invitation-id="${invitation.id}">
                            <i class="fas fa-times mr-1"></i>Decline
                        </button>
                    </div>
                ` : ''}
            </div>
        `).join('');

        initializeInvitationActions();
        initializeVisitProfileButtons();
    } else {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="fas fa-inbox text-4xl mb-4"></i>
                <p>No invitations received yet</p>
            </div>
        `;
    }
}

// Display sent invitations
function displaySentInvitations(invitations) {
    const container = document.getElementById('sentInvitationsList');
    if (!container) return;

    if (invitations && invitations.length > 0) {
        container.innerHTML = invitations.map(invitation => `
            <div class="border border-gray-200 rounded-lg p-4 mb-4 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start mb-3">
                    <div class="flex items-center">
                        <img src="${invitation.receiver?.profileImageUrl || '/placeholder-avatar.png'}" 
                             alt="Receiver" class="w-12 h-12 rounded-full object-cover mr-3">
                        <div>
                            <h4 class="font-semibold text-gray-900">${invitation.receiver?.firstName} ${invitation.receiver?.lastName}</h4>
                            <p class="text-sm text-gray-600">${invitation.receiver?.role}</p>
                            <button class="visit-profile-btn text-blue-600 hover:text-blue-800 text-sm"
                                    data-user-id="${invitation.receiver?.id}" data-user-role="${invitation.receiver?.role}">
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
                
                <div class="flex justify-between items-center text-xs text-gray-500">
                    <span>Sent: ${formatDate(invitation.createdAt)}</span>
                    <span>Post Type: ${invitation.post?.postType}</span>
                </div>
            </div>
        `).join('');

        initializeVisitProfileButtons();
    } else {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="fas fa-paper-plane text-4xl mb-4"></i>
                <p>No invitations sent yet</p>
            </div>
        `;
    }
}

// Initialize visit profile buttons - Fixed implementation
function initializeVisitProfileButtons() {
    document.querySelectorAll('.visit-profile-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const userId = btn.getAttribute('data-user-id');
            const userRole = btn.getAttribute('data-user-role');

            if (userId && userRole) {
                visitProfile(userId, userRole);
            }
        });
    });
}

// Visit profile functionality - Fixed implementation
function visitProfile(userId, userRole) {
    if (!userId || !userRole) {
        showMessage('Invalid profile information', 'error');
        return;
    }

    // Construct the profile URL based on user role
    const profileUrl = `/${userRole.toLowerCase()}/profile/${userId}`;

    // Navigate to the profile page
    window.location.href = profileUrl;
}

// Initialize invitation action buttons
function initializeInvitationActions() {
    document.querySelectorAll('.accept-invitation-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const invitationId = btn.getAttribute('data-invitation-id');
            await respondToInvitation(invitationId, 'ACCEPTED');
        });
    });

    document.querySelectorAll('.decline-invitation-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const invitationId = btn.getAttribute('data-invitation-id');
            await respondToInvitation(invitationId, 'DECLINED');
        });
    });
}

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

        const result = await response.json();

        if (result.success) {
            showMessage(`Invitation ${status.toLowerCase()} successfully`, 'success');
            loadReceivedInvitations(); // Reload the invitations
        } else {
            showMessage(result.message || 'Failed to respond to invitation', 'error');
        }
    } catch (error) {
        console.error('Error responding to invitation:', error);
        showMessage('Error responding to invitation', 'error');
    }
}

// Get invitation status color
function getInvitationStatusColor(status) {
    switch (status) {
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

// Post actions initialization
function initializePostActions() {
    document.querySelectorAll('.like-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const postId = btn.getAttribute('data-post-id');
            await toggleLike(postId);
        });
    });

    document.querySelectorAll('.send-invitation-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const postId = btn.getAttribute('data-post-id');
            openInvitationModal(postId);
        });
    });

    initializeVisitProfileButtons();
}

// Toggle like function
async function toggleLike(postId) {
    try {
        const response = await fetch(`/api/posts/${postId}/like`, {
            method: 'POST'
        });

        const result = await response.json();

        if (result.success) {
            const likeBtn = document.querySelector(`.like-btn[data-post-id="${postId}"]`);
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

// Profile functionality
async function loadProfileData() {
    try {
        const response = await fetch('/api/profile/data');
        const result = await response.json();

        if (result.success) {
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

function displayPersonalInfo(personalData) {
    if (!personalData) return;

    const profileImage = document.getElementById('profileImage');
    if (profileImage && personalData.profileImageUrl) {
        profileImage.src = personalData.profileImageUrl;
    }

    const fullName = document.getElementById('fullName');
    if (fullName) {
        fullName.textContent = `${personalData.firstName || ''} ${personalData.lastName || ''}`.trim() || 'Not provided';
    }

    const userEmail = document.getElementById('userEmail');
    if (userEmail) {
        userEmail.textContent = personalData.email || 'Not provided';
    }

    // Update other fields...
    ['phone', 'country', 'gender', 'aadhaar', 'bio'].forEach(field => {
        const element = document.getElementById(`${field}Display`);
        if (element) {
            element.textContent = personalData[field] || 'Not provided';
        }
    });

    populateEditForm(personalData);
}

function displayProfessionalInfo(professionalData, role) {
    const professionalDisplay = document.getElementById('professionalDisplay');
    if (!professionalDisplay) return;

    let professionalHTML = '';
    if (!professionalData || Object.keys(professionalData).length === 0) {
        professionalHTML = '<div class="text-center text-gray-500 py-8"><p>No professional information available</p></div>';
    } else {
        switch (role) {
            case 'athlete':
                professionalHTML = generateAthleteProfile(professionalData);
                break;
            case 'coach':
                professionalHTML = generateCoachProfile(professionalData);
                break;
            case 'sponsor':
                professionalHTML = generateSponsorProfile(professionalData);
                break;
            default:
                professionalHTML = '<div class="text-center text-gray-500 py-8"><p>No professional information available</p></div>';
        }
    }
    professionalDisplay.innerHTML = professionalHTML;
}

function generateAthleteProfile(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Location</label>
                <p class="text-gray-900">${data.state && data.district ? data.state + ', ' + data.district : 'Not specified'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Height</label>
                <p class="text-gray-900">${data.height ? data.height + ' cm' : 'Not specified'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Weight</label>
                <p class="text-gray-900">${data.weight ? data.weight + ' kg' : 'Not specified'}</p>
            </div>
        </div>
    `;
}

function generateCoachProfile(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Specialization</label>
                <p class="text-gray-900">${data.specialization || 'Not specified'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Experience</label>
                <p class="text-gray-900">${data.experienceYears ? data.experienceYears + ' years' : 'Not specified'}</p>
            </div>
        </div>
    `;
}

function generateSponsorProfile(data) {
    return `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Company</label>
                <p class="text-gray-900">${data.companyName || 'Not specified'}</p>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Industry</label>
                <p class="text-gray-900">${data.industry || 'Not specified'}</p>
            </div>
        </div>
    `;
}

function populateEditForm(personalData) {
    ['firstName', 'lastName', 'phone', 'country', 'gender', 'aadhaarNumber', 'bio'].forEach(field => {
        const element = document.getElementById(field);
        if (element) {
            element.value = personalData[field] || '';
        }
    });
}

function initializeProfileHandlers() {
    const editPersonalBtn = document.getElementById('editPersonalBtn');
    const cancelPersonalBtn = document.getElementById('cancelPersonalBtn');
    const personalDisplay = document.getElementById('personalDisplay');
    const personalEdit = document.getElementById('personalEdit');
    const personalForm = document.getElementById('personalForm');

    if (editPersonalBtn && personalDisplay && personalEdit) {
        editPersonalBtn.addEventListener('click', () => {
            personalDisplay.style.display = 'none';
            personalEdit.style.display = 'block';
        });

        if (cancelPersonalBtn) {
            cancelPersonalBtn.addEventListener('click', () => {
                personalDisplay.style.display = 'block';
                personalEdit.style.display = 'none';
            });
        }

        if (personalForm) {
            personalForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const formData = new FormData(personalForm);

                try {
                    const response = await fetch('/api/profile/personal', {
                        method: 'PUT',
                        body: formData
                    });

                    const result = await response.json();

                    if (result.success) {
                        showMessage('Profile updated successfully!', 'success');
                        personalDisplay.style.display = 'block';
                        personalEdit.style.display = 'none';
                        loadProfileData();
                    } else {
                        showMessage(result.message || 'Failed to update profile', 'error');
                    }
                } catch (error) {
                    console.error('Error updating profile:', error);
                    showMessage('An error occurred while updating the profile', 'error');
                }
            });
        }
    }
}

// Achievement functionality
function initializeAchievements() {
    const addAchievementBtn = document.getElementById('addAchievementBtn');
    const achievementModal = document.getElementById('achievementModal');
    const closeAchievementModal = document.getElementById('closeAchievementModal');
    const cancelAchievement = document.getElementById('cancelAchievement');
    const achievementForm = document.getElementById('achievementForm');

    if (!addAchievementBtn || !achievementModal || !achievementForm) {
        return;
    }

    addAchievementBtn.addEventListener('click', () => {
        achievementModal.classList.add('active');
        achievementForm.reset();
    });

    [closeAchievementModal, cancelAchievement].forEach(btn => {
        if (btn) {
            btn.addEventListener('click', () => {
                achievementModal.classList.remove('active');
                achievementForm.reset();
            });
        }
    });

    achievementModal.addEventListener('click', (e) => {
        if (e.target === achievementModal) {
            achievementModal.classList.remove('active');
            achievementForm.reset();
        }
    });

    achievementForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(achievementForm);

        try {
            const response = await fetch('/api/achievements/add', {
                method: 'POST',
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                achievementModal.classList.remove('active');
                achievementForm.reset();
                showMessage('Achievement added successfully!', 'success');
                loadUserAchievements();
            } else {
                showMessage(result.message || 'Failed to add achievement', 'error');
            }
        } catch (error) {
            console.error('Error adding achievement:', error);
            showMessage('An error occurred while adding the achievement', 'error');
        }
    });

    loadUserAchievements();
}

async function loadUserAchievements() {
    try {
        const response = await fetch('/api/achievements/my');
        const result = await response.json();

        if (result.success) {
            displayAchievements(result.achievements);
        } else {
            console.error('Failed to load achievements:', result.message);
        }
    } catch (error) {
        console.error('Error loading achievements:', error);
    }
}

function displayAchievements(achievements) {
    const achievementsList = document.getElementById('achievementsList');
    const noAchievements = document.getElementById('noAchievements');

    if (!achievementsList) return;

    if (achievements && achievements.length > 0) {
        if (noAchievements) noAchievements.style.display = 'none';

        achievementsList.innerHTML = achievements.map(achievement => `
            <div class="bg-gray-50 p-4 rounded-lg border border-gray-200 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start mb-2">
                    <div class="flex items-center">
                        <div class="w-12 h-12 bg-gradient-to-r from-yellow-400 to-yellow-600 rounded-full flex items-center justify-center mr-3">
                            <i class="fas fa-trophy text-white text-lg"></i>
                        </div>
                        <div>
                            <h4 class="font-semibold text-gray-900">${achievement.title}</h4>
                            <p class="text-sm text-gray-600">${achievement.organization || 'Personal Achievement'}</p>
                        </div>
                    </div>
                    <button class="delete-achievement-btn text-red-500 hover:text-red-700 text-sm" 
                            data-achievement-id="${achievement.id}" title="Delete achievement">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
                
                ${achievement.description ? `<p class="text-gray-700 text-sm mb-2">${achievement.description}</p>` : ''}
                
                <div class="flex justify-between items-center text-xs text-gray-500">
                    <div class="flex items-center space-x-4">
                        ${achievement.achievementDate ? `
                            <span class="flex items-center">
                                <i class="fas fa-calendar mr-1"></i>
                                ${formatDate(achievement.achievementDate)}
                            </span>
                        ` : ''}
                    </div>
                </div>
            </div>
        `).join('');

        document.querySelectorAll('.delete-achievement-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const achievementId = btn.getAttribute('data-achievement-id');
                if (confirm('Are you sure you want to delete this achievement?')) {
                    try {
                        const response = await fetch(`/api/achievements/${achievementId}`, {
                            method: 'DELETE'
                        });
                        const result = await response.json();
                        if (result.success) {
                            showMessage('Achievement deleted successfully', 'success');
                            loadUserAchievements();
                        } else {
                            showMessage(result.message || 'Failed to delete achievement', 'error');
                        }
                    } catch (error) {
                        console.error('Error deleting achievement:', error);
                        showMessage('An error occurred while deleting the achievement', 'error');
                    }
                }
            });
        });
    } else {
        if (noAchievements) noAchievements.style.display = 'block';
        achievementsList.innerHTML = '';
    }
}

// Modal functions
function openInvitationModal(postId) {
    const invitationModal = document.getElementById('invitationModal');
    const postPreview = document.getElementById('postPreview');
    const invitationPostId = document.getElementById('invitationPostId');

    if (!invitationModal || !postPreview || !invitationPostId) {
        console.error('Invitation modal elements not found');
        return;
    }

    invitationPostId.value = postId;

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

    invitationModal.classList.add('active');
}

function initializeInvitationModal() {
    const invitationModal = document.getElementById('invitationModal');
    const closeInvitationModal = document.getElementById('closeInvitationModal');
    const cancelInvitation = document.getElementById('cancelInvitation');
    const invitationForm = document.getElementById('invitationForm');

    if (!invitationModal) return;

    [closeInvitationModal, cancelInvitation].forEach(btn => {
        if (btn) {
            btn.addEventListener('click', () => {
                invitationModal.classList.remove('active');
                if (invitationForm) invitationForm.reset();
            });
        }
    });

    invitationModal.addEventListener('click', (e) => {
        if (e.target === invitationModal) {
            invitationModal.classList.remove('active');
            if (invitationForm) invitationForm.reset();
        }
    });

    if (invitationForm) {
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
                    invitationModal.classList.remove('active');
                    invitationForm.reset();
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
}

function initializeFloatingActionButton() {
    const fabNewPost = document.getElementById('fabNewPost');
    const newPostBtn = document.getElementById('newPostBtn');

    if (fabNewPost && newPostBtn) {
        fabNewPost.addEventListener('click', () => {
            newPostBtn.click();
        });
    }
}

function initializeNewPostModal() {
    const newPostBtn = document.getElementById('newPostBtn');
    const fabNewPost = document.getElementById('fabNewPost');
    const newPostModal = document.getElementById('newPostModal');
    const closeModal = document.getElementById('closeModal');
    const cancelPost = document.getElementById('cancelPost');
    const newPostForm = document.getElementById('newPostForm');

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

    if (newPostModal) {
        newPostModal.addEventListener('click', (e) => {
            if (e.target === newPostModal) {
                newPostModal.classList.remove('active');
                newPostModal.style.display = 'none';
                if (newPostForm) newPostForm.reset();
            }
        });
    }

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
                    newPostModal.classList.remove('active');
                    newPostModal.style.display = 'none';
                    newPostForm.reset();
                    showMessage('Post created successfully!', 'success');
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

// Initialize the page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    initializeNewPostModal();
});
