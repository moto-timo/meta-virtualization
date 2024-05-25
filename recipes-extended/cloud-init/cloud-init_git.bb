DESCRIPTION = "Init scripts for use on cloud images"
HOMEPAGE = "https://github.com/canonical/cloud-init"
SECTION = "devel/python"
LICENSE = "GPL-3.0-only | Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c6dd79b6ec2130a3364f6fa9d6380408 \
    file://LICENSE-GPLv3;md5=d32239bcb673463ab874e80d47fae504 \
    file://LICENSE-Apache2.0;md5=3b83ef96387f14655fc854ddc3c6bd57 \
"

SRCREV = "d4661b15ba592b27dabb25691817e91edfaf1547"
SRC_URI = "git://github.com/canonical/cloud-init;branch=24.1.x;protocol=https \
    file://cloud-init-source-local-lsb-functions.patch \
    file://0001-setup.py-check-for-install-anywhere-in-args.patch \
"

PV = "v24.1.6+git"

S = "${WORKDIR}/git"

DISTUTILS_INSTALL_ARGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', '--init-system=sysvinit_deb', '', d)}"
DISTUTILS_INSTALL_ARGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '--init-system=systemd', '', d)}"

do_install:append() {
    # mimic install locations from setup.py
	install -d ${D}${libdir}/${BPN}
	mv ${D}${PYTHON_SITEPACKAGES_DIR}${libdir}/* ${D}/${libdir}/

	install -m 0755 ${S}/tools/uncloud-init ${D}${libdir}/${BPN}/uncloud-init
	install -m 0755 ${S}/tools/write-ssh-key-fingerprints ${D}${libdir}/${BPN}/write-ssh-key-fingerprints

	install -d ${D}${sysconfdir}/cloud
	mv ${D}${PYTHON_SITEPACKAGES_DIR}${sysconfdir}/cloud/* ${D}${sysconfdir}/cloud/

	install -d ${D}${datadir}/doc/${BPN}
	mv ${D}${PYTHON_SITEPACKAGES_DIR}${datadir}/doc/${BPN}/* ${D}${datadir}/doc/${BPN}/

	install -d ${D}${sysconfdir}/cloud
    ln -s ${libdir}/${BPN}/uncloud-init ${D}${sysconfdir}/cloud/uncloud-init
    ln -s ${libdir}/${BPN}/write-ssh-key-fingerprints ${D}${sysconfdir}/cloud/write-ssh-key-fingerprints

	install -d ${D}${datadir}/bash-completion/completions
	mv ${D}${PYTHON_SITEPACKAGES_DIR}${datadir}/bash-completion/completions/* ${D}${datadir}/bash-completion/completions/

	#install -m 755 -d ${D}/${baselib}/systemd/system
	#mv ${D}${PYTHON_SITEPACKAGES_DIR}/${baselib}/systemd/system/* ${D}/${baselib}/systemd/system/

	install -m 755 -d ${D}${sysconfdir}/systemd/system
	mv ${D}${PYTHON_SITEPACKAGES_DIR}${sysconfdir}/systemd/system/* ${D}${sysconfdir}/systemd/systemd/

	#install -m 755 -d ${D}${systemd_unitdir}/system-generators
	#mv ${D}${PYTHON_SITEPACKAGES_DIR}${systemd_unitdir}/system-generators/* ${D}${systemd_unitdir}/system-generators/

	#install -d ${D}${base_libdir}/udev/rules.d
	#mv ${D}${PYTHON_SITEPACKAGES_DIR}${base_libdir}/udev/rules.d/* ${D}${base_libdir}/udev/rules.d/
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
       install -m 755 -d ${D}${sysconfdir}/init.d/
       install -m 755 ${S}/sysvinit/debian/* ${D}${sysconfdir}/init.d/
    fi
}

inherit pkgconfig
inherit python_setuptools_build_meta
inherit python3-dir
inherit update-rc.d
inherit systemd

# setup.py calls "pkg-config systemd --variable=systemdsystemunitdir" and needs to find our dev manager
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'udev', '', d)}"

inherit python3native

PACKAGES += "${PN}-systemd"

FILES:${PN} += "${sysconfdir}/* \
                ${datadir}/* \
                ${nonarch_libdir}/${BPN}/*"

FILES:${PN}-systemd += "${systemd_unitdir}/*"
RDEPENDS:${PN}-systemd += " ${PN}"

INITSCRIPT_PACKAGES = "${PN}"
INITSCRIPT_NAME:${BPN} = "cloud-init"

DEPENDS += "python3-pyyaml-native \
            python3-requests-native \
            python3-jinja2-native \
           "

RDEPENDS:${PN} = "python3 \
                  python3-jinja2 \
                  python3-configobj \
                  python3-requests \
                  python3-jsonpatch \
                  python3-jsonschema \
                  python3-pyyaml \
                  python3-oauthlib \
                  python3-netifaces \
                  python3-charset-normalizer \
                  bash \
                 "

