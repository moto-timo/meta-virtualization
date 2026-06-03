SUMMARY = "The low-level, core functionality of boto 3."
HOMEPAGE = "https://github.com/boto/botocore"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=2ee41112a44fe7014dce33e26468ba93"

SRC_URI[sha256sum] = "17604607efe28894e947401379e569cc8f0fe2d69337ece98bd0c82d1bcfaf92"

inherit pypi setuptools3

RDEPENDS:${PN} += "python3-jmespath python3-dateutil python3-logging"
