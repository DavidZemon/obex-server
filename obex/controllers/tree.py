import json
import os
import subprocess
from dataclasses import dataclass, asdict
from enum import Enum
from typing import List

from django.http import HttpResponse, HttpRequest
from django.views import View


class EntryType(Enum):
    FILE = 'FILE',
    SYMLINK = 'SYMLINK',
    FOLDER = 'FOLDER'

    def __str__(self):
        return self.name


@dataclass(frozen=True)
class TreeEntry:
    name: str
    full_path: str
    entry_type: EntryType
    size: int = None
    children: List['TreeEntry'] = None
    target: str = None


class TreeView(View):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.obex_path = os.path.abspath(os.environ.get('OBEX_PATH', '/tmp/obex'))

    def get(self, _: HttpRequest) -> HttpResponse:
        include_list = [
            line.strip() for line in subprocess.check_output(['git', 'ls-files'], cwd=self.obex_path).decode().split()
        ]

        entries: List[TreeEntry] = []
        if os.path.exists(self.obex_path):
            entries = self._get_tree(self.obex_path, include_list)

        return HttpResponse(json.dumps([asdict(entry) for entry in entries], default=str),
                            content_type='application/json')

    def _get_tree(self, path: str, include_list: List[str]) -> List[TreeEntry]:
        result: List[TreeEntry] = []

        for entry_name in os.listdir(os.path.join(self.obex_path, path)):
            abs_path = os.path.join(path, entry_name)
            full_path = os.path.relpath(abs_path, self.obex_path)

            if (os.path.isdir(abs_path) or full_path in include_list) and full_path != '.git':
                if os.path.islink(abs_path):
                    result.append(
                        TreeEntry(entry_name, full_path, EntryType.SYMLINK, target=os.readlink(abs_path))
                    )
                elif os.path.isdir(abs_path):
                    children = self._get_tree(abs_path, include_list)
                    result.append(
                        TreeEntry(entry_name, full_path, EntryType.FOLDER, children=children)
                    )
                elif os.path.isfile(abs_path):
                    result.append(
                        TreeEntry(entry_name, full_path, EntryType.FILE, size=os.stat(abs_path).st_size)
                    )

        return result

    def _build_ignore_list(self) -> List[str]:
        with open(os.path.join(self.obex_path, '.gitignore')) as ignore_file:
            stripped = [line.strip() for line in ignore_file.readlines()]
        return [line for line in stripped if line and not line.startswith('#')]
