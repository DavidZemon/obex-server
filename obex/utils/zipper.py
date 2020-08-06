import io
import os
import zipfile


def zip_dir(abs_path: str) -> memoryview:
    data = io.BytesIO()
    with zipfile.ZipFile(data, 'w') as ziph:
        for root, _, files in os.walk(abs_path):
            for f in files:
                abs_path_of_file = os.path.join(root, f)
                ziph.write(abs_path_of_file, os.path.relpath(abs_path_of_file, abs_path))
    return data.getbuffer()
