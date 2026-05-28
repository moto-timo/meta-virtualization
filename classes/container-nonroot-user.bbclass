# For secure and production environments, we want to run containers as a
# non-root user. Some applications, such as Python, require a $HOME
# directory with proper permissions. Because OCI_LAYERS :directories:
# copies with 'cp -a --no-preserve=ownership', we need a fixup function
# to create the proper permissions and ownership in a new raw layer.

# The behavior here is inspired by dhi.io/python:3 (Docker Hardened Image)

inherit extrausers

NONROOT_USER ?= "nonroot"
NONROOT_UID ?= "65532"
NONROOT_GID ?= "65532"

# ---------------------------------------------------------------------------
# Create the unprivileged "nonroot" user (uid 65532, group 65532)
# ---------------------------------------------------------------------------
EXTRA_USERS_PARAMS = "\
    groupadd -g ${NONROOT_GID} ${NONROOT_USER}; \
    useradd -m -u ${NONROOT_UID} -g ${NONROOT_GID} -d /home/${NONROOT_USER}  ${NONROOT_USER}; \
"

# Allow a container to choose to run as 'root'
OCI_IMAGE_RUNTIME_UID ?= "${NONROOT_UID}"
OCI_IMAGE_ENV_VARS = "HOME=/home/${NONROOT_USER}"

# Make sure we can write to e.g. /home/nonroot/.python_history
# using :directories: in OCI_LAYERS does not preserve permissions
fakeroot fix_oci_home_perms() {
    cd ${IMGDEPLOYDIR}
    image_name="${IMAGE_NAME}${IMAGE_NAME_SUFFIX}-oci"
    layer_tar="${WORKDIR}/oci-home-fix-layer.tar"

    rm -f "$layer_tar"

    python3 - "$layer_tar" <<'PYEOF'
import sys, tarfile, time

layer_tar = sys.argv[1]
mtime = int(time.time())

# (path, mode, uid, gid)
entries = [
    ("home",         0o755, 0,     0),
    ("home/${NONROOT_USER}", 0o700, ${NONROOT_UID}, ${NONROOT_GID}),
]

with tarfile.open(layer_tar, "w") as tar:
    for name, mode, uid, gid in entries:
        info = tarfile.TarInfo(name=name)
        info.type  = tarfile.DIRTYPE
        info.mode  = mode
        info.uid   = uid
        info.gid   = gid
        info.uname = ""   # numeric-only; let umoci canonicalize
        info.gname = ""
        info.mtime = mtime
        tar.addfile(info)
PYEOF

    umoci raw add-layer --image "$image_name:${OCI_IMAGE_TAG}" "$layer_tar"
    rm -f "$layer_tar"

    rm -f "$image_name.tar" "$image_name-dir.tar"
    ( cd "$image_name" && tar -cf "../$image_name.tar" "." )
    tar -cf "$image_name-dir.tar" "$image_name"
}
do_image_oci[postfuncs] += "fix_oci_home_perms"
