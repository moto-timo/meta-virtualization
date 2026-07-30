SUMMARY = "Base NGINX container image for development"
DESCRIPTION = "OCI container with NGINX web server."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

# Multi-layer mode: create explicit layers instead of single rootfs layer
OCI_LAYER_MODE = "multi"

OCI_IMAGE_APP_RECIPE = "nginx"

# Define layers: each layer contains specific packages
# Format: "name:type:content" where content uses + as delimiter for multiple items
OCI_LAYERS = "\
    base:packages:base-files+base-passwd+netbase \
    nginx:packages:nginx \
    nginx-dirs:directories:${localstatedir}/log/nginx+/run/nginx+${localstatedir}/volatile/tmp+${localstatedir}/volatile/log \
    nginx-files:files:${localstatedir}/log/nginx/access.log+${localstatedir}/log/nginx/error.log \
"
# Use CMD so `docker run image /bin/sh` works as expected
OCI_IMAGE_CMD = ""

IMAGE_FSTYPES = "container oci"
inherit image
inherit image-oci

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
NO_RECOMMENDATIONS = "1"

IMAGE_INSTALL = " \
       base-files \
       base-passwd \
       netbase \
       nginx \
"

# Allow build with or without a specific kernel
IMAGE_CONTAINER_NO_DUMMY = "1"

# Workaround /var/volatile for now
ROOTFS_POSTPROCESS_COMMAND:append = " rootfs_fixup_var_volatile ; "
rootfs_fixup_var_volatile () {
    install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log/nginx

    # Fix do_image_oci warnings
    # OCI: File not found in IMAGE_ROOTFS: /var/log/nginx/access.log
    touch ${IMAGE_ROOTFS}/${localstatedir}/volatile/log/nginx/access.log
    touch ${IMAGE_ROOTFS}/${localstatedir}/volatile/log/nginx/error.log

    # nginx opens the compiled-in error_log path before reading -c config.
    # /var/log is typically a symlink to /var/volatile/log in a Yocto rootfs,
    # so create the target path explicitly to guarantee the directory lands in
    # the container layer regardless of symlink resolution order.
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/log
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/log/nginx

    # nginx's compiled-in temp paths (client_body_temp, proxy_temp, etc.) all
    # live under /run/nginx, which is not created by any package.
    install -m 755 -d ${IMAGE_ROOTFS}/run/nginx
}

OCI_IMAGE_ENTRYPOINT = "/usr/sbin/nginx"
OCI_IMAGE_ENTRYPOINT_ARGS = "-g 'daemon off; error_log stderr notice;'"
OCI_IMAGE_PORTS = "80/tcp"
OCI_IMAGE_TAG = "latest"

SKIP_RECIPE[app-container-nginx] ?= "${@bb.utils.contains('BBFILE_COLLECTIONS', 'webserver', '', 'Depends on meta-webserver which is not included', d)}"
