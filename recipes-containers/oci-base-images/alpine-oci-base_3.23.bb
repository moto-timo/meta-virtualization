# SPDX-License-Identifier: MIT
#
# Alpine OCI base image for use with OCI_BASE_IMAGE
#
# This recipe fetches Alpine Linux from Docker Hub and deploys it to
# DEPLOY_DIR_IMAGE for use as a base layer in multi-layer OCI builds.
#
# Usage in your container recipe:
#   OCI_BASE_IMAGE = "alpine-oci-base"
#   IMAGE_INSTALL = "base-files busybox myapp"
#
# The Alpine layers will be preserved, and your IMAGE_INSTALL packages
# are added as an additional layer on top.

SUMMARY = "Alpine Linux OCI base image"
DESCRIPTION = "Fetches Alpine Linux from Docker Hub for use as an OCI base layer"
HOMEPAGE = "https://alpinelinux.org/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit container-bundle

# Remote container from Docker Hub
CONTAINER_BUNDLES = "docker.io/library/alpine:3.23"

# REQUIRED: Pinned digest for reproducible builds
# Get with: skopeo inspect docker://docker.io/library/alpine:3.23 | jq -r '.Digest'
CONTAINER_DIGESTS[docker.io_library_alpine_3.23] = "sha256:5b10f432ef3da1b8d4c7eb6c487f2f5a8f096bc91145e68878dd4a5019afde11"

# Enable deployment to DEPLOY_DIR_IMAGE for use as OCI base layer
CONTAINER_BUNDLE_DEPLOY = "1"
