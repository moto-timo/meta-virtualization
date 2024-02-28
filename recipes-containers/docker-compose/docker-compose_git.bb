HOMEPAGE = "https://github.com/docker/compose"
SUMMARY =  "Multi-container orchestration for Docker"
DESCRIPTION = "Docker compose v2"

DEPENDS = " \
    go-md2man \
    rsync-native \
"

# Specify the first two important SRCREVs as the format
SRCREV_FORMAT="compose_survey"
SRCREV_compose = "5c4f33702ef1f0f4f7d3476aade736c74f5c0851"

SRC_URI = "git://github.com/docker/compose;name=compose;branch=main;protocol=https"

include src_uri.inc

# patches and config
SRC_URI += "file://modules.txt"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/import/LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

GO_IMPORT = "import"

PV = "v2.24.6"

COMPOSE_PKG = "github.com/docker/compose/v2"

inherit go goarch
inherit pkgconfig

COMPATIBLE_HOST = "^(?!mips).*"

do_configure[noexec] = "1"

PACKAGECONFIG ?= "docker-plugin"
PACKAGECONFIG[docker-plugin] = ",,,docker"

include relocation.inc

GOBUILDFLAGS:append = " -mod=vendor"
do_compile() {
    	cd ${S}/src/import

	export GOPATH="$GOPATH:${S}/src/import/.gopath"

	# Pass the needed cflags/ldflags so that cgo
	# can find the needed headers files and libraries
	export GOARCH=${TARGET_GOARCH}
	export CGO_ENABLED="1"
	export CGO_CFLAGS="${CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export CGO_LDFLAGS="${LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"

        # temp. make slices available to the build
	(
          cd ${STAGING_LIBDIR}/go/src/
          ln -sf ${S}/src/import/vendor/golang.org/x/exp/slices .
	  # this is not a sufficient replacement
	  ln -sf ${S}/src/import/vendor/github.com/google/go-cmp/cmp .
	)

	# our copied .go files are to be used for the build
	ln -sf vendor.copy vendor
	# inform go that we know what we are doing
	cp ${WORKDIR}/modules.txt vendor/

	GO_LDFLAGS="-s -w -X internal.Version=${PV} -X ${COMPOSE_PKG}/internal.Version=${PV}"
	GO_BUILDTAGS=""
	mkdir -p ./bin
	${GO} build ${GOBUILDFLAGS} -tags "$GO_BUILDTAGS" -ldflags "$GO_LDFLAGS" -o ./bin/docker-compose ./cmd
}

do_install() {
	if ${@bb.utils.contains('PACKAGECONFIG', 'docker-plugin', 'true', 'false', d)}; then
		install -d ${D}${nonarch_libdir}/docker/cli-plugins
		install -m 755 ${S}/src/import/bin/docker-compose ${D}${nonarch_libdir}/docker/cli-plugins
	else
		install -d ${D}${bindir}
		install -m 755 ${S}/src/import/bin/docker-compose ${D}${bindir}
	fi
}


FILES:${PN} += " ${nonarch_libdir}/docker/cli-plugins/"

INHIBIT_PACKAGE_STRIP = "1"
INSANE_SKIP:${PN} += "ldflags already-stripped"
