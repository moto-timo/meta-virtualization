# Workaround /var/volatile for now
#
# AI-generated (Claude Code, Claude Opus 5.5).
# Use :append, not +=: image.bbclass pulls in rootfs-postcommands via
# inherit_defer, so its "+= empty_var_volatile" lands after anything a
# recipe adds with +=. empty_var_volatile then deletes everything under
# /var/volatile (base-files' fstab mounts tmpfs there), undoing this fixup.
# :append is applied after all += operations, so this runs afterwards.
ROOTFS_POSTPROCESS_COMMAND:append = " rootfs_fixup_var_volatile"
rootfs_fixup_var_volatile () {
    install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
}
