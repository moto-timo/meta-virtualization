SUMMARY = "k9s - Kubernetes CLI To Manage Your Clusters In Style!"
DESCRIPTION = "K9s provides a terminal UI to interact with your Kubernetes \
clusters. The aim of this project is to make it easier to navigate, observe \
and manage your applications in the wild. K9s continually watches Kubernetes \
for changes and offers subsequent commands to interact with your observed \
resources."
HOMEPAGE = "https://k9scli.io/"
SECTION = "console/tools"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/${GO_IMPORT}/LICENSE;md5=2ee41112a44fe7014dce33e26468ba93 \
                    file://src/${GO_IMPORT}/COPYING;md5=8a0efbf4d390c59e0892b53a31ce5deb"

GO_IMPORT = "github.com/derailed/k9s"
SRC_URI = "git://${GO_IMPORT}.git;protocol=https"

PV = "0.24.1+git${SRCPV}"
SRCREV = "68981ff5007e9f7031eed1f17e4bc9d406d4d67a"

S = "${WORKDIR}/git"

inherit go-mod goarch

RDEPENDS_${PN}-dev += "bash"

do_compile() {
	set +e

	cd ${S}/src/import

	# Pass the needed cflags/ldflags so that cgo
	# can find the needed headers files and libraries
	export GOARCH=${TARGET_GOARCH}
	export CGO_ENABLED="1"
	export CGO_CFLAGS="${CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export CGO_LDFLAGS="${LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"

	oe_runmake build
}

do_install() {
	set +e
	install -d ${D}${bindir}
	install -m 0755 ${B}/src/${GO_IMPORT}/execs/k9s ${D}/${bindir}
}
INSANE_SKIP_${PN} += "already-stripped"
