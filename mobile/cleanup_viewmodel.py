import re

file_path = r"d:\Eng App\mobile\app\src\main\java\com\esdispatch\viewmodel\DeliveryViewModel.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Remove the specific conflicting properties and functions
patterns = [
    r'\s*fun setGoogleAuthInProgress\([^)]*\)\s*\{[^}]*\}',
    r'\s*private val _loginMode = MutableStateFlow\([^)]*\)\s*val loginMode: StateFlow<String> = _loginMode\.asStateFlow\(\)',
    r'\s*fun setLoginMode\([^)]*\)\s*\{[^}]*\}',
    r'\s*fun setBiometricRegistered\([^)]*\)\s*\{[^}]*\}',
    r'\s*fun setBiometricEnabled\([^)]*\)\s*\{[^}]*\}',
    # Also clean up others that might have been missed
    r'\s*private val _biometricRegistered = MutableStateFlow\([^)]*\)\s*val biometricRegistered: StateFlow<Boolean> = _biometricRegistered\.asStateFlow\(\)',
    r'\s*private val _biometricEnabled = MutableStateFlow\([^)]*\)\s*val biometricEnabled: StateFlow<Boolean> = _biometricEnabled\.asStateFlow\(\)',
    r'\s*private val _phoneVerificationRequired = MutableStateFlow\([^)]*\)\s*val phoneVerificationRequired: StateFlow<Boolean> = _phoneVerificationRequired\.asStateFlow\(\)',
    r'\s*private val _isGoogleAuthInProgress = MutableStateFlow\([^)]*\)\s*val isGoogleAuthInProgress: StateFlow<Boolean> = _isGoogleAuthInProgress\.asStateFlow\(\)',
    r'\s*private val _userName = MutableStateFlow\([^)]*\)\s*val userName: StateFlow<String> = _userName\.asStateFlow\(\)',
    r'\s*private val _userEmail = MutableStateFlow\([^)]*\)\s*val userEmail: StateFlow<String> = _userEmail\.asStateFlow\(\)',
]

for p in patterns:
    content = re.sub(p, '', content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Cleaned up remaining Auth functions from DeliveryViewModel.kt")
