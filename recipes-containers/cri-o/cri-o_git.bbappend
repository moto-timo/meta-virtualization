SRC_URI += "\
    file://100-crio-bridge.conf \
    file://200-loopback.conf \
    "

inherit cni_networking

CNI_NETWORKING_FILES_append = " ${WORKDIR}/100-crio-bridge.conf ${WORKDIR}/200-loopback.conf"

do_install_append() {
  sed -e 's/cgroup_manager = "cgroupfs"/cgroup_manager = "systemd"/g' ${D}/${sysconfdir}/crio/crio.conf
}
