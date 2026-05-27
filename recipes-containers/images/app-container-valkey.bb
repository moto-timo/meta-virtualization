SUMMARY = "Valkey key-value store container image"
DESCRIPTION = "OCI container running the Valkey in-memory key-value \
datastore, a flexible distributed datastore that supports both caching \
and beyond caching workloads."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

# Multi-layer mode: create explicit layers instead of single rootfs layer
OCI_LAYER_MODE = "multi"

# Optional 'dev' mode:
#   - runs the container as root (UID 0)
# Enable with: PACKAGECONFIG:pn-app-container-valkey = "dev"
PACKAGECONFIG ??= ""
PACKAGECONFIG[dev] = ""

# Define layers: each layer contains specific packages
# Format: "name:type:content" where content uses + as delimiter for multiple items
OCI_LAYERS = "\
    base:packages:base-files+base-passwd+netbase \
    valkey:packages:valkey \
"

IMAGE_FSTYPES = "container oci"
inherit image
inherit image-oci
inherit container-nonroot-user

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
NO_RECOMMENDATIONS = "1"

IMAGE_INSTALL = " \
    base-files \
    base-passwd \
    netbase \
    valkey \
"

# Allow build with or without a specific kernel
IMAGE_CONTAINER_NO_DUMMY = "1"

# Workaround /var/volatile for now
ROOTFS_POSTPROCESS_COMMAND += "rootfs_fixup_var_volatile ; "
rootfs_fixup_var_volatile () {
    install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
}

OCI_IMAGE_ENTRYPOINT = "${bindir}/valkey-server"
# The stock valkey.conf shipped by meta-oe is tuned for a host install
# (daemonize yes, syslog-enabled yes, bind 127.0.0.1). Override those at
# launch so the server stays in the foreground as PID 1, logs to stdout,
# and is reachable from outside the container.
OCI_IMAGE_ENTRYPOINT_ARGS = "'${sysconfdir}/valkey/valkey.conf' \
    --daemonize no \
    --syslog-enabled no \
    --bind '0.0.0.0 -::*' \
    --protected-mode no"
OCI_IMAGE_PORTS = "6379/tcp"
OCI_IMAGE_TAG = "latest"
