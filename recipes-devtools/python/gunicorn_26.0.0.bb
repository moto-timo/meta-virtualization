SUMMARY = "WSGI HTTP Server for UNIX"
DESCRIPTION = "\
  Gunicorn ‘Green Unicorn’ is a Python WSGI HTTP Server for UNIX. It’s \
  a pre-fork worker model ported from Ruby’s Unicorn project. The \
  Gunicorn server is broadly compatible with various web frameworks, \
  simply implemented, light on server resource usage, and fairly speedy. \
  " 
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5dc9171ccd8fcbd7827c850148b3ca98"

SRC_URI = "https://pypi.python.org/packages/source/g/gunicorn/${BPN}-${PV}.tar.gz"

SRC_URI[md5sum] = "3949514cc5b42ba2ca16a34d85823ac8"
SRC_URI[sha256sum] = "ca9346f85e3a4aeeb64d491045c16b9a35647abd37ea15efe53080eb8b090baf"

inherit python_pep517 python_setuptools_build_meta
