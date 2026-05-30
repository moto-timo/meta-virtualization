SUMMARY = "Curl Application container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

# Multi-layer mode: create explicit layers instead of single rootfs layer
OCI_LAYER_MODE = "multi"

# Optional 'dev' mode:
#   - runs the container as root (UID 0)
# Enable with: PACKAGECONFIG:pn-app-container-curl = "dev"
PACKAGECONFIG ??= ""
PACKAGECONFIG[dev] = ""

# For 'dev' mode, we want a shell, but for default 'production' intent we do not for security purposes.
CONTAINER_SHELL ??= "${@bb.utils.contains('PACKAGECONFIG', 'dev', 'busybox', '', d)}"
# If the following is configured in local.conf (or the distro):
#      PACKAGE_EXTRA_ARCHS:append = " container-dummy-provides"
# 
# it has been explicitly # indicated that we don't want or need a shell, so we'll
# add the dummy provides.
# 
# This is required, since there are postinstall scripts in base-files and base-passwd
# that reference /bin/sh and we'll get a rootfs error if there's no shell or no dummy
# provider.
CONTAINER_SHELL ?= "${@bb.utils.contains('PACKAGE_EXTRA_ARCHS', 'container-dummy-provides', 'container-dummy-provides', 'busybox', d)}"


# Define layers: each layer contains specific packages
# Format: "name:type:content" where content uses + as delimiter for multiple items
OCI_LAYERS = "\
    base:packages:base-files+base-passwd+netbase \
    ${@bb.utils.contains('PACKAGECONFIG', 'dev', 'shell:packages:${CONTAINER_SHELL}', '', d)} \
    curl:packages:curl+ca-certificates \
"

# In 'dev' mode, override the nonroot UID inherited from container-nonroot-user
# so the container runs as root.
OCI_IMAGE_RUNTIME_UID = "${@bb.utils.contains('PACKAGECONFIG', 'dev', '0', '${NONROOT_UID}', d)}"

IMAGE_FSTYPES = "container oci"
inherit image
inherit image-oci
inherit container-nonroot-user
inherit container-volatile-fixup

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
NO_RECOMMENDATIONS = "1"

# Allow build with or without a specific kernel
IMAGE_CONTAINER_NO_DUMMY = "1"

OCI_IMAGE_ENTRYPOINT = "curl"
OCI_IMAGE_TAG = "latest"
OCI_IMAGE_ENTRYPOINT_ARGS = "--help"
