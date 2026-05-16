import os

pages_dir = r'd:\毕设\demo\wms-web\src\main\resources\static\pages'

for filename in os.listdir(pages_dir):
    if filename.endswith('.html'):
        filepath = os.path.join(pages_dir, filename)
        with open(filepath, 'rb') as f:
            first_3 = f.read(3)
            has_bom = first_3 == b'\xef\xbb\xbf'
            print(f"{filename}: {'BOM' if has_bom else 'NO BOM'} - {first_3.hex()}")