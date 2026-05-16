import os

static_dir = r'd:\毕设\demo\wms-web\src\main\resources\static'
bom = b'\xef\xbb\xbf'

for filename in ['index.html', 'login.html']:
    filepath = os.path.join(static_dir, filename)
    with open(filepath, 'rb') as f:
        content = f.read()
    
    if content.startswith(bom):
        new_content = content[3:]
        with open(filepath, 'wb') as f:
            f.write(new_content)
        print(f"Removed BOM from: {filename}")
    else:
        print(f"No BOM: {filename}")