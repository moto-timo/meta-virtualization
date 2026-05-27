SUMMARY = "Base python3 container image"
DESCRIPTION = "OCI container image running Python with non-root user. \
\
In "dev" mode, can optionally run as 'root' and add 'pip' to allow \
developers to simply run 'pip install' on top of this container (Not \
advised for production/hardened use)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

# Multi-layer mode: create explicit layers instead of single rootfs layer
OCI_LAYER_MODE = "multi"

# Optional 'dev' mode:
#   - adds python3-pip to the python layer (enables `pip install` at runtime)
#   - runs the container as root (UID 0) so pip can write to site-packages
# Enable with: PACKAGECONFIG:pn-app-container-python = "dev"
PACKAGECONFIG ??= ""
PACKAGECONFIG[dev] = ""

# Define layers: each layer contains specific packages
# Format: "name:type:content" where content uses + as delimiter for multiple items
OCI_LAYERS = "\
    base:packages:base-files+base-passwd+netbase \
    terminal:packages:ncurses-terminfo-base \
    python:packages:python3+coreutils${@bb.utils.contains('PACKAGECONFIG', 'dev', '+python3-pip', '', d)} \
"

# In 'dev' mode, override the nonroot UID inherited from container-nonroot-user
# so the container runs as root (required for `pip install`).
OCI_IMAGE_RUNTIME_UID = "${@bb.utils.contains('PACKAGECONFIG', 'dev', '0', '${NONROOT_UID}', d)}"

# Use CMD so `docker run image /bin/sh` works as expected
OCI_IMAGE_CMD = "python3"

IMAGE_FSTYPES = "container oci"
inherit image
inherit image-oci
inherit container-nonroot-user

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
NO_RECOMMENDATIONS = "1"

# IMAGE_INSTALL triggers package builds via do_rootfs recrdeptask.
# Even for multi-layer mode, list packages here to ensure they're built.
# The PM will install them directly to layers from DEPLOY_DIR_IPK.
# Note: IMAGE_ROOTFS is still created but ignored for packages layers.
IMAGE_INSTALL = "base-files base-passwd netbase"
IMAGE_INSTALL += "ncurses-terminfo-base"
IMAGE_INSTALL += "python3 coreutils"
IMAGE_INSTALL += "${@bb.utils.contains('PACKAGECONFIG', 'dev', 'python3-pip', '', d)}"

# Allow build with or without a specific kernel
IMAGE_CONTAINER_NO_DUMMY = "1"

# Note: No ROOTFS_POSTPROCESS_COMMAND needed - IMAGE_ROOTFS is empty
# and PM handles installation directly to OCI layers
