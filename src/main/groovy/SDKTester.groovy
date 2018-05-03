import com.profitbricks.sdk.model.*
import groovy.time.TimeCategory
import org.apache.http.client.HttpResponseException

import static com.profitbricks.sdk.Commands.*

// default run target: just `gradle run`
// see also JVM args in build.gradle
class SDKTester {
    private final static rand = new Random(System.currentTimeMillis())
    private final rnd = {rand.ints().findFirst().asInt}

    final static void main(final String[] args) {
        def _start = new Date()
        String fakeId = '00000000-0000-0000-0000-000000000000'
        
        new SDKTester().with { try {

            // -------- location list ----------

            Location loc = new Location()
            def locations = loc.all
            assert locations.size() > 0
            assert locations.contains("us/las")

            // -------- location read ----------

            loc = loc.read("us/las") as Location
            assert loc.id == "us/las"

            // -------- datacenter create ----------

           DataCenter dc = new DataCenter(
                name: "Groovy SDK Test",
                location: 'us/las',
                description: 'Groovy SDK test datacenter'
            ).create() as DataCenter
            assert dc.name == 'Groovy SDK Test'
            assert dc.description == 'Groovy SDK test datacenter'
            assert dc.location == 'us/las'

            // -------- datacenter create failure ----------

            try {
                new DataCenter(
                        name: "Groovy SDK Test"
                ).create()
            } catch (HttpResponseException e) {
                assert e.statusCode == 422
            }

            // -------- datacenter list ----------

            assert dc.all.size() > 0

            // -------- datacenter read ----------

            assert dc == dc.read()

            // -------- datacenter read failure ----------

            assert new DataCenter().read(fakeId) == null

            // -------- datacenter update ---------

            dc.name = "Groovy SDK Test - RENAME"
            dc.description = 'Groovy SDK test datacenter - RENAME'
            assert dc.update()
            assert dc == dc.read()

            // -------- server create ---------

            Server s = new Server(
                dataCenter: dc,
                name: "Groovy SDK Test",
                cores: 1,
                ram: 1024,
                cpuFamily: 'INTEL_XEON',
                availabilityZone: 'ZONE_1'
            ).create()
            assert s.name == 'Groovy SDK Test'
            assert s.cores == 1
            assert s.ram == 1024
            assert s.cpuFamily == 'INTEL_XEON'
            assert s.availabilityZone == 'ZONE_1'

            // -------- server create failure ----------

            try {
                new Server(
                    dataCenter: dc,
                    name: "Groovy SDK Test",
                    ram: 1024
                ).create()
            } catch (HttpResponseException e) {
                assert e.statusCode == 422
            }

            // -------- server list ----------

            assert s.all.size() > 0

            // -------- server read ----------

            s = s.read()
            assert s.vmState =~ /(?i)running/
            assert s.name == 'Groovy SDK Test'
            assert s.cores == 1
            assert s.ram == 1024
            assert s.cpuFamily == 'INTEL_XEON'
            assert s.availabilityZone == 'ZONE_1'

            // -------- server read failure ----------

            assert s.read(fakeId) == null

            // -------- server update ---------

            s.name = "Groovy SDK Test - RENAME"
            assert s.update()
            s = s.read()
            assert s.name == "Groovy SDK Test - RENAME"

            // -------- server delete ---------

            assert s.delete()

            s = s.create()

            // -------- volume create ---------

            Volume vol = new Volume(
                    dataCenter: dc,
                    name: "Groovy SDK Test",
                    size: 2,
                    imageAlias: "ubuntu:latest",
                    sshKeys: ["ssh-rsa AAAAB3NzaC1yc..."]
            ).create()

            Volume v = new Volume(
                    dataCenter: dc,
                    name: "Groovy SDK Test",
                    size: 2,
                    type: "HDD",
                    bus: "VIRTIO",
                    availabilityZone: "ZONE_3",
                    image: vol.image,
                    sshKeys: ["ssh-rsa AAAAB3NzaC1yc..."]
            ).create()

            assert v.name == "Groovy SDK Test"
            assert v.size == 2
            assert v.image == vol.image
            assert v.type == "HDD"
            assert v.bus == "VIRTIO"
            assert v.availabilityZone == "ZONE_3"
            assert v.sshKeys == ["ssh-rsa AAAAB3NzaC1yc..."]

            // -------- volume create failure ----------

            try {
                new Volume(
                        dataCenter: dc,
                        name: "Groovy SDK Test",
                ).create()
            } catch (HttpResponseException e) {
                assert e.statusCode == 422
            }

            // -------- volume list ----------

            assert v.all.size() > 0

            // -------- read ----------

            v = v.read()
            assert v.name == "Groovy SDK Test"
            assert v.size == 2
            assert v.image == vol.image
            assert v.type == "HDD"
            assert v.bus == "VIRTIO"
            assert v.availabilityZone == "ZONE_3"

            // -------- volume read failure ----------

            assert v.read(fakeId) == null

            // -------- volume update ---------

            v.name = "Groovy SDK Test - RENAME"
            v.size = 5
            assert v.update()
            v = v.read()
            assert v.name == "Groovy SDK Test - RENAME"
            assert v.size == 5

            // -------- volume delete ---------

            assert vol.delete()

            // -------- lan create ---------

            LAN l = new LAN(
                dataCenter: dc,
                name: "Groovy SDK Test",
                _public: true
            ).create()
            assert l.name == "Groovy SDK Test"
            assert l._public == true

            // -------- lan list ----------

            assert l.all.size() > 0

            // -------- lan read ----------

            def lan = l.read()
            assert l == lan

            // -------- lan read failure ----------

            assert l.read(0) == null

            // -------- lan update ---------

            l.name = "Groovy SDK Test - RENAME"
            l._public = false
            assert l.update()
            lan = l.read()
            assert lan == lan

            // -------- lan delete ---------

            assert l.delete()

            l = l.create()

            // -------- nic create ---------

            NIC n = new NIC(
                server: s,
                lan: l,
                name: "Groovy SDK Test",
                firewallActive: true,
                ips: ["10.0.0.1"]
            ).create()
            assert n.name == "Groovy SDK Test"
            assert n.nat == false
            assert n.dhcp == true
            assert n.firewallActive == true
            assert n.ips.size() > 0
            assert n.lan.id == l.id

            // -------- nic list ----------

            assert n.all.size() > 0

            // -------- nic read ----------

            def nic = n.read()
            assert nic.id == n.id
            assert nic.name == n.name
            assert nic.nat == n.nat
            assert nic.dhcp == n.dhcp
            assert nic.firewallActive == n.firewallActive
            assert nic.ips.size() > 0
            assert nic.lan.id == n.lan.id

            // -------- NIC read failure ----------

            assert n.read(fakeId) == null

            // -------- nic update ---------

            n.name = "Groovy SDK Test - RENAME"
            n.firewallActive = false
            assert n.update()
            n = n.read()
            assert n.name == "Groovy SDK Test - RENAME"
            assert n.firewallActive == false

            // -------- nic delete ---------

            assert n.delete()

            n = nic.create()

            // -------- loadbalancer create ---------

            LoadBalancer lb = new LoadBalancer(
                dataCenter: dc,
                name: "Groovy SDK Test"
            ).create()
            assert lb.name == "Groovy SDK Test"
            assert lb.dhcp

            // -------- loadbalancer list ----------

            assert lb.all.size() > 0

            // -------- loadbalancer read ----------

            def lb1 = lb.read()
            assert lb1.id == lb.id
            assert lb1.name == "Groovy SDK Test"
            assert lb1.dhcp

            // -------- loadbalancer read failure ----------

            assert lb.read(fakeId) == null

            // -------- loadbalancer update ---------

            lb.name = "Groovy SDK Test - RENAME"
            assert lb.update()
            lb = lb.read()
            assert lb.name == "Groovy SDK Test - RENAME"

            // -------- loadbalancer delete ---------

            assert lb.delete()

            lb = lb1.create()

            // -------- IPBlock create ---------
            IPBlock ip = new IPBlock(
                location: dc.location,
                name: "Groovy SDK Test",
                size: 2
            ).create() as IPBlock

            assert ip.name == "Groovy SDK Test"
            assert ip.location == dc.location
            assert ip.size == 2

            // -------- IPBlock create failure ----------

            try {
                new IPBlock(
                        name: "Groovy SDK Test",
                        size: 1
                ).create()
            } catch (HttpResponseException e) {
                assert e.statusCode == 422
            }

            // -------- IPBlock list ---------

            assert ip.all.size() > 0

            // -------- IPBlock read ---------

            def ip1 = ip.read() as IPBlock
            assert ip1.id == ip.id
            assert ip1.name == "Groovy SDK Test"
            assert ip1.location == dc.location
            assert ip1.size == 2
            ip = ip1

            // -------- IPBlock read failure ----------

            assert ip.read(fakeId) == null

            // -------- firewall rule create ---------

            FirewallRule fw = new FirewallRule(
                nic: n,
                name: 'SSH',
                protocol: 'TCP',
                portRangeStart: '22',
                portRangeEnd: '22',
                sourceMac: '01:23:45:67:89:00'
            ).create()
            assert fw.name == 'SSH'
            assert fw.protocol == 'TCP'
            assert fw.sourceMac == '01:23:45:67:89:00'
            assert fw.portRangeStart == '22'
            assert fw.portRangeEnd == '22'
            assert fw.sourceIp == null
            assert fw.targetIp == null
            assert fw.icmpCode == null
            assert fw.icmpType == null

            // -------- firewall rule create failure ----------

            try {
                new FirewallRule(
                        nic: n,
                        name: 'Groovy SDK Test'
                ).create()
            } catch (HttpResponseException e) {
                assert e.statusCode == 422
            }

            // -------- firewall list ----------

            assert fw.all.size() > 0

            // -------- firewall read ----------

            def fwr = fw.read()
            assert fwr.id == fw.id
            fw = fwr
            assert fw.name == 'SSH'
            assert fw.protocol == 'TCP'
            assert fw.sourceMac == '01:23:45:67:89:00'
            assert fw.portRangeStart == '22'
            assert fw.portRangeEnd == '22'
            assert fw.sourceIp == null
            assert fw.targetIp == null
            assert fw.icmpCode == null
            assert fw.icmpType == null

            // -------- firewall rule read failure ----------

            assert fw.read(fakeId) == null

            // -------- firewall rule update ---------

            FirewallRule ufr = new FirewallRule(
                    nic: n,
                    id: fw.id,
                    name: "SSH - RENAME"
            )
            assert ufr.update()
            fw = fw.read()
            assert fw.name == "SSH - RENAME"

            // -------- firewall rule delete ---------

            assert fw.delete()

            // -------- image list ---------

            Image i = new Image()
            def allImages = i.all
            assert allImages.size() > 0

            // -------- image read ---------

            i = allImages.collect{i.read(it) as Image}.findAll{
                it._public &&
                it.location == dc.location &&
                it.licenceType =~ /(?i)linux/ &&
                it.imageType =~ /(?i)cdrom/
            }.first()
            assert i.location == dc.location
            assert i.name != null
            assert i.description == null
            assert ['HDD', 'CDROM'].contains(i.imageType)
            assert ['LINUX', 'WINDOWS', 'WINDOWS2016', 'UNKNOWN', 'OTHER'].contains(i.licenceType)
            assert i.cpuHotPlug instanceof Boolean
            assert i.cpuHotUnplug instanceof Boolean
            assert i.ramHotPlug instanceof Boolean
            assert i.ramHotUnplug instanceof Boolean
            assert i.nicHotPlug instanceof Boolean
            assert i.nicHotUnplug instanceof Boolean
            assert i.discVirtioHotPlug instanceof Boolean
            assert i.discVirtioHotUnplug instanceof Boolean
            assert i.discScsiHotPlug instanceof Boolean
            assert i.discScsiHotUnplug instanceof Boolean
            assert i._public instanceof Boolean

            // -------- image read failure ----------

            assert i.read(fakeId) == null

            // -------- commands: volume ---------

            // snapshot create
            Snapshot sn = snapshot(v, 'Groovy SDK Test', 'Groovy SDK test snapshot')
            assert sn.name == 'Groovy SDK Test'
            assert sn.description == 'Groovy SDK test snapshot'

            // snapshot restore
            assert restore(v, sn)

            // -------- snapshot list ---------

            assert sn.all.size() > 0

            // -------- snapshot read ---------

            Snapshot sn1 = sn.read() as Snapshot
            assert sn1.id == sn.id
            assert sn1.name == 'Groovy SDK Test'
            assert sn1.description == 'Groovy SDK test snapshot'
            assert sn1.location == dc.location
            assert sn1.size == v.size
            assert sn1.licenceType == v.licenceType
            sn = sn1

            // -------- snapshot read failure ----------

            assert sn.read(fakeId) == null

            // -------- snapshot update ---------

            sn1 = new Snapshot(
                    id: sn.id,
                    name: 'Groovy SDK Test - RENAME',
                    description: 'Groovy SDK test snapshot - RENAME'
            )
            assert sn1.update()
            sn = sn.read() as Snapshot
            assert sn.name == 'Groovy SDK Test - RENAME'
            assert sn.description == 'Groovy SDK test snapshot - RENAME'

            // -------- commands: server ---------

            // attach volume
            assert attach(s, v)
            // list attached volumes
            assert attachedVolumes(s).size() > 0
            // get attached volume
            Volume av = attachedVolume(s, v.id as String)
            assert av.id == v.id
            assert av.name == v.name
            assert av.size == v.size
            assert av.bus == v.bus
            assert av.type == v.type
            assert av.licenceType == v.licenceType
            // detach volume
            assert detach(s, v)

            // attach CDROM
            assert attach(s, i)
            // list attached CDROMs
            assert attachedCDROMs(s).size() > 0
            // get attached CDROM
            Image cri =  attachedCDROM(s, i.id as String)
            assert cri.id == i.id
            assert cri.name == i.name
            // detach CDROM
            assert detach(s, i)

            // stop server
            assert stop(s)
            assert (s = s.read()).vmState =~ /(?i)shutoff/
            // start server
            assert start(s)
            assert (s = s.read()).vmState =~ /(?i)running/
            // reboot server
            assert reboot(s)
            assert (s = s.read()).vmState =~ /(?i)running/

            // -------- commands: loadbalancer ---------

            // associate NIC to load balancer
            NIC asc = associate(lb, n)
            assert asc.id == n.id
            assert asc.name == n.name
            def loadBalancer = lb.read()

            // list associated NICs
            assert associatedNics(loadBalancer).size() > 0

            assert dissociate(lb, n)

            // -------- commands: contract ---------

            // list contract resources
            Contract ct = contract()
            assert ct.contractNumber instanceof Long

            // -------- LAN IP failover ----------

            assert n.delete()
            nic.ips = ip.ips
            nic.dhcp = false
            n = nic.create()
            l.ipFailover = [
                    new LAN.IPFailover(ip: ip.ips[1], nicUuid: n.id as String)
            ]

            assert l.update()

            // -------- user create ---------

            String email = "no-reply_${rnd()}@example.test.com"
            User u = new User(
                    firstname: "John",
                    lastname: "Doe",
                    email: email,
                    password: "secretpassword123",
                    administrator: true,
                    forceSecAuth: false
            ).create()

            assert u.firstname == "John"
            assert u.lastname == "Doe"
            assert u.email == email
            assert u.administrator

            // -------- user create failure ---------

            try {
                new User(
                        firstname: "John",
                        lastname: "Doe"
                ).create()
            } catch (HttpResponseException e) {
                assert e.statusCode == 422
            }

            // -------- user list ---------

            assert u.all.size() > 0

            // -------- user read ---------

            def u1 = u.read() as User
            assert u1.id == u.id
            assert u1.firstname == u.firstname
            assert u1.lastname == u.lastname
            assert u1.email == u.email
            assert u1.administrator == u.administrator
            assert u1.forceSecAuth == u.forceSecAuth
            assert u1.secAuthActive == u.secAuthActive
            u = u1

            // -------- user read failure ----------

            assert u.read(fakeId) == null

            // -------- user update ---------

            u.administrator = false
            assert u.update()
            u1 = u.read() as User
            assert u1.firstname == "John"
            assert u1.lastname == "Doe"
            assert u1.email == email
            assert !u1.administrator
            assert !u1.forceSecAuth

            // -------- group create ---------

            Group g = new Group(
                    name: 'Groovy SDK Test',
                    createDataCenter: true,
                    createSnapshot: true,
                    reserveIp: true,
                    accessActivityLog: true
            ).create()

            // -------- group create failure ---------

            try {
                new Group(
                        createDataCenter: true
                ).create()
            } catch (HttpResponseException e) {
                assert e.statusCode == 422
            }

            // -------- group list ---------

            assert g.all.size() > 0

            // -------- group read ---------

            def g1 = g.read() as Group
            assert g1.id == g.id
            assert g1.name == g.name
            assert g1.createDataCenter == g.createDataCenter
            assert g1.createSnapshot == g.createSnapshot
            assert g1.reserveIp == g.reserveIp
            assert g1.accessActivityLog == g.accessActivityLog

            // -------- group read failure ----------

            assert g.read(fakeId) == null

            // -------- group update ---------

            g.name = 'Groovy SDK Test - RENAME'
            g.createDataCenter = false
            assert g.update()
            g1 = g.read() as Group
            assert g1.id == g.id
            assert g1.name == 'Groovy SDK Test - RENAME'
            assert !g1.createDataCenter

            // -------- commands: group ---------

            // add user to group
            assert addGroupUser(g, u)
            // list group users
            assert listGroupUsers(g).size() > 0
            // remove user from group
            assert removeGroupUser(g, u)

            // -------- commands: share ---------

            Share sh = share(g, dc.id as String, true, true)
            assert sh.editPrivilege
            assert sh.sharePrivilege

            // -------- share list ---------

            assert sh.all.size() > 0

            // -------- share read ---------

            def sh1 = sh.read()
            assert sh1.id == sh.id
            assert sh1.editPrivilege == sh.editPrivilege
            assert sh1.sharePrivilege == sh.sharePrivilege

            // -------- share read failure ----------

            assert sh.read(fakeId) == null

            // -------- share update ---------

            sh.editPrivilege = false
            assert sh.update()
            sh1 = sh.read()
            assert sh1.id == sh.id
            assert !sh1.editPrivilege

            // -------- share delete ---------

            assert sh.delete()

            // -------- commands: resource ---------
            // list all resources
            assert resources().size() > 0

            // -------- datacenter resource list ---------

            Resource res = new Resource(type: 'datacenter')
            assert res.all.size() > 0

            // -------- datacenter resource get ---------

            def res1 = res.read(dc.id)
            assert res1.id == dc.id
            assert res1.type == res.type

            // -------- datacenter resource get failure ---------

            assert new Resource(type: 'datacenter').read(fakeId) == null

            // -------- snapshot resource list ---------

            res = new Resource(type: 'snapshot')
            assert res.all.size() > 0

            // -------- snapshot resource get ---------

            res1 = res.read(sn.id)
            assert res1.id == sn.id
            assert res1.type == res.type

            // -------- ipblock resource list ---------

            res = new Resource(type: 'ipblock')
            assert res.all.size() > 0

            // -------- ipblock resource get ---------

            res1 = res.read(ip.id)
            assert res1.id == ip.id
            assert res1.type == res.type

            // -------- image resource list ---------

            res = new Resource(type: 'image')
            assert res.all.size() > 0

            // -------- image resource get ---------

            res1 = res.read(i.id)
            assert res1.id == i.id
            assert res1.type == res.type

            // -------- group delete ---------

            assert g.delete()

            // -------- user delete ---------

            assert u.delete()

            // -------- datacenter delete ---------

            def _dc = dc.read(dc.id) as DataCenter
            assert _dc.delete()

            // -------- snapshot delete ---------

            assert sn.delete()

            // -------- IPBlock delete ---------

            assert ip.delete()

            // -------- request list ---------

            Request r = new Request()
            def rqs = r.all
            assert rqs.size() > 0

            // -------- request get ---------

            assert r.read(rqs[0]).id == rqs[0]

            // -------- request get failure ---------

            assert r.read(fakeId) == null

            // -------- request status---------

            assert requestStatus(r.all[0]) != null

            } finally {
                println "that took me ${TimeCategory.minus(new Date(), _start)}"
            }
        }
    }
}