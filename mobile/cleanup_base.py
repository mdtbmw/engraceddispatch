import re

file_path = r"d:\Eng App\mobile\app\src\main\java\com\esdispatch\viewmodel\DeliveryViewModel.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Remove appContext
content = re.sub(r'var appContext: Context\? = null\n', '', content)

# Remove _firebaseUserId
content = re.sub(r'\s*private val _firebaseUserId = MutableStateFlow<String\?>\(null\)\n\s*val firebaseUserId: StateFlow<String\?> = _firebaseUserId\.asStateFlow\(\)\n', '\n', content)

def remove_function(name, text):
    start_idx = text.find(f"fun {name}(")
    if start_idx == -1:
        return text
    
    open_braces = 0
    in_function = False
    end_idx = -1
    
    for i in range(start_idx, len(text)):
        if text[i] == '{':
            open_braces += 1
            in_function = True
        elif text[i] == '}':
            open_braces -= 1
            
        if in_function and open_braces == 0:
            end_idx = i + 1
            break
            
    if end_idx != -1:
        nl_idx = text.rfind('\n', 0, start_idx)
        if nl_idx != -1:
            start_idx = nl_idx + 1
        return text[:start_idx] + text[end_idx:]
    return text

content = remove_function("savePref", content)
content = remove_function("logAdminActivity", content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)
print("Cleaned up BaseViewModel helpers from DeliveryViewModel")
