// Gemini Deep Research - Client-side JavaScript

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Handle template form
    const templateForm = document.getElementById('templateForm');
    if (templateForm) {
        const submitBtn = document.getElementById('submitBtn');
        if (submitBtn) {
            submitBtn.addEventListener('click', function() {
                applyTemplate();
            });
        }
    }
    // Check for notification checkboxes
    const sendEmailCheckbox = document.getElementById('sendEmail');
    const sendSmsCheckbox = document.getElementById('sendSms');
    const sendWhatsappCheckbox = document.getElementById('sendWhatsapp');
    const emailSection = document.getElementById('emailSection');
    const phoneSection = document.getElementById('phoneSection');
    
    // Toggle notification fields visibility
    if (sendEmailCheckbox) {
        sendEmailCheckbox.addEventListener('change', function() {
            emailSection.classList.toggle('d-none', !this.checked);
            
            // If unchecked, clear email input
            if (!this.checked) {
                document.getElementById('notificationEmail').value = '';
            }
        });
    }
    
    if (sendSmsCheckbox || sendWhatsappCheckbox) {
        const togglePhoneSection = function() {
            const smsChecked = sendSmsCheckbox ? sendSmsCheckbox.checked : false;
            const whatsappChecked = sendWhatsappCheckbox ? sendWhatsappCheckbox.checked : false;
            phoneSection.classList.toggle('d-none', !(smsChecked || whatsappChecked));
            
            // If both unchecked, clear phone input
            if (!(smsChecked || whatsappChecked)) {
                document.getElementById('notificationPhone').value = '';
            }
        };
        
        if (sendSmsCheckbox) {
            sendSmsCheckbox.addEventListener('change', togglePhoneSection);
        }
        
        if (sendWhatsappCheckbox) {
            sendWhatsappCheckbox.addEventListener('change', togglePhoneSection);
        }
    }
    
    // Handle prompt form submission
    const promptForm = document.getElementById('promptForm');
    if (promptForm) {
        promptForm.addEventListener('submit', function(event) {
            event.preventDefault();
            submitPrompt();
        });
    }
    
    // Handle file upload form submission
    const fileUploadForm = document.getElementById('fileUploadForm');
    if (fileUploadForm) {
        fileUploadForm.addEventListener('submit', function(event) {
            event.preventDefault();
            uploadFile();
        });
    }
    
    // Handle Google Sheets import form submission
    const sheetsForm = document.getElementById('sheetsForm');
    if (sheetsForm) {
        sheetsForm.addEventListener('submit', function(event) {
            event.preventDefault();
            importFromGoogleSheets();
        });
    }
    
    // Refresh prompts list buttons
    const refreshPromptsBtn = document.getElementById('refreshPrompts');
    if (refreshPromptsBtn) {
        refreshPromptsBtn.addEventListener('click', function() {
            loadPrompts();
        });
    }
    
    const refreshPromptsAdminBtn = document.getElementById('refreshPromptsAdmin');
    if (refreshPromptsAdminBtn) {
        refreshPromptsAdminBtn.addEventListener('click', function() {
            // Reload the page to refresh all dashboard data
            window.location.reload();
        });
    }
    
    // Status filter dropdown in admin page
    const statusFilters = document.querySelectorAll('.dropdown-item[data-status]');
    statusFilters.forEach(function(filter) {
        filter.addEventListener('click', function(event) {
            event.preventDefault();
            const status = this.dataset.status;
            
            // If status is ALL, reload the page
            if (status === 'ALL') {
                window.location.reload();
                return;
            }
            
            // Otherwise, filter the table
            filterPromptsByStatus(status);
        });
    });
    
    // Process buttons in admin page
    document.querySelectorAll('.process-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const promptId = this.dataset.id;
            processPrompt(promptId);
        });
    });
    
    // Delete buttons in admin page
    document.querySelectorAll('.delete-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const promptId = this.dataset.id;
            deletePrompt(promptId);
        });
    });
    
    // Load prompts on home page if list exists
    if (document.getElementById('promptsList') && window.location.pathname === '/') {
        loadPrompts();
    }
});

// Submit a new prompt
function submitPrompt() {
    const content = document.getElementById('content').value.trim();
    if (!content) {
        showAlert('Please enter a research prompt.', 'danger', 'submitResult');
        return;
    }
    
    // Prepare the request data
    const data = {
        content: content,
        createdBy: document.getElementById('createdBy').value.trim() || 'Anonymous',
        source: 'WEB'
    };
    
    // Add notification details if checked
    if (document.getElementById('sendEmail').checked) {
        const email = document.getElementById('notificationEmail').value.trim();
        if (email) {
            data.notificationEmail = email;
        } else {
            showAlert('Please enter an email address for notifications.', 'danger', 'submitResult');
            return;
        }
    }
    
    if (document.getElementById('sendSms').checked || document.getElementById('sendWhatsapp').checked) {
        const phone = document.getElementById('notificationPhone').value.trim();
        if (phone) {
            data.notificationPhone = phone;
            data.sendSms = document.getElementById('sendSms').checked;
            data.sendWhatsapp = document.getElementById('sendWhatsapp').checked;
        } else {
            showAlert('Please enter a phone number for notifications.', 'danger', 'submitResult');
            return;
        }
    }
    
    // Send the request
    fetch('/api/prompts', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        return response.json();
    })
    .then(result => {
        showAlert('Prompt submitted successfully! ID: ' + result.id, 'success', 'submitResult');
        document.getElementById('promptForm').reset();
        
        // Reload the prompts list
        loadPrompts();
    })
    .catch(error => {
        console.error('Error submitting prompt:', error);
        showAlert('Error submitting prompt: ' + (error.error || error.message || 'Unknown error'), 'danger', 'submitResult');
    });
}

// Upload a file with prompts
function uploadFile() {
    const fileInput = document.getElementById('file');
    if (!fileInput.files.length) {
        showAlert('Please select a file to upload.', 'danger', 'fileUploadResult');
        return;
    }
    
    const formData = new FormData(document.getElementById('fileUploadForm'));
    
    fetch('/api/upload/text', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        return response.json();
    })
    .then(result => {
        showAlert('File processed successfully! ' + result.promptsCreated + ' prompts created.', 'success', 'fileUploadResult');
        document.getElementById('fileUploadForm').reset();
        
        // Reload the prompts list
        loadPrompts();
    })
    .catch(error => {
        console.error('Error uploading file:', error);
        showAlert('Error uploading file: ' + (error.error || error.message || 'Unknown error'), 'danger', 'fileUploadResult');
    });
}

// Import prompts from Google Sheets
function importFromGoogleSheets() {
    const spreadsheetId = document.getElementById('spreadsheetId').value.trim();
    const range = document.getElementById('range').value.trim();
    
    if (!spreadsheetId || !range) {
        showAlert('Please enter both Spreadsheet ID and Range.', 'danger', 'sheetsImportResult');
        return;
    }
    
    const formData = new FormData(document.getElementById('sheetsForm'));
    
    // Convert formData to URL params
    const params = new URLSearchParams();
    for (const pair of formData) {
        params.append(pair[0], pair[1]);
    }
    
    fetch('/api/sheets/process?' + params.toString(), {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        return response.json();
    })
    .then(result => {
        showAlert('Google Sheet processed successfully! ' + result.promptsCreated + ' prompts created.', 'success', 'sheetsImportResult');
        document.getElementById('sheetsForm').reset();
        
        // Reload the prompts list
        loadPrompts();
    })
    .catch(error => {
        console.error('Error processing Google Sheet:', error);
        showAlert('Error processing Google Sheet: ' + (error.error || error.message || 'Unknown error'), 'danger', 'sheetsImportResult');
    });
}

// Load prompts for home page
function loadPrompts() {
    const promptsList = document.getElementById('promptsList');
    if (!promptsList) return;
    
    fetch('/api/prompts')
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load prompts');
        }
        return response.json();
    })
    .then(prompts => {
        // Display prompts in the table
        if (prompts.length === 0) {
            promptsList.innerHTML = `
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Prompt</th>
                            <th>Status</th>
                            <th>Created</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td colspan="5" class="text-center">No prompts found</td>
                        </tr>
                    </tbody>
                </table>
            `;
            return;
        }
        
        // Sort prompts by creation date (newest first)
        prompts.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        
        // Take only the 10 most recent prompts
        const recentPrompts = prompts.slice(0, 10);
        
        let html = `
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Prompt</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        recentPrompts.forEach(prompt => {
            const createdDate = new Date(prompt.createdAt);
            const formattedDate = createdDate.toLocaleDateString() + ' ' + createdDate.toLocaleTimeString();
            
            html += `
                <tr>
                    <td>${prompt.id}</td>
                    <td>${prompt.content.length > 50 ? prompt.content.substring(0, 50) + '...' : prompt.content}</td>
                    <td><span class="badge ${getStatusBadgeClass(prompt.status)}">${prompt.status}</span></td>
                    <td>${formattedDate}</td>
                    <td>
                        <a href="/prompts/${prompt.id}" class="btn btn-sm btn-outline-primary">View</a>
                    </td>
                </tr>
            `;
        });
        
        html += `
                </tbody>
            </table>
        `;
        
        promptsList.innerHTML = html;
    })
    .catch(error => {
        console.error('Error loading prompts:', error);
        promptsList.innerHTML = `
            <div class="alert alert-danger">
                Error loading prompts: ${error.message}
            </div>
        `;
    });
}

// Process a prompt (admin action)
function processPrompt(promptId) {
    if (!promptId) return;
    
    // Update UI to show processing
    const promptRow = document.getElementById('prompt-' + promptId);
    if (promptRow) {
        const statusCell = promptRow.querySelector('td:nth-child(3)');
        const originalHtml = statusCell.innerHTML;
        statusCell.innerHTML = '<div class="processing-indicator"><div></div><div></div><div></div><div></div></div>';
        
        // Disable action buttons
        const actionButtons = promptRow.querySelectorAll('button');
        actionButtons.forEach(btn => btn.disabled = true);
    }
    
    fetch('/api/prompts/' + promptId + '/process', {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        return response.json();
    })
    .then(result => {
        // Reload the page to update all data
        window.location.reload();
    })
    .catch(error => {
        console.error('Error processing prompt:', error);
        
        // Restore original status display
        if (promptRow) {
            const statusCell = promptRow.querySelector('td:nth-child(3)');
            statusCell.innerHTML = originalHtml;
            
            // Re-enable action buttons
            const actionButtons = promptRow.querySelectorAll('button');
            actionButtons.forEach(btn => btn.disabled = false);
        }
        
        alert('Error processing prompt: ' + (error.error || error.message || 'Unknown error'));
    });
}

// Delete a prompt (admin action)
function deletePrompt(promptId) {
    if (!promptId) return;
    
    if (!confirm('Are you sure you want to delete this prompt? This action cannot be undone.')) {
        return;
    }
    
    fetch('/api/prompts/' + promptId, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        
        // Remove the row from the table
        const promptRow = document.getElementById('prompt-' + promptId);
        if (promptRow) {
            promptRow.remove();
        }
        
        // Show success message
        alert('Prompt deleted successfully');
    })
    .catch(error => {
        console.error('Error deleting prompt:', error);
        alert('Error deleting prompt: ' + (error.error || error.message || 'Unknown error'));
    });
}

// Filter prompts by status (admin page)
function filterPromptsByStatus(status) {
    if (!status) return;
    
    const promptRows = document.querySelectorAll('tbody tr');
    promptRows.forEach(row => {
        const statusCell = row.querySelector('td:nth-child(3)');
        if (statusCell) {
            const rowStatus = statusCell.textContent.trim();
            if (rowStatus === status) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        }
    });
}

// Utility function to get Bootstrap badge class for status
function getStatusBadgeClass(status) {
    switch (status) {
        case 'COMPLETED':
            return 'bg-success';
        case 'PENDING':
            return 'bg-warning';
        case 'IN_PROGRESS':
            return 'bg-info';
        case 'ERROR':
            return 'bg-danger';
        default:
            return 'bg-secondary';
    }
}

// Show alert message
function showAlert(message, type, containerId) {
    const alertContainer = document.getElementById(containerId);
    if (!alertContainer) return;
    
    alertContainer.innerHTML = message;
    alertContainer.className = `alert alert-${type} mt-3`;
    
    // Remove d-none class to show the alert
    alertContainer.classList.remove('d-none');
    
    // Automatically hide success alerts after 5 seconds
    if (type === 'success') {
        setTimeout(() => {
            alertContainer.classList.add('d-none');
        }, 5000);
    }
}

// Apply template with variables and create a prompt
function applyTemplate() {
    const templateId = document.getElementById('templateId').value;
    
    // Collect variables from placeholder inputs
    const variables = {};
    document.querySelectorAll('.placeholder-input').forEach(input => {
        const placeholder = input.getAttribute('data-placeholder');
        const value = input.value.trim();
        
        if (placeholder) {
            variables[placeholder] = value;
        }
    });
    
    // Get notification details
    const email = document.getElementById('notificationEmail') ? document.getElementById('notificationEmail').value.trim() : '';
    const phone = document.getElementById('notificationPhone') ? document.getElementById('notificationPhone').value.trim() : '';
    const sendSms = document.getElementById('sendSms') ? document.getElementById('sendSms').checked : false;
    const sendWhatsapp = document.getElementById('sendWhatsapp') ? document.getElementById('sendWhatsapp').checked : false;
    
    // Validate notification options
    if (email === '' && (phone === '' || (!sendSms && !sendWhatsapp))) {
        if (!confirm('No notification method selected. You will not be notified when the research is complete. Continue anyway?')) {
            return;
        }
    }
    
    // Prepare the request data
    const data = {
        templateId: templateId,
        variables: variables,
        notificationEmail: email,
        notificationPhone: phone,
        sendSms: sendSms,
        sendWhatsapp: sendWhatsapp
    };
    
    // Send the request
    fetch('/api/templates/apply', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        return response.json();
    })
    .then(result => {
        // Show the success modal
        document.getElementById('promptIdText').textContent = result.id;
        document.getElementById('viewPromptBtn').href = '/prompts/' + result.id;
        new bootstrap.Modal(document.getElementById('resultModal')).show();
    })
    .catch(error => {
        console.error('Error applying template:', error);
        document.getElementById('errorText').textContent = error.error || error.message || 'Unknown error';
        new bootstrap.Modal(document.getElementById('errorModal')).show();
    });
}