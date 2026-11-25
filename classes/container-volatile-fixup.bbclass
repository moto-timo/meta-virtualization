# Workaround /var/volatile for now
# :append: run after empty_var_volatile (rootfs-postcommands, inherit_defer)
ROOTFS_POSTPROCESS_COMMAND:append = " rootfs_fixup_var_volatile"
rootfs_fixup_var_volatile () {
    install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
}
