include runc.inc

SRCREV = "f8699560587ee98bdb14731cb80f92fc375642bc"
SRC_URI = " \
    git://github.com/opencontainers/runc;branch=release-1.5;protocol=https;destsuffix=${GO_SRCURI_DESTSUFFIX} \
    file://0001-Makefile-respect-GOBUILDFLAGS-for-runc-and-remove-re.patch \
    "
RUNC_VERSION = "1.5.0-rc.2"

# for compatibility with existing RDEPENDS that have existed since
# runc-docker and runc-opencontainers were separate
RPROVIDES:${PN} += "runc-docker"
RPROVIDES:${PN} += "runc-opencontainers"

CVE_PRODUCT = "runc"

LDFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'ld-is-gold', ' -fuse-ld=bfd', '', d)}"
