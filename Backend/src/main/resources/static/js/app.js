// Sports Bridge Authentication System - Aadhaar Based

// Global Variables
let currentUser = null;
let userRole = null;
let currentStep = 1;
let aadhaarData = {};

// Main Application Object
const SportsBrewge = {

    // Initialize the application
    init: function() {
        this.setupEventListeners();
        this.initializeAuthFlow();
        this.loadUserData();
    },

    // Setup event listeners for authentication flow
    setupEventListeners: function() {
        // Aadhaar form submission
        const aadhaarForm = document.getElementById('aadhaarForm');
        if (aadhaarForm) {
            aadhaarForm.addEventListener('submit', this.handleAadhaarSubmission.bind(this));
        }

        // OTP form submission
        const otpForm = document.getElementById('otpForm');
        if (otpForm) {
            otpForm.addEventListener('submit', this.handleOTPVerification.bind(this));
        }

        // Profile form submission
        const profileForm = document.getElementById('profileForm');
        if (profileForm) {
            profileForm.addEventListener('submit', this.handleProfileSubmission.bind(this));
        }

        // Role selection functionality
        document.querySelectorAll('.role-card').forEach(card => {
            card.addEventListener('click', this.handleRoleSelection.bind(this));
        });

        // Aadhaar number formatting
        const aadhaarInput = document.getElementById('aadhaarNumber');
        if (aadhaarInput) {
            aadhaarInput.addEventListener('input', this.formatAadhaarNumber);
        }

        // Phone number formatting
        const phoneInput = document.getElementById('phone');
        if (phoneInput) {
            phoneInput.addEventListener('input', this.formatPhoneNumber);
        }

        // OTP input formatting
        const otpInput = document.getElementById('otp');
        if (otpInput) {
            otpInput.addEventListener('input', this.formatOTPInput);
        }

        // Password confirmation validation
        const confirmPassword = document.getElementById('confirmPassword');
        if (confirmPassword) {
            confirmPassword.addEventListener('input', this.validatePasswordConfirmation);
        }
    },

    // Initialize authentication flow
    initializeAuthFlow: function() {
        this.showStep(1);
    },

    // Handle Aadhaar form submission
    handleAadhaarSubmission: function(event) {
        event.preventDefault();

        const aadhaarNumber = document.getElementById('aadhaarNumber').value.replace(/\s/g, '');
        const phone = document.getElementById('phone').value;

        // Validate Aadhaar number (12 digits)
        if (aadhaarNumber.length !== 12 || !/^\d{12}$/.test(aadhaarNumber)) {
            this.showAlert('Please enter a valid 12-digit Aadhaar number', 'danger');
            return;
        }

        // Validate phone number (10 digits)
        if (phone.length !== 10 || !/^\d{10}$/.test(phone)) {
            this.showAlert('Please enter a valid 10-digit mobile number', 'danger');
            return;
        }

        // Store data for next step
        aadhaarData.aadhaarNumber = aadhaarNumber;
        aadhaarData.phone = phone;

        // Show loading
        this.showAlert('Sending OTP to your mobile number...', 'info');

        // Simulate API call (replace with actual backend call)
        setTimeout(() => {
            this.proceedToOTPStep();
        }, 2000);
    },

    // Proceed to OTP verification step
    proceedToOTPStep: function() {
        // Fill hidden fields
        document.getElementById('hiddenAadhaar').value = aadhaarData.aadhaarNumber;
        document.getElementById('hiddenPhone').value = aadhaarData.phone;

        // Show masked phone number
        const maskedPhone = aadhaarData.phone.replace(/(\d{6})(\d{4})/, 'XXXXXX$2');
        document.getElementById('maskedPhone').textContent = `+91 ${maskedPhone}`;

        this.showStep(2);
        this.showAlert('OTP sent successfully! Please check your mobile.', 'success');
    },

    // Handle OTP verification
    handleOTPVerification: function(event) {
        event.preventDefault();

        const otp = document.getElementById('otp').value;

        if (otp.length !== 6 || !/^\d{6}$/.test(otp)) {
            this.showAlert('Please enter a valid 6-digit OTP', 'danger');
            return;
        }

        // Show loading
        this.showAlert('Verifying OTP...', 'info');

        // Simulate API call for OTP verification
        setTimeout(() => {
            // Simulate checking if user exists
            const userExists = this.checkIfUserExists();

            if (userExists) {
                // Existing user - redirect to dashboard
                this.showStep(4);
                setTimeout(() => {
                    this.redirectToDashboard();
                }, 2000);
            } else {
                // New user - proceed to registration
                this.proceedToRegistration();
            }
        }, 1500);
    },

    // Check if user exists (simulate)
    checkIfUserExists: function() {
        // This would be replaced with actual backend call
        // For demo, randomly decide if user exists
        return Math.random() > 0.7; // 30% chance user exists
    },

    // Proceed to registration step
    proceedToRegistration: function() {
        // Fill hidden fields with Aadhaar data
        document.getElementById('finalAadhaar').value = aadhaarData.aadhaarNumber;
        document.getElementById('finalPhone').value = aadhaarData.phone;

        this.showStep(3);
        this.showAlert('Please complete your profile to create your account', 'info');
    },

    // Handle role selection
    handleRoleSelection: function(event) {
        const card = event.currentTarget;
        const role = card.dataset.role;

        // Remove selected class from all cards
        document.querySelectorAll('.role-card').forEach(c => c.classList.remove('selected'));

        // Add selected class to clicked card
        card.classList.add('selected');

        // Update hidden input
        document.getElementById('selectedRole').value = role;

        console.log('Selected Role:', role);
    },

    // Handle profile form submission
    handleProfileSubmission: function(event) {
        const selectedRole = document.getElementById('selectedRole').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Validate role selection
        if (!selectedRole) {
            event.preventDefault();
            this.showAlert('Please select a role before proceeding!', 'warning');
            return;
        }

        // Validate password confirmation
        if (password !== confirmPassword) {
            event.preventDefault();
            this.showAlert('Passwords do not match!', 'danger');
            return;
        }

        this.showAlert('Creating your account...', 'info');
        // Form will submit to backend
    },

    // Format Aadhaar number input
    formatAadhaarNumber: function() {
        let value = this.value.replace(/\D/g, ''); // Remove non-digits
        value = value.replace(/(\d{4})(?=\d)/g, '$1 '); // Add spaces every 4 digits
        if (value.length > 14) value = value.slice(0, 14); // Limit to 12 digits + 2 spaces
        this.value = value;
    },

    // Format phone number input
    formatPhoneNumber: function() {
        let value = this.value.replace(/\D/g, ''); // Remove non-digits
        if (value.length > 10) value = value.slice(0, 10); // Limit to 10 digits
        this.value = value;
    },

    // Format OTP input
    formatOTPInput: function() {
        let value = this.value.replace(/\D/g, ''); // Remove non-digits
        if (value.length > 6) value = value.slice(0, 6); // Limit to 6 digits
        this.value = value;
    },

    // Validate password confirmation
    validatePasswordConfirmation: function() {
        const password = document.getElementById('password');
        const confirmPassword = document.getElementById('confirmPassword');

        if (password && confirmPassword) {
            if (password.value !== confirmPassword.value) {
                confirmPassword.setCustomValidity('Passwords do not match');
                confirmPassword.classList.add('is-invalid');
            } else {
                confirmPassword.setCustomValidity('');
                confirmPassword.classList.remove('is-invalid');
            }
        }
    },

    // Show specific step
    showStep: function(stepNumber) {
        // Hide all steps
        document.querySelectorAll('.auth-step').forEach(step => {
            step.classList.add('d-none');
        });

        // Show requested step
        const targetStep = document.getElementById(`step${stepNumber}`);
        if (targetStep) {
            targetStep.classList.remove('d-none');
            targetStep.classList.add('fade-in');
        }

        currentStep = stepNumber;
    },

    // Show alert messages
    showAlert: function(message, type = 'info') {
        // Remove existing alerts
        const existingAlerts = document.querySelectorAll('.alert');
        existingAlerts.forEach(alert => alert.remove());

        const alertHtml = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                <i class="fas fa-${this.getAlertIcon(type)}"></i> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        // Insert at top of card body
        const container = document.querySelector('.card-body');
        if (container) {
            container.insertAdjacentHTML('afterbegin', alertHtml);
        }
    },

    // Get appropriate icon for alert type
    getAlertIcon: function(type) {
        const icons = {
            'info': 'info-circle',
            'success': 'check-circle',
            'warning': 'exclamation-triangle',
            'danger': 'times-circle'
        };
        return icons[type] || 'info-circle';
    },

    // Load user data from session/localStorage
    loadUserData: function() {
        const userData = localStorage.getItem('userData');
        if (userData) {
            currentUser = JSON.parse(userData);
            userRole = currentUser.role;
        }
    },

    // Redirect to dashboard based on user role
    redirectToDashboard: function() {
        window.location.href = '/dashboard';
    }
};

// Global functions for HTML onclick events
function resendOTP() {
    SportsBrewge.showAlert('OTP resent successfully!', 'success');
    // Simulate resending OTP
    console.log('Resending OTP to:', aadhaarData.phone);
}

function redirectToDashboard() {
    SportsBrewge.redirectToDashboard();
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    SportsBrewge.init();
});

// Export for use in other files
window.SportsBrewge = SportsBrewge;
