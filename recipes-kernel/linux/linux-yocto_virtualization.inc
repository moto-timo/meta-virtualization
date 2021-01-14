FILESEXTRAPATHS_prepend := "${THISDIR}/linux-yocto:"

SRC_URI += "file://xt-checksum.scc \
            file://ebtables.scc \
	    file://vswitch.scc \
	    file://lxc.scc \
            file://docker.scc \
	    "
KERNEL_FEATURES_append = " ${@bb.utils.contains('DISTRO_FEATURES', 'kvm', 'features/kvm/qemu-kvm-enable.scc', '', d)}"

KERNEL_MODULE_AUTOLOAD += "nf_conntrack_ipv6 openvswitch"
KERNEL_MODULE_AUTOLOAD += "${@bb.utils.contains('DISTRO_FEATURES', 'kvm', 'kvm', '', d)}"

# aufs kernel support required for xen-image-minimal
KERNEL_FEATURES_append += "${@bb.utils.contains('DISTRO_FEATURES', 'aufs', ' features/aufs/aufs-enable.scc', '', d)}"

KERNEL_FEATURES_append = " cfg/virtio.scc"

# xen kernel support
SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'xen', ' file://xen.scc', '', d)}"

# kubeadm recommends cgroup-hugetlb and requires cgroup_memory
# so APPEND += "cgroup_enable=memory cgroup_memory=1" in your .conf
# it also requires /var/lib/modules/<kversion>/build/.config which is
# provided by kernel-devsrc so add it to your image
KERNEL_FEATURES_append += "${@bb.utils.contains('DISTRO_FEATURES', 'k8s', ' features/hugetlb/hugetlb.scc', '', d)}"
SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'k8s', 'file://cgroup-hugetlb.scc', '', d)}"

