import os
import re

file_path = r"d:\Eng App\mobile\app\src\main\java\com\esdispatch\viewmodel\DeliveryViewModel.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Change inheritance
content = re.sub(r'class DeliveryViewModel : ViewModel\(\) \{', r'class DeliveryViewModel : AuthViewModel() {', content)

# Remove the state flows we moved to AuthViewModel
# We use re.sub with multiline to remove them
flows_to_remove = [
    r'\s*private val _loginMode = MutableStateFlow\([^)]+\)\s*val loginMode: StateFlow<String> = _loginMode\.asStateFlow\(\)\n',
    r'\s*private val _biometricRegistered = MutableStateFlow\([^)]+\)\s*val biometricRegistered: StateFlow<Boolean> = _biometricRegistered\.asStateFlow\(\)\n',
    r'\s*private val _biometricEnabled = MutableStateFlow\([^)]+\)\s*val biometricEnabled: StateFlow<Boolean> = _biometricEnabled\.asStateFlow\(\)\n',
    r'\s*private val _phoneVerificationRequired = MutableStateFlow\([^)]+\)\s*val phoneVerificationRequired: StateFlow<Boolean> = _phoneVerificationRequired\.asStateFlow\(\)\n',
    r'\s*private val _isGoogleAuthInProgress = MutableStateFlow\([^)]+\)\s*val isGoogleAuthInProgress: StateFlow<Boolean> = _isGoogleAuthInProgress\.asStateFlow\(\)\n',
    r'\s*private val _userName = MutableStateFlow\([^)]+\)\s*val userName: StateFlow<String> = _userName\.asStateFlow\(\)\n',
    r'\s*private val _userEmail = MutableStateFlow\([^)]+\)\s*val userEmail: StateFlow<String> = _userEmail\.asStateFlow\(\)\n',
]

for pattern in flows_to_remove:
    content = re.sub(pattern, '\n', content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Updated DeliveryViewModel.kt")
