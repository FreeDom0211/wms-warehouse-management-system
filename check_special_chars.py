import os
import re

pages_dir = r'd:\毕设\demo\wms-web\src\main\resources\static\pages'

for filename in os.listdir(pages_dir):
    if filename.endswith('.html'):
        filepath = os.path.join(pages_dir, filename)
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            special_chars = re.findall(r'[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]', content)
            if special_chars:
                print(f"特殊字符在 {filename}: {set(special_chars)}")
            
            weird_chars = re.findall(r'[\u0080-\uFFFF]', content)
            if weird_chars:
                unique_chars = list(set(weird_chars))[:5]
                print(f"Unicode字符在 {filename}: {unique_chars}")
        except Exception as e:
            print(f"读取 {filename} 失败: {e}")

print("\nDone!")