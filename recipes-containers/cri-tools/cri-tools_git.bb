HOMEPAGE = "https://github.com/kubernetes-sigs/cri-tools"
SUMMARY = "CLI and validation tools for Kubelet Container Runtime Interface (CRI)"
DESCRIPTION = "What is the scope of this project? \
\
cri-tools aims to provide a series of debugging and validation tools for \
Kubelet CRI, which includes: \
\
  * crictl: CLI for kubelet CRI. \
  * critest: validation test suites for kubelet CRI. \
\
What is not in scope for this project? \
\
  * Building a new kubelet container runtime based on CRI. \
  * Managing pods/containers for CRI-compatible runtimes by end-users, e.g. \
    pods created by crictl may be removed automatically by kubelet because of \
    non-exist on the kube-apiserver. \
 "

SRCREV_cri-tools = "ec9e336fd8c21c4bab89a6aed2c4a138c8cfae75"
SRC_URI = "\
	git://github.com/kubernetes-sigs/cri-tools.git;branch=master;name=cri-tools \
	"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/import/LICENSE;md5=e3fc50a88d0a364313df4b21ef20c29e"

GO_IMPORT = "import"

PV = "1.20.0+git${SRCREV_cri-tools}"

RPROVIDES_${PN} += "crictl"
PACKAGES =+ "${PN}-critest"

inherit go
inherit goarch
inherit pkgconfig

EXTRA_OEMAKE="BUILDTAGS=''"

do_compile() {
	set +e

	# Pass the needed cflags/ldflags so that cgo
	# can find the needed headers files and libraries
	export CGO_ENABLED="1"
	export CGO_CFLAGS="${CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export CGO_LDFLAGS="${LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export GO=${GO}

	export CFLAGS=""
	export LDFLAGS=""

	cd ${S}/src/import

	oe_runmake binaries
}

FILES_${PN}-critest = "${bindir}/critest"

# don't clobber hooks.d
ALLOW_EMPTY_${PN} = "1"

INSANE_SKIP_${PN} += "ldflags already-stripped"

deltask compile_ptest_base

COMPATIBLE_HOST = "^(?!(qemu)?mips).*"
