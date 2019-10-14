import os
import itertools
from glob import glob
from django.http import HttpResponse, JsonResponse, HttpResponseNotAllowed, HttpResponseServerError, HttpResponseBadRequest, HttpResponseNotFound
from collections import namedtuple
from django.shortcuts import render
import shutil
import tempfile
import subprocess
from datetime import datetime
from django.conf import settings

# test with
# pip install httpie
# then
#
# test a config file:
# http POST http://127.0.0.1:8000/nginx/test </etc/nginx/nginx.conf
# add a new version:
# http POST http://127.0.0.1:8000/nginx/set </etc/nginx/nginx.conf
# http http://127.0.0.1:8000/nginx/versions - returns an ordered json list
# http PUT http://127.0.0.1:8000/nginx/undo
# http PUT  http://127.0.0.1:8000/nginx/undo/N - goes back to version number N
# http PUT http://127.0.0.1:8000/nginx/reset - goes back the first version
#

class ConfigVersion:
    date_format = "%Y-%m-%d"

    def __init__(self, fn):
        self.basename, self.ext = os.path.splitext(fn)
        ps = self.basename.split('-', maxsplit=1)
        self.version = None
        self.date = None
        if ps:
            self.basename = ps[0]
        if len(ps) == 2:
            vps = ps[1].split('_')
            if len(vps) == 2 and vps[0].startswith('v'):
                try:
                    self.version = int(vps[0][1:])
                    self.date = datetime.strptime(vps[1], self.date_format)
                except ValueError:
                    pass

    @property
    def filename(self):
        if self.version and self.date:
            return "%s-v%03d_%s%s" % (self.basename, self.version, self.date_str, self.ext)
        self.basename + self.ext

    @property
    def date_str(self):
        if self.date:
            return self.date.strftime(self.date_format)
        return ''

    def __eq__(a, b):
        return a.filename == b.filename

    def __lt__(a, b):
        return a.filename < b.filename

    def __gt__(a, b):
        return a.filename > b.filename



# nginx.conf is always the current and newest version.
def find_versions(path=settings.CONFIG_PATH):
    versions = list()
    for s in sorted(glob(os.path.join(path, "nginx-v*.conf"))):
        versions.append(ConfigVersion(s))
    return versions


def find_filename(version=None):
    if version is None:
        return os.path.join(settings.CONFIG_PATH, "nginx.conf")
    else:
        for fn in glob(os.path.join(settings.CONFIG_PATH, "nginx-v%03d_*.conf" % version)):
            return fn


def nginx_get(request, version=None):
    if request.method != 'GET':
        return HttpResponseNotAllowed(['GET'])
    fn = find_filename(version=version)
    if fn is None:
        return HttpResponseNotFound(content=f"The version {version} can not be found in the system.")
    with open(fn) as fd:
        response = HttpResponse(content=fd)
        response['Content-Type'] = "text/plain"
        response['Content-Disposition'] = "inline; filename=nginx.conf"
        return response


def nginx_get_versions(request):
    if request.method != 'GET':
        return HttpResponseNotAllowed(['GET'])
    return JsonResponse(
        [ { 'version': v.version, 'date': v.date_str } for v in find_versions() ],
        safe=False)


def reload_config():
    if settings.RECONF_COMMAND:
        if isinstance(settings.RECONF_COMMAND, str):
            lst = [ settings.RECONF_COMMAND ]
        else:
            lst = settings.RECONF_COMMAND
        for cmd in lst:
            p = subprocess.run(cmd, shell=True, capture_output=True)
        if p.returncode != 0:
            return HttpResponseServerError(content=p.stderr)
    return HttpResponse()



def nginx_undo(request, version=None):
    if request.method != 'PUT':
        return HttpResponseNotAllowed(['PUT'])

    vs = find_versions()
    if version is not None:
        fn = find_filename(version)
        if not fn:
            return HttpResponseNotFound(content=f"The version {version} can not be found in the system.")
        v = ConfigVersion(fn)
    elif not vs:
        return HttpResponseBadRequest(content=f"There are no versions to restore.")
    else:
        v = vs[-1]
    shutil.move(v.filename, find_filename(version=None))
    for t in vs:
        if t > v:
            os.unlink(t.filename)
    return reload_config()


def nginx_reset(request):
    if request.method != 'PUT':
        return HttpResponseNotAllowed(['PUT'])
    vs = find_versions()
    if vs:
        return nginx_undo(request, version=vs[0].version)
    return HttpResponse() # OK



def nginx_test_config(request):
    if request.method != 'POST':
        return HttpResponseNotAllowed(['POST'])
    if not request.body:
        return HttpResponseBadRequest(content=f"Empty file.")
    with tempfile.NamedTemporaryFile(mode="wb", dir=settings.CONFIG_PATH, delete=True) as tmp_conf:
        tmp_conf.write(request.body)
        tmp_conf.flush()
        p = subprocess.run(settings.TEST_COMMAND.format(filename=tmp_conf.name),
                           shell=True, capture_output=True)
        if p.returncode != 0:
            response = HttpResponseServerError(content=p.stderr)
            response['Content-Type'] = "text/plain"
            return response
    return HttpResponse() # OK


def nginx_set_config(request):
    config_name = os.path.join(settings.CONFIG_PATH, "nginx.conf")
    if request.method != 'POST':
        return HttpResponseNotAllowed(['POST'])
    if not request.body:
        return HttpResponseBadRequest(content=f"Empty file.")
    with tempfile.NamedTemporaryFile(mode="wb", dir=settings.CONFIG_PATH, delete=False) as tmp_conf:
        tmp_conf.write(request.body)
        tmp_conf.flush()
        vs = find_versions()
        if vs:
            v = vs[-1]
        else:
            v = ConfigVersion(config_name)
        if v.version is None:
            v.version = 1
        else:
            v.version += 1
        v.date = datetime.now()
        filename = tmp_conf.name
    shutil.move(config_name, v.filename)
    shutil.move(filename, config_name)
    return reload_config()
