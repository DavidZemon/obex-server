import mimetypes
import os
import urllib.parse

from django.http import HttpRequest, HttpResponse
from django.views import View

from obex.utils.zipper import zip_dir


class DownloadsView(View):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.obex_path = os.path.abspath(os.environ.get('OBEX_PATH', '/tmp/obex'))

    def get(self, request: HttpRequest, encoded_path) -> HttpResponse:
        path = urllib.parse.unquote(encoded_path)
        abs_path = os.path.join(self.obex_path, path)

        if path.endswith('.zip') and os.path.isdir(abs_path[:-4]):
            return self._get_dir(abs_path[:-4])
        elif os.path.exists(abs_path):
            return self._get_file(abs_path)
        elif path.endswith('.zip'):
            return HttpResponse(f'No such file ({path}) or directory ({path[:-4]})', status=400)
        else:
            return HttpResponse(f'No such file {path}', status=400)

    @classmethod
    def _get_dir(cls, abs_path: str) -> HttpResponse:
        zip_data = zip_dir(abs_path)
        response = HttpResponse(zip_data)
        response['Content-Type'] = mimetypes.guess_type('bogus.zip')[0]
        response['Content-Disposition'] = f'attachment; filename="{urllib.parse.quote(os.path.basename(abs_path))}.zip"'
        response['Content-Length'] = len(zip_data)
        return response

    @classmethod
    def _get_file(cls, abs_path: str) -> HttpResponse:
        with open(abs_path, 'rb') as f:
            response = HttpResponse(f.read())

        mime = mimetypes.guess_type(abs_path)
        response['Content-Type'] = mime[0] if mime[0] else 'application/octet-stream'
        response['Content-Disposition'] = f'attachment; filename="{urllib.parse.quote(os.path.basename(abs_path))}"'
        response['Content-Length'] = os.stat(abs_path).st_size
        return response
