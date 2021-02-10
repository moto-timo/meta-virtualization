SRC_URI += "\
    file://config.yaml \
    "

do_install_append() {
  install -d ${D}${localstatedir}/lib
  install -d ${D}${localstatedir}/lib/kubelet
  install -m 0644 ${WORKDIR}/config.yaml ${D}${localstatedir}/lib/kubelet
}

FILES_${PN} += "${localstatedir}/lib/kubelet/config.yaml"
