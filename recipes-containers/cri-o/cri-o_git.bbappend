SRC_URI += "\
    file://100-crio-bridge.conf \
    file://200-loopback.conf \
    "

do_install_append() {
  install -d ${D}/${sysconfdir}/cni
  install -d ${D}/${sysconfdir}/cni/net.d
  install -m 0644 ${WORKDIR}/100-crio-bridge.conf ${D}${sysconfdir}/cni/net.d/
  install -m 0644 ${WORKDIR}/200-loopback.conf ${D}${sysconfdir}/cni/net.d/
}

FILES_${PN}-config += "${sysconfdir}/cni/net.d/*.conf"
