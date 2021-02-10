SRC_URI += "\
    file://crictl.yaml \
    "

do_install_append() {
  install -d ${D}/${sysconfdir}
  install -m 0644 ${WORKDIR}/crictl.yaml ${D}${sysconfdir}/
}

FILES_${PN}-config += "${sysconfdir}/crictl.yaml"
