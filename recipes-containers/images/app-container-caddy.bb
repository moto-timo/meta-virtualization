SUMMARY = "Base Caddy container image for development"
DESCRIPTION = "OCI container with the Caddy web server."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

# Multi-layer mode: create explicit layers instead of single rootfs layer
OCI_LAYER_MODE = "multi"

# Optional 'dev' mode:
#   - adds a shell to the container
#   - runs the container as root (UID 0)
# Enable with: PACKAGECONFIG:pn-app-container-caddy = "dev"
PACKAGECONFIG ??= ""
PACKAGECONFIG[dev] = ""
inherit container-dev-mode
NONROOT_USER = "caddy"

OCI_IMAGE_APP_RECIPE = "caddy"

# Define layers: each layer contains specific packages
# Format: "name:type:content" where content uses + as delimiter for multiple items
OCI_LAYERS = "\
    base:packages:base-files+base-passwd+netbase \
    ${@bb.utils.contains('PACKAGECONFIG', 'dev', 'shell:packages:${CONTAINER_SHELL}', '', d)} \
    caddy:packages:caddy \
"

# Caddy keeps certificates in XDG_DATA_HOME and its last config in
# XDG_CONFIG_HOME; the nonroot user (uid 65532) must own both.
NONROOT_OWNED_DIRS = "\
    ${localstatedir}/lib/caddy \
    ${localstatedir}/lib/caddy/config \
"
OCI_IMAGE_ENV_VARS:append = " \
    XDG_DATA_HOME=${localstatedir}/lib/caddy \
    XDG_CONFIG_HOME=${localstatedir}/lib/caddy/config \
"

# Use CMD so `docker run image /bin/sh` works as expected
OCI_IMAGE_CMD = ""

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

OCI_IMAGE_ENTRYPOINT = "/usr/bin/caddy"
OCI_IMAGE_ENTRYPOINT_ARGS = "run --config /etc/caddy/Caddyfile --adapter caddyfile"
OCI_IMAGE_PORTS = "80/tcp 443/tcp 443/udp"
OCI_IMAGE_TAG = "latest"

SKIP_RECIPE[app-container-caddy] ?= "${@bb.utils.contains('BBFILE_COLLECTIONS', 'webserver', '', 'Depends on meta-webserver which is not included', d)}"
