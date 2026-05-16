import os

pages_dir = r'd:\毕设\demo\wms-web\src\main\resources\static\pages'
bom = b'\xef\xbb\xbf'

for filename in os.listdir(pages_dir):
    if filename.endswith('.html'):
        filepath = os.path.join(pages_dir, filename)
        with open(filepath, 'rb') as f:
            content = f.read()
        
        if content.startswith(bom):
            new_content = content[3:]
            with open(filepath, 'wb') as f:
                f.write(new_content)
            print(f"Removed BOM from: {filename}")
        else:
            print(f"No BOM: {filename}")

print("\nDone!")